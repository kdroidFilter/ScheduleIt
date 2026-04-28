package dev.nucleus.scheduleit.presentation.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.nucleus.scheduleit.data.ScheduleRepository
import dev.nucleus.scheduleit.data.decodeBackupFromString
import dev.nucleus.scheduleit.data.drive.GoogleDriveStatus
import dev.nucleus.scheduleit.data.drive.GoogleDriveSync
import dev.nucleus.scheduleit.data.encodeToString
import dev.nucleus.scheduleit.data.toBackup
import dev.nucleus.scheduleit.data.toSnapshot
import dev.nucleus.scheduleit.domain.AppDayOfWeek
import dev.nucleus.scheduleit.domain.DayEvent
import dev.nucleus.scheduleit.domain.EffectiveEvent
import dev.nucleus.scheduleit.domain.ScheduleEvent
import dev.nucleus.scheduleit.domain.ScheduleSettings
import dev.nucleus.scheduleit.domain.TemplateEventOverride
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
    private val googleDriveSync: GoogleDriveSync?,
) : ViewModel() {

    private val _state = MutableStateFlow(
        ScheduleUiState(googleDrive = googleDriveSync?.status?.value),
    )
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
                        dayEventsByDay = snapshot.dayEventsByDay,
                        overridesByDay = snapshot.overridesByDay,
                    )
                }
            }
        }
        googleDriveSync?.let { sync ->
            viewModelScope.launch {
                sync.status.collect { status ->
                    _state.update { it.copy(googleDrive = status) }
                }
            }
        }
    }

    fun onEvent(intent: ScheduleIntent) {
        when (intent) {
            ScheduleIntent.OpenSettings -> _state.update { it.copy(showSettings = true) }
            ScheduleIntent.CloseSettings -> _state.update { it.copy(showSettings = false) }

            is ScheduleIntent.RequestCreateEvent -> startCreate(intent.day, intent.startMinute)
            is ScheduleIntent.RequestEditEffectiveEvent -> startEdit(intent.effective)
            is ScheduleIntent.DeleteEffectiveEvent -> deleteEffective(intent.effective)
            is ScheduleIntent.HideEffectiveEvent -> hideEffective(intent.effective)
            is ScheduleIntent.CopyEvent -> copyEvent(intent.effective)
            is ScheduleIntent.PasteEventAt -> pasteAt(intent.day, intent.startMinute)
            is ScheduleIntent.UpdateDraft -> _state.update {
                it.copy(editor = it.editor?.copy(draft = intent.draft))
            }
            is ScheduleIntent.SetEditorScope -> _state.update {
                it.copy(editor = it.editor?.copy(scope = intent.scope))
            }
            ScheduleIntent.DismissEditor -> _state.update { it.copy(editor = null) }
            ScheduleIntent.SaveEditor -> saveEditor()
            ScheduleIntent.DeleteEditor -> deleteEditor()

            is ScheduleIntent.ChangeHours -> updateHours(intent.startHour, intent.endHour)
            is ScheduleIntent.SetNotificationsEnabled -> setNotificationsEnabled(intent.enabled)
            ScheduleIntent.ExportData -> exportData()
            ScheduleIntent.ImportData -> importData()
            ScheduleIntent.ResetData -> resetData()
            ScheduleIntent.ConnectGoogleDrive -> connectGoogleDrive()
            ScheduleIntent.DisconnectGoogleDrive -> disconnectGoogleDrive()
            ScheduleIntent.BackupNowToDrive -> backupNowToDrive()
            ScheduleIntent.RestoreFromDrive -> restoreFromDrive()
            is ScheduleIntent.HideDay -> hideDay(intent.day)
            is ScheduleIntent.AssignDayToTemplate -> assignDay(intent.day, intent.templateId)
            is ScheduleIntent.AssignDayToNewTemplate -> assignDayToNew(intent.day)
            is ScheduleIntent.RenameTemplate -> renameTemplate(intent.id, intent.name)

            ScheduleIntent.DismissError -> _state.update { it.copy(errorMessage = null) }
            is ScheduleIntent.ReportBlocked -> _state.update { it.copy(errorMessage = intent.reason) }
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
                    day = day,
                    templateId = templateId,
                    draft = draft,
                    scope = EventEditorState.Scope.ThisDayOnly,
                    original = null,
                    templateIsShared = current.isTemplateShared(templateId),
                ),
            )
        }
    }

    private fun startEdit(effective: EffectiveEvent) {
        val current = _state.value
        val day = effective.day
        val templateId = current.assignments[day] ?: return
        val (original, scope) = when (val s = effective.source) {
            is EffectiveEvent.Source.TemplateShared ->
                EventEditorState.Original.TemplateEvent(s.event) to EventEditorState.Scope.AllLinkedDays
            is EffectiveEvent.Source.TemplateOverridden ->
                EventEditorState.Original.Overridden(s.base, s.override) to EventEditorState.Scope.ThisDayOnly
            is EffectiveEvent.Source.DayOnly ->
                EventEditorState.Original.DayOnly(s.event) to EventEditorState.Scope.ThisDayOnly
        }
        val draft = ScheduleEvent(
            id = 0L,
            templateId = templateId,
            title = effective.title,
            startMinute = effective.startMinute,
            endMinute = effective.endMinute,
            color = effective.color,
            notes = effective.notes,
        )
        _state.update {
            it.copy(
                editor = EventEditorState(
                    mode = EventEditorState.Mode.Edit,
                    day = day,
                    templateId = templateId,
                    draft = draft,
                    scope = scope,
                    original = original,
                    templateIsShared = current.isTemplateShared(templateId),
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
        val sameDayConflict = draft.overlapsAnyOf(current.effectiveEventsFor(editor.day), editor.original)
        if (sameDayConflict) {
            _state.update { it.copy(errorMessage = ErrorKey.Overlap) }
            return
        }
        if (editor.scope == EventEditorState.Scope.AllLinkedDays && editor.templateIsShared) {
            val otherDays = current.assignments.entries
                .filter { it.value == editor.templateId && it.key != editor.day }
                .map { it.key }
            for (other in otherDays) {
                if (draft.overlapsAnyOf(current.effectiveEventsFor(other), editor.original)) {
                    _state.update { it.copy(errorMessage = ErrorKey.Overlap) }
                    return
                }
            }
        }
        viewModelScope.launch {
            persistEditor(editor)
            _state.update { it.copy(editor = null) }
        }
    }

    private suspend fun persistEditor(editor: EventEditorState) {
        val draft = editor.draft
        when (editor.scope) {
            EventEditorState.Scope.ThisDayOnly -> when (val original = editor.original) {
                null -> repository.upsertDayEvent(
                    DayEvent(
                        id = 0L,
                        day = editor.day,
                        title = draft.title,
                        startMinute = draft.startMinute,
                        endMinute = draft.endMinute,
                        color = draft.color,
                        notes = draft.notes,
                    ),
                )
                is EventEditorState.Original.DayOnly -> repository.upsertDayEvent(
                    DayEvent(
                        id = original.event.id,
                        day = editor.day,
                        title = draft.title,
                        startMinute = draft.startMinute,
                        endMinute = draft.endMinute,
                        color = draft.color,
                        notes = draft.notes,
                    ),
                )
                is EventEditorState.Original.TemplateEvent -> repository.upsertOverride(
                    buildOverride(original.event, editor.day, draft),
                )
                is EventEditorState.Original.Overridden -> repository.upsertOverride(
                    buildOverride(original.base, editor.day, draft),
                )
            }
            EventEditorState.Scope.AllLinkedDays -> when (val original = editor.original) {
                null -> repository.upsertEvent(
                    ScheduleEvent(
                        id = 0L,
                        templateId = editor.templateId,
                        title = draft.title,
                        startMinute = draft.startMinute,
                        endMinute = draft.endMinute,
                        color = draft.color,
                        notes = draft.notes,
                    ),
                )
                is EventEditorState.Original.TemplateEvent -> repository.upsertEvent(
                    ScheduleEvent(
                        id = original.event.id,
                        templateId = editor.templateId,
                        title = draft.title,
                        startMinute = draft.startMinute,
                        endMinute = draft.endMinute,
                        color = draft.color,
                        notes = draft.notes,
                    ),
                )
                is EventEditorState.Original.Overridden -> {
                    repository.upsertEvent(
                        ScheduleEvent(
                            id = original.base.id,
                            templateId = editor.templateId,
                            title = draft.title,
                            startMinute = draft.startMinute,
                            endMinute = draft.endMinute,
                            color = draft.color,
                            notes = draft.notes,
                        ),
                    )
                    repository.deleteOverride(original.base.id, editor.day)
                }
                is EventEditorState.Original.DayOnly -> {
                    repository.upsertEvent(
                        ScheduleEvent(
                            id = 0L,
                            templateId = editor.templateId,
                            title = draft.title,
                            startMinute = draft.startMinute,
                            endMinute = draft.endMinute,
                            color = draft.color,
                            notes = draft.notes,
                        ),
                    )
                    repository.deleteDayEvent(original.event.id)
                }
            }
        }
    }

    private fun buildOverride(
        base: ScheduleEvent,
        day: AppDayOfWeek,
        draft: ScheduleEvent,
    ): TemplateEventOverride = TemplateEventOverride(
        baseEventId = base.id,
        day = day,
        hidden = false,
        title = draft.title.takeIf { it != base.title },
        startMinute = draft.startMinute.takeIf { it != base.startMinute },
        endMinute = draft.endMinute.takeIf { it != base.endMinute },
        color = draft.color.takeIf { it != base.color },
        notes = draft.notes.takeIf { it != base.notes },
    )

    private fun deleteEditor() {
        val editor = _state.value.editor ?: return
        val original = editor.original
        if (original == null) {
            _state.update { it.copy(editor = null) }
            return
        }
        viewModelScope.launch {
            when (original) {
                is EventEditorState.Original.TemplateEvent -> repository.deleteEvent(original.event.id)
                is EventEditorState.Original.DayOnly -> repository.deleteDayEvent(original.event.id)
                is EventEditorState.Original.Overridden -> repository.deleteOverride(original.base.id, editor.day)
            }
            _state.update { it.copy(editor = null) }
        }
    }

    private fun deleteEffective(effective: EffectiveEvent) {
        viewModelScope.launch {
            when (val s = effective.source) {
                is EffectiveEvent.Source.TemplateShared -> repository.deleteEvent(s.event.id)
                is EffectiveEvent.Source.TemplateOverridden -> repository.deleteOverride(s.base.id, effective.day)
                is EffectiveEvent.Source.DayOnly -> repository.deleteDayEvent(s.event.id)
            }
        }
    }

    private fun hideEffective(effective: EffectiveEvent) {
        viewModelScope.launch {
            when (val s = effective.source) {
                is EffectiveEvent.Source.TemplateShared ->
                    repository.hideTemplateEventForDay(s.event.id, effective.day)
                is EffectiveEvent.Source.TemplateOverridden ->
                    repository.upsertOverride(s.override.copy(hidden = true))
                is EffectiveEvent.Source.DayOnly ->
                    repository.deleteDayEvent(s.event.id)
            }
            _state.update { it.copy(editor = null) }
        }
    }

    private fun copyEvent(effective: EffectiveEvent) {
        _state.update {
            it.copy(
                clipboard = ClipboardEvent(
                    title = effective.title,
                    color = effective.color,
                    notes = effective.notes,
                    durationMinutes = effective.endMinute - effective.startMinute,
                ),
            )
        }
    }

    private fun pasteAt(day: AppDayOfWeek, startMinute: Int) {
        val current = _state.value
        val clipboard = current.clipboard ?: return
        val window = current.settings
        val snapped = (startMinute / SLOT_MINUTES) * SLOT_MINUTES
        val start = snapped.coerceAtLeast(window.startMinute)
        val end = start + clipboard.durationMinutes
        if (end > window.endMinute) {
            _state.update { it.copy(errorMessage = ErrorKey.OutsideWindow) }
            return
        }
        val draft = ScheduleEvent(
            id = 0L,
            templateId = current.assignments[day] ?: 0L,
            title = clipboard.title,
            startMinute = start,
            endMinute = end,
            color = clipboard.color,
            notes = clipboard.notes,
        )
        if (draft.overlapsAnyOf(current.effectiveEventsFor(day), editing = null)) {
            _state.update { it.copy(errorMessage = ErrorKey.Overlap) }
            return
        }
        viewModelScope.launch {
            repository.upsertDayEvent(
                DayEvent(
                    id = 0L,
                    day = day,
                    title = clipboard.title,
                    startMinute = start,
                    endMinute = end,
                    color = clipboard.color,
                    notes = clipboard.notes,
                ),
            )
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

    private fun resetData() {
        viewModelScope.launch { repository.resetAll() }
    }

    private fun connectGoogleDrive() {
        val sync = googleDriveSync ?: return
        viewModelScope.launch { sync.connect() }
    }

    private fun disconnectGoogleDrive() {
        val sync = googleDriveSync ?: return
        viewModelScope.launch { sync.disconnect() }
    }

    private fun backupNowToDrive() {
        val sync = googleDriveSync ?: return
        if (sync.status.value !is GoogleDriveStatus.Connected) return
        viewModelScope.launch {
            val snapshot = repository.snapshotOnce()
            val payload = snapshot.toBackup().encodeToString()
            sync.backup(payload)
        }
    }

    private fun restoreFromDrive() {
        val sync = googleDriveSync ?: return
        if (sync.status.value !is GoogleDriveStatus.Connected) return
        viewModelScope.launch {
            val payload = sync.restore() ?: return@launch
            val backup = runCatching { decodeBackupFromString(payload) }.getOrNull() ?: run {
                _state.update { it.copy(errorMessage = ErrorKey.InvalidBackup) }
                return@launch
            }
            repository.replaceAll(backup.toSnapshot())
        }
    }

    companion object {
        const val SLOT_MINUTES = 15
        const val DEFAULT_COLOR = 0xFF42A5F5L
        const val BACKUP_EXTENSION = "scheduleit"
    }
}
