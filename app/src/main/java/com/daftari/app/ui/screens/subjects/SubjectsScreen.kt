package com.daftari.app.ui.screens.subjects

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daftari.app.R
import com.daftari.app.ui.components.ColorPickerRow
import com.daftari.app.ui.theme.SubjectColorPalette
import com.daftari.app.viewmodel.AppViewModel

@Composable
fun SubjectsScreen(viewModel: AppViewModel, onOpenSubject: (Long) -> Unit) {
    val subjects by viewModel.subjects.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.subjects_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_subject))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(subjects) { subject ->
                val color = runCatching { Color(AndroidColor.parseColor(subject.colorHex)) }.getOrDefault(Color.Gray)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSubject(subject.id) }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(subject.name, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddSubjectDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, colorHex, weeklyHours ->
                viewModel.addSubject(name, colorHex, weeklyHours)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddSubjectDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var color by remember { mutableStateOf(SubjectColorPalette.first()) }
    var weeklyHours by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_subject)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.subject_name)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.subject_color))
                ColorPickerRow(selectedHex = color, onColorSelected = { color = it })
                OutlinedTextField(
                    value = weeklyHours,
                    onValueChange = { weeklyHours = it.filter { c -> c.isDigit() } },
                    label = { Text(stringResource(R.string.subject_weekly_hours)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(name, color, weeklyHours.toIntOrNull() ?: 0)
                    }
                }
            ) { Text(stringResource(R.string.save)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    )
}
