package dev.nucleus.scheduleit.ui.jewel

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.rememberDialogState
import androidx.compose.foundation.text.input.rememberTextFieldState
import io.github.kdroidfilter.nucleus.window.jewel.JewelDecoratedDialog
import io.github.kdroidfilter.nucleus.window.jewel.JewelDialogTitleBar
import dev.nucleus.scheduleit.domain.ScheduleEvent
import dev.nucleus.scheduleit.domain.ScheduleSettings
import dev.nucleus.scheduleit.presentation.schedule.EventEditorState
import dev.nucleus.scheduleit.presentation.schedule.ScheduleIntent
import dev.nucleus.scheduleit.presentation.schedule.ScheduleViewModel
import dev.nucleus.scheduleit.presentation.schedule.computeEditorBounds
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.jewel.foundation.theme.JewelTheme
import org.jetbrains.jewel.ui.component.DefaultButton
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text
import org.jetbrains.jewel.ui.component.TextArea
import org.jetbrains.jewel.ui.component.TextField
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

@Composable
fun JewelEventEditor(
    editor: EventEditorState,
    settings: ScheduleSettings,
    siblings: List<ScheduleEvent>,
    onIntent: (ScheduleIntent) -> Unit,
) {
    val draft = editor.draft
    val bounds = computeEditorBounds(draft, siblings, settings)
    val state = rememberDialogState(size = DpSize(460.dp, 560.dp))
    val dialogTitle = stringResource(
        if (editor.mode == EventEditorState.Mode.Create) Res.string.event_dialog_new_title
        else Res.string.event_dialog_edit_title,
    )
    JewelDecoratedDialog(
        onCloseRequest = { onIntent(ScheduleIntent.DismissEditor) },
        state = state,
        title = dialogTitle,
    ) {
        JewelDialogTitleBar { _ -> Text(dialogTitle) }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(JewelTheme.globalColors.panelBackground)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stringResource(Res.string.event_field_title), color = JewelTheme.globalColors.text.info)
            TitleTextField(
                value = draft.title,
                editorKey = editor.draft.id to editor.mode,
                onValueChange = { onIntent(ScheduleIntent.UpdateDraft(draft.copy(title = it))) },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, androidx.compose.ui.Alignment.CenterHorizontally),
            ) {
                JewelTimePicker(
                    label = stringResource(Res.string.event_field_start),
                    valueMinute = draft.startMinute,
                    rangeStart = bounds.lowerMinute,
                    rangeEnd = draft.endMinute - ScheduleViewModel.SLOT_MINUTES,
                    stepMinutes = ScheduleViewModel.SLOT_MINUTES,
                    onChange = { v ->
                        onIntent(ScheduleIntent.UpdateDraft(draft.copy(startMinute = v)))
                    },
                )
                JewelTimePicker(
                    label = stringResource(Res.string.event_field_end),
                    valueMinute = draft.endMinute,
                    rangeStart = draft.startMinute + ScheduleViewModel.SLOT_MINUTES,
                    rangeEnd = bounds.upperMinute,
                    stepMinutes = ScheduleViewModel.SLOT_MINUTES,
                    onChange = { v ->
                        onIntent(ScheduleIntent.UpdateDraft(draft.copy(endMinute = v)))
                    },
                )
            }

            Text(stringResource(Res.string.event_field_color), color = JewelTheme.globalColors.text.info)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, androidx.compose.ui.Alignment.CenterHorizontally),
            ) {
                EVENT_COLORS.forEach { color ->
                    val selected = color == draft.color
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(color.toInt()))
                            .border(
                                width = if (selected) 3.dp else 1.dp,
                                color = if (selected) JewelTheme.globalColors.outlines.focused else Color.Transparent,
                                shape = CircleShape,
                            )
                            .clickable { onIntent(ScheduleIntent.UpdateDraft(draft.copy(color = color))) },
                    )
                }
            }

            Text(stringResource(Res.string.event_field_notes), color = JewelTheme.globalColors.text.info)
            NotesTextArea(
                value = draft.notes,
                editorKey = editor.draft.id to editor.mode,
                onValueChange = { onIntent(ScheduleIntent.UpdateDraft(draft.copy(notes = it))) },
            )

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, alignment = androidx.compose.ui.Alignment.End),
            ) {
                if (editor.mode == EventEditorState.Mode.Edit) {
                    OutlinedButton(onClick = { onIntent(ScheduleIntent.DeleteEditor) }) {
                        Text(stringResource(Res.string.action_delete))
                    }
                }
                OutlinedButton(onClick = { onIntent(ScheduleIntent.DismissEditor) }) {
                    Text(stringResource(Res.string.action_cancel))
                }
                DefaultButton(onClick = { onIntent(ScheduleIntent.SaveEditor) }) {
                    Text(stringResource(Res.string.action_save))
                }
            }
        }
    }
}

@Composable
private fun TitleTextField(
    value: String,
    editorKey: Any,
    onValueChange: (String) -> Unit,
) {
    val state = rememberTextFieldState(initialText = value)
    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    LaunchedEffect(editorKey) {
        snapshotFlow { state.text.toString() }.collect { text ->
            if (text != currentValue) currentOnValueChange(text)
        }
    }
    TextField(
        state = state,
        modifier = Modifier.fillMaxWidth().widthIn(max = 480.dp),
    )
}

@Composable
private fun NotesTextArea(
    value: String,
    editorKey: Any,
    onValueChange: (String) -> Unit,
) {
    val state = rememberTextFieldState(initialText = value)
    val currentValue by rememberUpdatedState(value)
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    LaunchedEffect(editorKey) {
        snapshotFlow { state.text.toString() }.collect { text ->
            if (text != currentValue) currentOnValueChange(text)
        }
    }
    TextArea(
        state = state,
        modifier = Modifier.fillMaxWidth().height(96.dp),
    )
}
