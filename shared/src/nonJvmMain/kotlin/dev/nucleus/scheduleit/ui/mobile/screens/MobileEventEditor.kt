package dev.nucleus.scheduleit.ui.mobile.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.domain.EffectiveEvent
import dev.nucleus.scheduleit.domain.ScheduleSettings
import dev.nucleus.scheduleit.presentation.schedule.ErrorKey
import dev.nucleus.scheduleit.presentation.schedule.EventEditorState
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel
import dev.nucleus.scheduleit.presentation.schedule.computeEditorBounds
import dev.nucleus.scheduleit.ui.common.fullName
import dev.nucleus.scheduleit.ui.mobile.components.MobileTextField
import dev.nucleus.scheduleit.ui.mobile.components.MobileTimePicker
import dev.nucleus.scheduleit.ui.mobile.components.ModalSheet
import dev.nucleus.scheduleit.ui.mobile.components.SectionHeading
import dev.nucleus.scheduleit.ui.mobile.theme.EventColors
import dev.nucleus.scheduleit.ui.mobile.theme.MobileTheme
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_cancel
import scheduleit.shared.generated.resources.action_delete
import scheduleit.shared.generated.resources.action_hide_for_day
import scheduleit.shared.generated.resources.action_save
import scheduleit.shared.generated.resources.event_dialog_edit_title
import scheduleit.shared.generated.resources.event_dialog_new_title
import scheduleit.shared.generated.resources.event_field_color
import scheduleit.shared.generated.resources.event_field_end
import scheduleit.shared.generated.resources.event_field_notes
import scheduleit.shared.generated.resources.event_field_start
import scheduleit.shared.generated.resources.event_field_time
import scheduleit.shared.generated.resources.event_field_title
import scheduleit.shared.generated.resources.event_scope_all_days
import scheduleit.shared.generated.resources.event_scope_label
import scheduleit.shared.generated.resources.event_scope_this_day

@Composable
fun MobileEventEditor(
    editor: EventEditorState?,
    settings: ScheduleSettings,
    siblings: List<EffectiveEvent>,
    onIntent: (ScheduleIntent) -> Unit,
) {
    var lastEditor by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<EventEditorState?>(null) }
    androidx.compose.runtime.LaunchedEffect(editor) {
        if (editor != null) lastEditor = editor
    }
    val current = editor ?: lastEditor ?: return
    val draft = current.draft
    val bounds = computeEditorBounds(draft, siblings, current.original, settings)
    val title = stringResource(
        if (current.mode == EventEditorState.Mode.Create) {
            Res.string.event_dialog_new_title
        } else {
            Res.string.event_dialog_edit_title
        },
    )

    ModalSheet(
        visible = editor != null,
        title = title,
        onDismiss = { onIntent(ScheduleIntent.DismissEditor) },
        primaryLabel = stringResource(Res.string.action_save),
        cancelLabel = stringResource(Res.string.action_cancel),
        onPrimary = { onIntent(ScheduleIntent.SaveEditor) },
        fillHeight = 0.92f,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
        ) {
            if (current.templateIsShared) {
                ScopeSection(
                    dayLabel = current.day.fullName(),
                    scope = current.scope,
                    onScopeChange = { onIntent(ScheduleIntent.SetEditorScope(it)) },
                )
            }

            SectionHeading(text = stringResource(Res.string.event_field_title))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                MobileTextField(
                    value = draft.title,
                    onValueChange = { onIntent(ScheduleIntent.UpdateDraft(draft.copy(title = it))) },
                )
            }

            SectionHeading(text = stringResource(Res.string.event_field_time))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterHorizontally),
            ) {
                MobileTimePicker(
                    label = stringResource(Res.string.event_field_start),
                    valueMinute = draft.startMinute,
                    rangeStart = bounds.lowerMinute,
                    rangeEnd = draft.endMinute - ScheduleViewModel.SLOT_MINUTES,
                    stepMinutes = ScheduleViewModel.SLOT_MINUTES,
                    onChange = { v -> onIntent(ScheduleIntent.UpdateDraft(draft.copy(startMinute = v))) },
                    onBlocked = { atUpper ->
                        val reason = if (atUpper) ErrorKey.InvalidRange else bounds.lowerReason
                        onIntent(ScheduleIntent.ReportBlocked(reason))
                    },
                )
                MobileTimePicker(
                    label = stringResource(Res.string.event_field_end),
                    valueMinute = draft.endMinute,
                    rangeStart = draft.startMinute + ScheduleViewModel.SLOT_MINUTES,
                    rangeEnd = bounds.upperMinute,
                    stepMinutes = ScheduleViewModel.SLOT_MINUTES,
                    onChange = { v -> onIntent(ScheduleIntent.UpdateDraft(draft.copy(endMinute = v))) },
                    onBlocked = { atUpper ->
                        val reason = if (atUpper) bounds.upperReason else ErrorKey.InvalidRange
                        onIntent(ScheduleIntent.ReportBlocked(reason))
                    },
                )
            }

            SectionHeading(text = stringResource(Res.string.event_field_color))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                EventColors.forEach { c ->
                    val selected = c == draft.color
                    val borderColor = if (selected) MobileTheme.colors.text else Color.Transparent
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(c.toInt()))
                            .border(if (selected) 2.dp else 0.dp, borderColor, RoundedCornerShape(4.dp))
                            .clickable { onIntent(ScheduleIntent.UpdateDraft(draft.copy(color = c))) },
                    )
                }
            }

            SectionHeading(text = stringResource(Res.string.event_field_notes))
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                MobileTextField(
                    value = draft.notes,
                    onValueChange = { onIntent(ScheduleIntent.UpdateDraft(draft.copy(notes = it))) },
                    singleLine = false,
                    maxLines = 4,
                    modifier = Modifier.height(100.dp),
                )
            }

            if (current.mode == EventEditorState.Mode.Edit) {
                Spacer(Modifier.height(20.dp))
                EditModeActions(editor = current, onIntent = onIntent)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ScopeSection(
    dayLabel: String,
    scope: EventEditorState.Scope,
    onScopeChange: (EventEditorState.Scope) -> Unit,
) {
    SectionHeading(text = stringResource(Res.string.event_scope_label))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        ScopeChip(
            label = stringResource(Res.string.event_scope_this_day, dayLabel),
            selected = scope == EventEditorState.Scope.ThisDayOnly,
            onClick = { onScopeChange(EventEditorState.Scope.ThisDayOnly) },
            modifier = Modifier.weight(1f),
        )
        ScopeChip(
            label = stringResource(Res.string.event_scope_all_days),
            selected = scope == EventEditorState.Scope.AllLinkedDays,
            onClick = { onScopeChange(EventEditorState.Scope.AllLinkedDays) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ScopeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val bg = if (selected) colors.accentSoft else Color.Transparent
    val borderColor = if (selected) colors.accent else colors.line
    val fg = if (selected) colors.accent else colors.textSec
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = fg,
                fontSize = typography.label,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

@Composable
private fun EditModeActions(
    editor: EventEditorState,
    onIntent: (ScheduleIntent) -> Unit,
) {
    val colors = MobileTheme.colors
    val typography = MobileTheme.typography
    val original = editor.original
    val canHide = editor.templateIsShared && (
        original is EventEditorState.Original.TemplateEvent ||
            original is EventEditorState.Original.Overridden
        )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (canHide) {
            DangerOutlineButton(
                label = stringResource(Res.string.action_hide_for_day, editor.day.fullName()),
                color = colors.textSec,
                borderColor = colors.line,
                onClick = { onIntent(ScheduleIntent.HideEffectiveEvent(editor.toEffective())) },
            )
        }
        DangerOutlineButton(
            label = stringResource(Res.string.action_delete),
            color = colors.danger,
            borderColor = colors.danger.copy(alpha = 0.4f),
            onClick = { onIntent(ScheduleIntent.DeleteEditor) },
        )
    }
}

@Composable
private fun DangerOutlineButton(
    label: String,
    color: Color,
    borderColor: Color,
    onClick: () -> Unit,
) {
    val typography = MobileTheme.typography
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, borderColor, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 11.dp),
        contentAlignment = Alignment.Center,
    ) {
        BasicText(
            text = label,
            style = TextStyle(
                color = color,
                fontSize = typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
            ),
        )
    }
}

private fun EventEditorState.toEffective(): EffectiveEvent {
    val source: EffectiveEvent.Source = when (val o = original) {
        is EventEditorState.Original.TemplateEvent -> EffectiveEvent.Source.TemplateShared(o.event)
        is EventEditorState.Original.Overridden -> EffectiveEvent.Source.TemplateOverridden(o.base, o.override)
        is EventEditorState.Original.DayOnly -> EffectiveEvent.Source.DayOnly(o.event)
        null -> error("Editor without original cannot be hidden")
    }
    return EffectiveEvent(
        day = day,
        title = draft.title,
        startMinute = draft.startMinute,
        endMinute = draft.endMinute,
        color = draft.color,
        notes = draft.notes,
        source = source,
    )
}
