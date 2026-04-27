package dev.nucleus.scheduleit.ui.material3

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.nucleus.scheduleit.domain.ScheduleEvent
import dev.nucleus.scheduleit.domain.ScheduleSettings
import dev.nucleus.scheduleit.presentation.schedule.ErrorKey
import dev.nucleus.scheduleit.presentation.schedule.EventEditorState
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel
import dev.nucleus.scheduleit.presentation.schedule.computeEditorBounds
import org.jetbrains.compose.resources.stringResource
import scheduleit.shared.generated.resources.Res
import scheduleit.shared.generated.resources.action_cancel
import scheduleit.shared.generated.resources.action_delete
import scheduleit.shared.generated.resources.action_save
import scheduleit.shared.generated.resources.event_dialog_edit_title
import scheduleit.shared.generated.resources.event_dialog_new_title
import scheduleit.shared.generated.resources.event_field_color
import scheduleit.shared.generated.resources.event_field_end
import scheduleit.shared.generated.resources.event_field_notes
import scheduleit.shared.generated.resources.event_field_start
import scheduleit.shared.generated.resources.event_field_title

private val EVENT_COLORS = listOf(
    0xFF42A5F5L, 0xFFEF5350L, 0xFF66BB6AL, 0xFFFFCA28L,
    0xFFAB47BCL, 0xFFFF7043L, 0xFF26C6DAL, 0xFF8D6E63L,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialEventEditor(
    editor: EventEditorState,
    settings: ScheduleSettings,
    siblings: List<ScheduleEvent>,
    onIntent: (ScheduleIntent) -> Unit,
) {
    val draft = editor.draft
    val bounds = computeEditorBounds(draft, siblings, settings)
    AlertDialog(
        onDismissRequest = { onIntent(ScheduleIntent.DismissEditor) },
        title = {
            Text(
                stringResource(
                    if (editor.mode == EventEditorState.Mode.Create) {
                        Res.string.event_dialog_new_title
                    } else {
                        Res.string.event_dialog_edit_title
                    },
                ),
            )
        },
        text = {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                MaterialStableTextField(
                    value = draft.title,
                    onValueChange = { onIntent(ScheduleIntent.UpdateDraft(draft.copy(title = it))) },
                    label = stringResource(Res.string.event_field_title),
                    singleLine = true,
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp, androidx.compose.ui.Alignment.CenterHorizontally),
                ) {
                    MaterialTimePicker(
                        label = stringResource(Res.string.event_field_start),
                        valueMinute = draft.startMinute,
                        rangeStart = bounds.lowerMinute,
                        rangeEnd = draft.endMinute - ScheduleViewModel.SLOT_MINUTES,
                        stepMinutes = ScheduleViewModel.SLOT_MINUTES,
                        onChange = { v ->
                            onIntent(ScheduleIntent.UpdateDraft(draft.copy(startMinute = v)))
                        },
                        onBlocked = { atUpper ->
                            val reason = if (atUpper) ErrorKey.InvalidRange else bounds.lowerReason
                            onIntent(ScheduleIntent.ReportBlocked(reason))
                        },
                    )
                    MaterialTimePicker(
                        label = stringResource(Res.string.event_field_end),
                        valueMinute = draft.endMinute,
                        rangeStart = draft.startMinute + ScheduleViewModel.SLOT_MINUTES,
                        rangeEnd = bounds.upperMinute,
                        stepMinutes = ScheduleViewModel.SLOT_MINUTES,
                        onChange = { v ->
                            onIntent(ScheduleIntent.UpdateDraft(draft.copy(endMinute = v)))
                        },
                        onBlocked = { atUpper ->
                            val reason = if (atUpper) bounds.upperReason else ErrorKey.InvalidRange
                            onIntent(ScheduleIntent.ReportBlocked(reason))
                        },
                    )
                }

                Text(stringResource(Res.string.event_field_color), style = MaterialTheme.typography.labelMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.CenterHorizontally),
                ) {
                    EVENT_COLORS.forEach { color ->
                        val selected = color == draft.color
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(color.toInt()))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape,
                                )
                                .clickable { onIntent(ScheduleIntent.UpdateDraft(draft.copy(color = color))) },
                        )
                    }
                }

                MaterialStableTextField(
                    value = draft.notes,
                    onValueChange = { onIntent(ScheduleIntent.UpdateDraft(draft.copy(notes = it))) },
                    label = stringResource(Res.string.event_field_notes),
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth().height(96.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onIntent(ScheduleIntent.SaveEditor) }) {
                Text(stringResource(Res.string.action_save))
            }
        },
        dismissButton = {
            Row {
                if (editor.mode == EventEditorState.Mode.Edit) {
                    TextButton(onClick = { onIntent(ScheduleIntent.DeleteEditor) }) {
                        Text(stringResource(Res.string.action_delete))
                    }
                }
                TextButton(onClick = { onIntent(ScheduleIntent.DismissEditor) }) {
                    Text(stringResource(Res.string.action_cancel))
                }
            }
        },
    )
}
