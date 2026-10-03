package com.daftari.app.ui.screens.onboarding

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.daftari.app.R
import com.daftari.app.viewmodel.AppViewModel

private enum class OnboardingStep { INTRO, NOTIFICATIONS, ADD_NOW }

@Composable
fun OnboardingScreen(
    viewModel: AppViewModel,
    onFinished: () -> Unit,
    onGoToSubjects: () -> Unit
) {
    var step by remember { mutableStateOf(OnboardingStep.INTRO) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        viewModel.setNotificationsEnabled(granted)
        step = OnboardingStep.ADD_NOW
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        when (step) {
            OnboardingStep.INTRO -> {
                Text(
                    stringResource(R.string.onboarding_title),
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.onboarding_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
                Button(onClick = { step = OnboardingStep.NOTIFICATIONS }) {
                    Text(stringResource(R.string.onboarding_next))
                }
            }

            OnboardingStep.NOTIFICATIONS -> {
                Text(
                    stringResource(R.string.onboarding_notif_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.onboarding_notif_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
                Button(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.setNotificationsEnabled(true)
                        step = OnboardingStep.ADD_NOW
                    }
                }) {
                    Text(stringResource(R.string.onboarding_enable_notifications))
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { step = OnboardingStep.ADD_NOW }) {
                    Text(stringResource(R.string.onboarding_skip))
                }
            }

            OnboardingStep.ADD_NOW -> {
                Text(
                    stringResource(R.string.onboarding_add_now_title),
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    stringResource(R.string.onboarding_add_now_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
                Button(onClick = {
                    viewModel.setOnboardingDone(true)
                    onGoToSubjects()
                }) {
                    Text(stringResource(R.string.onboarding_add_now))
                }
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = {
                    viewModel.setOnboardingDone(true)
                    onFinished()
                }) {
                    Text(stringResource(R.string.onboarding_later))
                }
            }
        }
    }
}
