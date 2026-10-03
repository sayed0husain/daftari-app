package com.daftari.app.ui.screens.behavior

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daftari.app.R
import com.daftari.app.viewmodel.AppViewModel

@Composable
fun BehaviorScreen(viewModel: AppViewModel) {
    val generalGrades by viewModel.behaviorGrades.collectAsState()
    val subjects by viewModel.subjects.collectAsState()
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.behavior_title)) }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_behavior_period))
            }
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            item {
                Text(stringResource(R.string.behavior_general), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
            items(generalGrades) { grade ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${grade.periodName}: ${grade.score} / ${grade.scoreTotal}")
                        IconButton(onClick = { viewModel.deleteBehaviorGrade(grade) }) {
                            Icon(Icons.Filled.Delete, contentDescription = null)
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(stringResource(R.string.behavior_per_subject), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }
            items(subjects) { subject ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(subject.name)
                        Text("${subject.subjectBehaviorScore} / 40")
                    }
                }
            }
        }
    }

    if (showAdd) {
        var periodName by remember { mutableStateOf("") }
        var score by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAdd = false },
            title = { Text(stringResource(R.string.add_behavior_period)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = periodName, onValueChange = { periodName = it }, label = { Text(stringResource(R.string.behavior_period_name)) })
                    OutlinedTextField(
                        value = score,
                        onValueChange = { score = it.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.behavior_score)) }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val s = score.toIntOrNull()
                    if (periodName.isNotBlank() && s != null) {
                        viewModel.addBehaviorGrade(periodName, s.coerceIn(0, 40))
                        showAdd = false
                    }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
