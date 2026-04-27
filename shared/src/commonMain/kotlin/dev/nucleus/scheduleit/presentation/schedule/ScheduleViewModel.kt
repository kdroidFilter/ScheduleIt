package dev.nucleus.scheduleit.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nucleus.scheduleit.data.ScheduleRepository
import dev.nucleus.scheduleit.data.decodeBackupFromString
import dev.nucleus.scheduleit.data.encodeToString
import dev.nucleus.scheduleit.data.toBackup
import dev.nucleus.scheduleit.data.toSnapshot
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.domain.ScheduleEvent
import dev.nucleus.scheduleit.domain.ScheduleSettings
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Inject
@ContributesIntoMap(AppScope::class)
@ViewModelKey(ScheduleViewModel::class)
class ScheduleViewModel(
    private val repository: ScheduleRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ScheduleUiState())
    val state: StateFlow<ScheduleUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureDefaults()
            repository.observeSchedule().collect { snapshot ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        settings = snapshot.settings,
                        templates = snapshot.templates,
                        assignments = snapshot.assignments,
                        eventsByTemplate = snapshot.eventsByTemplate,
                    )
                }
            }
        }
    }

    fun onEvent(intent: ScheduleIntent) {
        when (intent) {
            ScheduleIntent.OpenSettings -> _state.update { it.copy(showSettings = true) }
            ScheduleIntent.CloseSettings -> _state.update { it.copy(showSettings = false) }

            is ScheduleIntent.RequestCreateEvent -> startCreate(intent.day, intent.startMinute)
            is ScheduleIntent.RequestEditEvent -> startEdit(intent.event)
            is ScheduleIntent.DeleteEvent -> viewModelScope.launch { repository.deleteEvent(intent.id) }
            is ScheduleIntent.UpdateDraft -> _state.update {
                it.copy(editor = it.editor?.copy(draft = intent.draft))
            }
            ScheduleIntent.DismissEditor -> _state.update { it.copy(editor = null) }
            ScheduleIntent.SaveEditor -> saveEditor()
            ScheduleIntent.DeleteEditor -> deleteEditor()

            is ScheduleIntent.ChangeHours -> updateHours(intent.startHour, intent.endHour)
            is ScheduleIntent.SetNotificationsEnabled -> setNotificationsEnabled(intent.enabled)
            ScheduleIntent.ExportData -> exportData()
            ScheduleIntent.ImportData -> importData()
            is ScheduleIntent.HideDay -> hideDay(intent.day)
            is ScheduleIntent.AssignDayToTemplate -> assignDay(intent.day, intent.templateId)
            is ScheduleIntent.AssignDayToNewTemplate -> assignDayToNew(intent.day)
            is ScheduleIntent.RenameTemplate -> renameTemplate(intent.id, intent.name)

            ScheduleIntent.DismissError -> _state.update { it.copy(errorMessage = null) }
        }
    }

    private fun startCreate(day: AppDayOfWeek, startMinute: Int) {
        val current = _state.value
        val templateId = current.assignments[day] ?: return
        val rounded = (startMinute / SLOT_MINUTES) * SLOT_MINUTES
        val draft = ScheduleEvent(
            id = 0L,
            templateId = templateId,
            title = "",
            startMinute = rounded.coerceAtLeast(current.settings.startMinute),
            endMinute = (rounded + SLOT_MINUTES).coerceAtMost(current.settings.endMinute),
            color = DEFAULT_COLOR,
            notes = "",
        )
        _state.update {
            it.copy(
                editor = EventEditorState(
                    mode = EventEditorState.Mode.Create,
                    templateId = templateId,
                    originDay = day,
                    draft = draft,
                ),
            )
        }
    }

    private fun startEdit(event: ScheduleEvent) {
        val current = _state.value
        val originDay = current.assignments.entries.firstOrNull { it.value == event.templateId }?.key
            ?: return
        _state.update {
            it.copy(
                editor = EventEditorState(
                    mode = EventEditorState.Mode.Edit,
                    templateId = event.templateId,
                    originDay = originDay,
                    draft = event,
                ),
            )
        }
    }

    private fun saveEditor() {
        val current = _state.value
        val editor = current.editor ?: return
        val draft = editor.draft
        val window = current.settings
        if (draft.endMinute <= draft.startMinute) {
            _state.update { it.copy(errorMessage = ErrorKey.InvalidRange) }
            return
        }
        if (draft.startMinute < window.startMinute || draft.endMinute > window.endMinute) {
            _state.update { it.copy(errorMessage = ErrorKey.OutsideWindow) }
            return
        }
        val siblings = current.eventsByTemplate[editor.templateId].orEmpty()
        if (draft.overlapsAnyOf(siblings)) {
            _state.update { it.copy(errorMessage = ErrorKey.Overlap) }
            return
        }
        viewModelScope.launch {
            repository.upsertEvent(draft)
            _state.update { it.copy(editor = null) }
        }
    }

    private fun deleteEditor() {
        val editor = _state.value.editor ?: return
        if (editor.draft.id == 0L) {
            _state.update { it.copy(editor = null) }
            return
        }
        viewModelScope.launch {
            repository.deleteEvent(editor.draft.id)
            _state.update { it.copy(editor = null) }
        }
    }

    private fun updateHours(startHour: Int, endHour: Int) {
        val newStart = startHour * 60
        val newEnd = endHour * 60
        if (newStart >= newEnd) return
        viewModelScope.launch {
            repository.updateSettings(ScheduleSettings(newStart, newEnd))
        }
    }

    private fun hideDay(day: AppDayOfWeek) {
        val current = _state.value
        val templateId = current.assignments[day] ?: return
        viewModelScope.launch {
            repository.hideDay(day)
            repository.deleteTemplateIfEmpty(templateId)
        }
    }

    private fun assignDay(day: AppDayOfWeek, templateId: Long) {
        val current = _state.value
        val previous = current.assignments[day]
        viewModelScope.launch {
            repository.assignDayToTemplate(day, templateId)
            if (previous != null && previous != templateId) {
                repository.deleteTemplateIfEmpty(previous)
            }
        }
    }

    private fun assignDayToNew(day: AppDayOfWeek) {
        val current = _state.value
        val previous = current.assignments[day]
        viewModelScope.launch {
            val newId = repository.createTemplate()
            repository.assignDayToTemplate(day, newId)
            if (previous != null && previous != newId) {
                repository.deleteTemplateIfEmpty(previous)
            }
        }
    }

    private fun renameTemplate(id: Long, name: String) {
        viewModelScope.launch { repository.renameTemplate(id, name) }
    }

    private fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setNotificationsEnabled(enabled) }
    }

    private fun exportData() {
        viewModelScope.launch {
            val snapshot = repository.snapshotOnce()
            val json = snapshot.toBackup().encodeToString()
            val file = FileKit.openFileSaver(
                suggestedName = "scheduleit-backup",
                extension = BACKUP_EXTENSION,
            )
            file?.writeString(json)
        }
    }

    private fun importData() {
        viewModelScope.launch {
            val file = FileKit.openFilePicker() ?: return@launch
            val text = runCatching { file.readString() }.getOrNull() ?: return@launch
            val backup = runCatching { decodeBackupFromString(text) }.getOrNull() ?: run {
                _state.update { it.copy(errorMessage = ErrorKey.InvalidBackup) }
                return@launch
            }
            repository.replaceAll(backup.toSnapshot())
        }
    }

    companion object {
        const val SLOT_MINUTES = 30
        const val DEFAULT_COLOR = 0xFF42A5F5L
        const val BACKUP_EXTENSION = "scheduleit"
    }
}
