package com.daftari.app.ui.screens.subjects

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daftari.app.R
import com.daftari.app.data.entity.AttendanceStatus
import com.daftari.app.data.entity.Exam
import com.daftari.app.ui.components.ConfirmDialog
import com.daftari.app.ui.components.rememberDatePickerLauncher
import com.daftari.app.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

private enum class SubjectTab { GRADES, EXAMS, HOMEWORK, REQUIREMENTS, ATTENDANCE }

@Composable
fun SubjectDetailsScreen(subjectId: Long, viewModel: AppViewModel, onBack: () -> Unit) {
    val subject by viewModel.repository.getSubject(subjectId).collectAsState(initial = null)
    val grades by viewModel.repository.getGrades(subjectId).collectAsState(initial = emptyList())
    val exams by viewModel.repository.getExams(subjectId).collectAsState(initial = emptyList())
    val homework by viewModel.repository.getHomework(subjectId).collectAsState(initial = emptyList())
    val attendance by viewModel.repository.getAttendance(subjectId).collectAsState(initial = emptyList())

    var tab by remember { mutableStateOf(SubjectTab.GRADES) }
    val average = viewModel.subjectAverage(grades)
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(subject?.name ?: stringResource(R.string.subject_details_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = null) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (average != null) {
                Text(
                    "${stringResource(R.string.average_label)}: ${"%.1f".format(average)}%",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            ScrollableTabRow(selectedTabIndex = tab.ordinal) {
                Tab(selected = tab == SubjectTab.GRADES, onClick = { tab = SubjectTab.GRADES },
                    text = { Text(stringResource(R.string.tab_grades)) })
                Tab(selected = tab == SubjectTab.EXAMS, onClick = { tab = SubjectTab.EXAMS },
                    text = { Text(stringResource(R.string.tab_exams)) })
                Tab(selected = tab == SubjectTab.HOMEWORK, onClick = { tab = SubjectTab.HOMEWORK },
                    text = { Text(stringResource(R.string.tab_homework)) })
                Tab(selected = tab == SubjectTab.REQUIREMENTS, onClick = { tab = SubjectTab.REQUIREMENTS },
                    text = { Text(stringResource(R.string.tab_requirements)) })
                Tab(selected = tab == SubjectTab.ATTENDANCE, onClick = { tab = SubjectTab.ATTENDANCE },
                    text = { Text(stringResource(R.string.tab_attendance)) })
            }

            when (tab) {
                SubjectTab.GRADES -> GradesTab(subjectId, grades, viewModel)
                SubjectTab.EXAMS -> ExamsTab(subjectId, exams, viewModel, dateFormat)
                SubjectTab.HOMEWORK -> HomeworkTab(subjectId, homework, viewModel, dateFormat)
                SubjectTab.REQUIREMENTS -> subject?.let { RequirementsTab(it, viewModel) }
                SubjectTab.ATTENDANCE -> AttendanceTab(subjectId, attendance, viewModel)
            }
        }
    }
}

@Composable
private fun GradesTab(subjectId: Long, grades: List<com.daftari.app.data.entity.Grade>, viewModel: AppViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { showAdd = true }) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.add_grade))
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(grades) { grade ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${grade.name}: ${grade.scoreObtained.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }} / ${grade.scoreTotal.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() }}")
                        IconButton(onClick = { viewModel.deleteGrade(grade) }) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var name by remember { mutableStateOf("") }
        var obtained by remember { mutableStateOf("") }
        var total by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text(stringResource(R.string.add_grade)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.grade_name)) })
                    OutlinedTextField(value = obtained, onValueChange = { obtained = it }, label = { Text(stringResource(R.string.grade_obtained)) })
                    OutlinedTextField(value = total, onValueChange = { total = it }, label = { Text(stringResource(R.string.grade_total)) })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val o = obtained.toDoubleOrNull()
                    val t = total.toDoubleOrNull()
                    if (name.isNotBlank() && o != null && t != null && t > 0) {
                        viewModel.addGrade(subjectId, name, o, t)
                        showAdd = false
                    }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun ExamsTab(
    subjectId: Long,
    exams: List<Exam>,
    viewModel: AppViewModel,
    dateFormat: SimpleDateFormat
) {
    var showAdd by remember { mutableStateOf(false) }
    var editingExam by remember { mutableStateOf<Exam?>(null) }
    var pendingLimitConfirm by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = {
            scope.launch {
                if (viewModel.wouldExceedExamLimit(subjectId)) {
                    pendingLimitConfirm = true
                } else {
                    showAdd = true
                }
            }
        }) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.add_exam))
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(exams) { exam ->
                Card(Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(exam.name, style = MaterialTheme.typography.bodyLarge)
                            Text(dateFormat.format(exam.date), style = MaterialTheme.typography.bodySmall)
                        }
                        Row {
                            IconButton(onClick = { editingExam = exam }) {
                                Icon(Icons.Filled.Edit, contentDescription = null)
                            }
                            IconButton(onClick = { viewModel.deleteExam(exam) }) {
                                Icon(Icons.Filled.Delete, contentDescription = null)
                            }
                        }
                    }
                }
            }
        }
    }

    if (pendingLimitConfirm) {
        ConfirmDialog(
            title = stringResource(R.string.exam_limit_reached_title),
            message = stringResource(R.string.exam_limit_reached_msg),
            confirmLabel = stringResource(R.string.confirm),
            cancelLabel = stringResource(R.string.cancel),
            onConfirm = { pendingLimitConfirm = false; showAdd = true },
            onDismiss = { pendingLimitConfirm = false }
        )
    }

    if (showAdd || editingExam != null) {
        ExamFormDialog(
            existing = editingExam,
            onDismiss = { showAdd = false; editingExam = null },
            onSave = { name, date, notes, reminderEnabled, offsetMinutes ->
                val current = editingExam
                if (current != null) {
                    viewModel.updateExam(
                        current.copy(
                            name = name, date = date, notes = notes,
                            reminderEnabled = reminderEnabled, reminderOffsetMinutes = offsetMinutes
                        )
                    )
                } else {
                    viewModel.addExam(subjectId, name, date, notes, reminderEnabled, offsetMinutes)
                }
                showAdd = false
                editingExam = null
            }
        )
    }
}

@Composable
private fun ExamFormDialog(
    existing: Exam?,
    onDismiss: () -> Unit,
    onSave: (String, Long, String, Boolean, Long) -> Unit
) {
    var name by remember { mutableStateOf(existing?.name ?: "") }
    var date by remember { mutableStateOf(existing?.date ?: System.currentTimeMillis()) }
    var notes by remember { mutableStateOf(existing?.notes ?: "") }
    var reminderEnabled by remember { mutableStateOf(existing?.reminderEnabled ?: false) }
    var reminderChoice by remember { mutableStateOf(if ((existing?.reminderOffsetMinutes ?: 1440L) == 2880L) 1 else if ((existing?.reminderOffsetMinutes ?: 1440L) == 1440L) 0 else 2) }
    var customHours by remember { mutableStateOf(((existing?.reminderOffsetMinutes ?: 60L) / 60).toString()) }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val pickDate = rememberDatePickerLauncher(date) { date = it }

    val offsetMinutes = when (reminderChoice) {
        0 -> 1440L
        1 -> 2880L
        else -> (customHours.toLongOrNull() ?: 1L) * 60L
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(if (existing != null) R.string.edit_exam else R.string.add_exam)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.exam_name)) })
                OutlinedButton(onClick = pickDate) { Text("${stringResource(R.string.exam_date)}: ${dateFormat.format(date)}") }
                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text(stringResource(R.string.exam_notes)) })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                    Text(stringResource(R.string.exam_reminder))
                }
                if (reminderEnabled) {
                    Text(stringResource(R.string.exam_reminder_before))
                    Row {
                        FilterChip(selected = reminderChoice == 0, onClick = { reminderChoice = 0 }, label = { Text(stringResource(R.string.reminder_day)) })
                        Spacer(Modifier.width(4.dp))
                        FilterChip(selected = reminderChoice == 1, onClick = { reminderChoice = 1 }, label = { Text(stringResource(R.string.reminder_two_days)) })
                        Spacer(Modifier.width(4.dp))
                        FilterChip(selected = reminderChoice == 2, onClick = { reminderChoice = 2 }, label = { Text(stringResource(R.string.reminder_hours)) })
                    }
                    if (reminderChoice == 2) {
                        OutlinedTextField(
                            value = customHours,
                            onValueChange = { customHours = it.filter { c -> c.isDigit() } },
                            label = { Text(stringResource(R.string.reminder_hours_value)) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onSave(name, date, notes, reminderEnabled, offsetMinutes)
            }) { Text(stringResource(R.string.save)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@Composable
private fun HomeworkTab(
    subjectId: Long,
    homework: List<com.daftari.app.data.entity.Homework>,
    viewModel: AppViewModel,
    dateFormat: SimpleDateFormat
) {
    var showAdd by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Button(onClick = { showAdd = true }) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(4.dp))
            Text(stringResource(R.string.add_homework))
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(homework) { hw ->
                Card(Modifier.fillMaxWidth()) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(hw.description)
                            Text(dateFormat.format(hw.dueDate), style = MaterialTheme.typography.bodySmall)
                            Text(
                                stringResource(if (hw.completed) R.string.homework_done else R.string.homework_not_done),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Checkbox(checked = hw.completed, onCheckedChange = { viewModel.toggleHomeworkDone(hw) })
                        IconButton(onClick = { viewModel.deleteHomework(hw) }) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        var description by remember { mutableStateOf("") }
        var due by remember { mutableStateOf(System.currentTimeMillis()) }
        var reminderEnabled by remember { mutableStateOf(false) }
        val pickDate = rememberDatePickerLauncher(due) { due = it }

        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text(stringResource(R.string.add_homework)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text(stringResource(R.string.homework_desc)) })
                    OutlinedButton(onClick = pickDate) { Text("${stringResource(R.string.homework_due)}: ${dateFormat.format(due)}") }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
                        Text(stringResource(R.string.exam_reminder))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (description.isNotBlank()) {
                        viewModel.addHomework(subjectId, description, due, reminderEnabled, 1440L)
                        showAdd = false
                    }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun RequirementsTab(subject: com.daftari.app.data.entity.Subject, viewModel: AppViewModel) {
    var notebookComplete by remember(subject.id) { mutableStateOf(subject.notebookComplete) }
    var copybookComplete by remember(subject.id) { mutableStateOf(subject.copybookComplete) }
    var missingInput by remember(subject.id) { mutableStateOf(subject.copybookMissingPagesInput) }
    var requiredInput by remember(subject.id) { mutableStateOf(subject.copybookRequiredPagesInput) }
    var behaviorScore by remember(subject.id) { mutableStateOf(subject.subjectBehaviorScore.toString()) }
    val missingCount = viewModel.missingPagesCount(missingInput)

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.notebook_status), Modifier.weight(1f))
            Switch(checked = notebookComplete, onCheckedChange = { notebookComplete = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.copybook_status), Modifier.weight(1f))
            Switch(checked = copybookComplete, onCheckedChange = { copybookComplete = it })
        }
        if (!copybookComplete) {
            OutlinedTextField(
                value = missingInput,
                onValueChange = { missingInput = it },
                label = { Text(stringResource(R.string.copybook_pages_input)) },
                modifier = Modifier.fillMaxWidth()
            )
            Text(stringResource(R.string.copybook_missing_count, missingCount))
            OutlinedTextField(
                value = requiredInput,
                onValueChange = { requiredInput = it },
                label = { Text(stringResource(R.string.copybook_required_pages)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedTextField(
            value = behaviorScore,
            onValueChange = { behaviorScore = it.filter { c -> c.isDigit() } },
            label = { Text(stringResource(R.string.subject_behavior_score)) },
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
            viewModel.updateRequirements(
                subject, notebookComplete, copybookComplete,
                missingInput, requiredInput,
                (behaviorScore.toIntOrNull() ?: 40).coerceIn(0, 40)
            )
        }) { Text(stringResource(R.string.save)) }
    }
}

@Composable
private fun AttendanceTab(
    subjectId: Long,
    attendance: List<com.daftari.app.data.entity.Attendance>,
    viewModel: AppViewModel
) {
    val rate = viewModel.attendanceRate(attendance)
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("${stringResource(R.string.attendance_rate)}: ${"%.0f".format(rate)}%", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { viewModel.addAttendance(subjectId, AttendanceStatus.PRESENT) }) {
                Text(stringResource(R.string.attendance_present))
            }
            Button(onClick = { viewModel.addAttendance(subjectId, AttendanceStatus.LATE) }) {
                Text(stringResource(R.string.attendance_late))
            }
            Button(onClick = { viewModel.addAttendance(subjectId, AttendanceStatus.ABSENT) }) {
                Text(stringResource(R.string.attendance_absent))
            }
        }
    }
}
