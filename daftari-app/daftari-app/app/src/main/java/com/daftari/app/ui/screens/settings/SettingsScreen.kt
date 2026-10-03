package com.daftari.app.ui.screens.settings

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.daftari.app.R
import com.daftari.app.ui.components.SectionCard
import com.daftari.app.viewmodel.AppViewModel
import com.daftari.app.viewmodel.NewTermOptions
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(viewModel: AppViewModel) {
    val themeMode by viewModel.settings.themeMode.collectAsState(initial = -1)
    val language by viewModel.settings.language.collectAsState(initial = "ar")
    val appLockEnabled by viewModel.settings.appLockEnabled.collectAsState(initial = false)
    val notificationsEnabled by viewModel.settings.notificationsEnabled.collectAsState(initial = false)
    val dailySummaryEnabled by viewModel.settings.dailySummaryEnabled.collectAsState(initial = false)
    var showNewTermDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val activity = LocalContext.current as? android.app.Activity
    fun changeLanguage(lang: String) {
        scope.launch {
            viewModel.settings.setLanguage(lang)
            activity?.recreate()
        }
    }
    var showPinDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        if (uri != null) scope.launch { viewModel.exportBackup(uri) }
    }
    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) scope.launch { viewModel.importBackup(uri) }
    }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.settings_title)) }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionCard {
                Text(stringResource(R.string.settings_theme), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row {
                    FilterChip(selected = themeMode == -1, onClick = { viewModel.setThemeMode(-1) }, label = { Text(stringResource(R.string.theme_system)) })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = themeMode == 0, onClick = { viewModel.setThemeMode(0) }, label = { Text(stringResource(R.string.theme_light)) })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = themeMode == 1, onClick = { viewModel.setThemeMode(1) }, label = { Text(stringResource(R.string.theme_dark)) })
                }
            }

            SectionCard {
                Text(stringResource(R.string.settings_language), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Row {
                    FilterChip(selected = language == "ar", onClick = { changeLanguage("ar") }, label = { Text(stringResource(R.string.lang_ar)) })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = language == "en", onClick = { changeLanguage("en") }, label = { Text(stringResource(R.string.lang_en)) })
                }
            }

            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.settings_notifications), Modifier.weight(1f))
                    Switch(checked = notificationsEnabled, onCheckedChange = { viewModel.setNotificationsEnabled(it) })
                }
            }

            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.settings_daily_summary))
                        Text(
                            stringResource(R.string.settings_daily_summary_desc),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(checked = dailySummaryEnabled, onCheckedChange = { viewModel.setDailySummaryEnabled(it) })
                }
            }

            SectionCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.settings_app_lock), Modifier.weight(1f))
                    Switch(
                        checked = appLockEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) showPinDialog = true else viewModel.setAppLock(false, "")
                        }
                    )
                }
            }

            SectionCard {
                Text(stringResource(R.string.settings_backup), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Button(onClick = { exportLauncher.launch("daftari_backup.json") }) {
                    Text(stringResource(R.string.settings_export))
                }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { importLauncher.launch(arrayOf("application/json")) }) {
                    Text(stringResource(R.string.settings_import))
                }
            }

            SectionCard {
                Text(stringResource(R.string.settings_new_term), style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.settings_new_term_desc), style = MaterialTheme.typography.bodySmall)
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = { showNewTermDialog = true }) {
                    Text(stringResource(R.string.settings_new_term_button))
                }
            }
        }
    }

    if (showNewTermDialog) {
        NewTermDialog(
            onDismiss = { showNewTermDialog = false },
            onConfirm = { options ->
                viewModel.startNewTerm(options)
                showNewTermDialog = false
            }
        )
    }

    if (showPinDialog) {
        var pin by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPinDialog = false },
            title = { Text(stringResource(R.string.settings_app_lock)) },
            text = {
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it.filter { c -> c.isDigit() }.take(6) },
                    label = { Text("PIN") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (pin.length >= 4) {
                        viewModel.setAppLock(true, pin)
                        showPinDialog = false
                    }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { showPinDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun NewTermDialog(
    onDismiss: () -> Unit,
    onConfirm: (NewTermOptions) -> Unit
) {
    var clearGrades by remember { mutableStateOf(true) }
    var clearExams by remember { mutableStateOf(true) }
    var clearHomework by remember { mutableStateOf(true) }
    var clearNotebookChecks by remember { mutableStateOf(true) }
    var clearGeneralBehavior by remember { mutableStateOf(true) }
    var resetSubjectBehaviorScores by remember { mutableStateOf(true) }
    var resetRequirements by remember { mutableStateOf(true) }
    var confirmStep by remember { mutableStateOf(false) }

    val anySelected = clearGrades || clearExams || clearHomework || clearNotebookChecks ||
        clearGeneralBehavior || resetSubjectBehaviorScores || resetRequirements

    if (!confirmStep) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.new_term_title)) },
            text = {
                Column {
                    Text(stringResource(R.string.new_term_desc), style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(12.dp))
                    CheckRow(stringResource(R.string.new_term_grades), clearGrades) { clearGrades = it }
                    CheckRow(stringResource(R.string.new_term_exams), clearExams) { clearExams = it }
                    CheckRow(stringResource(R.string.new_term_homework), clearHomework) { clearHomework = it }
                    CheckRow(stringResource(R.string.new_term_checks), clearNotebookChecks) { clearNotebookChecks = it }
                    CheckRow(stringResource(R.string.new_term_general_behavior), clearGeneralBehavior) { clearGeneralBehavior = it }
                    CheckRow(stringResource(R.string.new_term_subject_behavior), resetSubjectBehaviorScores) { resetSubjectBehaviorScores = it }
                    CheckRow(stringResource(R.string.new_term_requirements), resetRequirements) { resetRequirements = it }
                }
            },
            confirmButton = {
                TextButton(onClick = { if (anySelected) confirmStep = true }) {
                    Text(stringResource(R.string.next_step))
                }
            },
            dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
        )
    } else {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.new_term_confirm_title)) },
            text = { Text(stringResource(R.string.new_term_confirm_msg)) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm(
                        NewTermOptions(
                            clearGrades = clearGrades,
                            clearExams = clearExams,
                            clearHomework = clearHomework,
                            clearNotebookChecks = clearNotebookChecks,
                            clearGeneralBehavior = clearGeneralBehavior,
                            resetSubjectBehaviorScores = resetSubjectBehaviorScores,
                            resetRequirements = resetRequirements
                        )
                    )
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = { TextButton(onClick = { confirmStep = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun CheckRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(label)
    }
}
