package com.daftari.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daftari.app.R
import com.daftari.app.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(viewModel: AppViewModel, onOpenSubject: (Long) -> Unit, onOpenNotes: () -> Unit) {
    val exams by viewModel.allExams.collectAsState()
    val homework by viewModel.allHomework.collectAsState()
    val checks by viewModel.allNotebookChecks.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    val now = System.currentTimeMillis()
    val upcomingExams = exams.filter { it.date >= now }.sortedBy { it.date }
    val pendingHomework = homework.filter { !it.completed }.sortedBy { it.dueDate }
    val upcomingChecks = checks.filter { it.date >= now }.sortedBy { it.date }
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    fun subjectName(id: Long) = subjects.find { it.id == id }?.name ?: ""

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_welcome)) },
                actions = {
                    IconButton(onClick = onOpenNotes) {
                        Icon(Icons.Filled.StickyNote2, contentDescription = stringResource(R.string.notes_title))
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { SectionHeader(stringResource(R.string.home_upcoming_exams)) }
            if (upcomingExams.isEmpty()) {
                item { EmptyRow() }
            } else {
                items(upcomingExams) { exam ->
                    InfoRow(title = "${subjectName(exam.subjectId)} — ${exam.name}", subtitle = dateFormat.format(exam.date))
                }
            }

            item { SectionHeader(stringResource(R.string.home_pending_homework)) }
            if (pendingHomework.isEmpty()) {
                item { EmptyRow() }
            } else {
                items(pendingHomework) { hw ->
                    InfoRow(title = "${subjectName(hw.subjectId)} — ${hw.description}", subtitle = dateFormat.format(hw.dueDate))
                }
            }

            item { SectionHeader(stringResource(R.string.home_upcoming_checks)) }
            if (upcomingChecks.isEmpty()) {
                item { EmptyRow() }
            } else {
                items(upcomingChecks) { check ->
                    InfoRow(title = "${subjectName(check.subjectId)} — ${check.description}", subtitle = dateFormat.format(check.date))
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
}

@Composable
private fun EmptyRow() {
    Text(stringResource(R.string.home_no_data), style = MaterialTheme.typography.bodyMedium)
}

@Composable
private fun InfoRow(title: String, subtitle: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
