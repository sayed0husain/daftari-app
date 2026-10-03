package com.daftari.app.ui.screens.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daftari.app.R
import com.daftari.app.ui.components.ConfirmDialog
import com.daftari.app.ui.components.rememberTimePickerLauncher
import com.daftari.app.viewmodel.AppViewModel

private val dayLabelResIds = listOf(
    R.string.days_sunday, R.string.days_monday, R.string.days_tuesday,
    R.string.days_wednesday, R.string.days_thursday, R.string.days_friday, R.string.days_saturday
)

@Composable
fun WeeklyScheduleScreen(viewModel: AppViewModel) {
    val slots by viewModel.scheduleSlots.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    fun subjectName(id: Long) = subjects.find { it.id == id }?.name ?: ""

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.schedule_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_schedule_slot))
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            for (day in 0..6) {
                val daySlots = slots.filter { it.dayOfWeek == day }.sortedBy { it.startTime }
                if (daySlots.isNotEmpty()) {
                    item {
                        Text(
                            stringResource(dayLabelResIds[day]),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(daySlots) { slot ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            Row(
                                Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(subjectName(slot.subjectId), style = MaterialTheme.typography.bodyLarge)
                                    Text("${slot.startTime} - ${slot.endTime}", style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { viewModel.deleteScheduleSlot(slot) }) {
                                    Icon(Icons.Filled.Delete, contentDescription = null)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AddSlotDialog(
            subjects = subjects,
            existingSlots = slots,
            viewModel = viewModel,
            onDismiss = { showAdd = false }
        )
    }
}

@Composable
private fun AddSlotDialog(
    subjects: List<com.daftari.app.data.entity.Subject>,
    existingSlots: List<com.daftari.app.data.entity.ScheduleSlot>,
    viewModel: AppViewModel,
    onDismiss: () -> Unit
) {
    var selectedSubjectId by remember { mutableStateOf(subjects.firstOrNull()?.id ?: 0L) }
    var day by remember { mutableStateOf(0) }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("09:00") }
    var expandedSubject by remember { mutableStateOf(false) }
    var expandedDay by remember { mutableStateOf(false) }
    var pendingConflict by remember { mutableStateOf(false) }

    val pickStart = rememberTimePickerLauncher { startTime = it }
    val pickEnd = rememberTimePickerLauncher { endTime = it }

    fun trySave() {
        if (viewModel.hasConflict(day, startTime, endTime, existingSlots)) {
            pendingConflict = true
        } else {
            viewModel.addScheduleSlot(selectedSubjectId, day, startTime, endTime)
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_schedule_slot)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExposedDropdownMenuBox(expanded = expandedSubject, onExpandedChange = { expandedSubject = it }) {
                    OutlinedTextField(
                        value = subjects.find { it.id == selectedSubjectId }?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.subject_name)) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedSubject, onDismissRequest = { expandedSubject = false }) {
                        subjects.forEach { s ->
                            DropdownMenuItem(text = { Text(s.name) }, onClick = {
                                selectedSubjectId = s.id
                                expandedSubject = false
                            })
                        }
                    }
                }
                ExposedDropdownMenuBox(expanded = expandedDay, onExpandedChange = { expandedDay = it }) {
                    OutlinedTextField(
                        value = stringResource(dayLabelResIds[day]),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.schedule_day)) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedDay, onDismissRequest = { expandedDay = false }) {
                        dayLabelResIds.forEachIndexed { index, resId ->
                            DropdownMenuItem(text = { Text(stringResource(resId)) }, onClick = {
                                day = index
                                expandedDay = false
                            })
                        }
                    }
                }
                OutlinedButton(onClick = pickStart) { Text("${stringResource(R.string.schedule_start)}: $startTime") }
                OutlinedButton(onClick = pickEnd) { Text("${stringResource(R.string.schedule_end)}: $endTime") }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (subjects.isNotEmpty()) trySave() }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )

    if (pendingConflict) {
        ConfirmDialog(
            title = stringResource(R.string.schedule_conflict_title),
            message = stringResource(R.string.schedule_conflict_msg),
            confirmLabel = stringResource(R.string.confirm),
            cancelLabel = stringResource(R.string.cancel),
            onConfirm = {
                pendingConflict = false
                viewModel.addScheduleSlot(selectedSubjectId, day, startTime, endTime)
                onDismiss()
            },
            onDismiss = { pendingConflict = false }
        )
    }
}
