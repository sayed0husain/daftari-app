package com.daftari.app

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingWorkPolicy
import com.daftari.app.notification.DailySummaryScheduler
import com.daftari.app.ui.components.DaftariBottomBar
import com.daftari.app.ui.navigation.Routes
import com.daftari.app.ui.screens.behavior.BehaviorScreen
import com.daftari.app.ui.screens.home.HomeScreen
import com.daftari.app.ui.screens.notes.NotesScreen
import com.daftari.app.ui.screens.onboarding.OnboardingScreen
import com.daftari.app.ui.screens.schedule.WeeklyScheduleScreen
import com.daftari.app.ui.screens.settings.LockScreen
import com.daftari.app.ui.screens.settings.SettingsScreen
import com.daftari.app.ui.screens.subjects.SubjectDetailsScreen
import com.daftari.app.ui.screens.subjects.SubjectsScreen
import com.daftari.app.ui.theme.DaftariTheme
import com.daftari.app.viewmodel.AppViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels {
        AppViewModel.factory(application)
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = runBlocking {
            (newBase.applicationContext as? DaftariApplication)?.settingsDataStore?.language?.first() ?: "ar"
        }
        super.attachBaseContext(applyLocale(newBase, lang))
    }

    private fun applyLocale(context: Context, lang: String): Context {
        val locale = java.util.Locale(lang)
        java.util.Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeMode by viewModel.settings.themeMode.collectAsState(initial = -1)
            val onboardingDone by viewModel.settings.onboardingDone.collectAsState(initial = false)
            val lockEnabled by viewModel.settings.appLockEnabled.collectAsState(initial = false)
            val lockPin by viewModel.settings.appLockPin.collectAsState(initial = "")
            var unlocked by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                if (viewModel.settings.dailySummaryEnabled.first()) {
                    DailySummaryScheduler.scheduleNext(applicationContext, ExistingWorkPolicy.KEEP)
                }
            }

            DaftariTheme(themeMode = themeMode) {
              if (lockEnabled && lockPin.isNotEmpty() && !unlocked) {
                LockScreen(correctPin = lockPin, onUnlocked = { unlocked = true })
              } else {
                val navController = rememberNavController()
                val startDestination = if (onboardingDone) Routes.HOME else Routes.ONBOARDING

                Scaffold(
                    bottomBar = {
                        if (onboardingDone) DaftariBottomBar(navController)
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier.padding(padding)
                    ) {
                        composable(Routes.ONBOARDING) {
                            OnboardingScreen(
                                viewModel = viewModel,
                                onFinished = {
                                    navController.navigate(Routes.HOME) {
                                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                                    }
                                },
                                onGoToSubjects = {
                                    navController.navigate(Routes.SUBJECTS) {
                                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable(Routes.HOME) {
                            HomeScreen(
                                viewModel = viewModel,
                                onOpenSubject = { id -> navController.navigate(Routes.subjectDetails(id)) },
                                onOpenNotes = { navController.navigate(Routes.NOTES) }
                            )
                        }
                        composable(Routes.NOTES) {
                            NotesScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
                        }
                        composable(Routes.SUBJECTS) {
                            SubjectsScreen(viewModel = viewModel, onOpenSubject = { id ->
                                navController.navigate(Routes.subjectDetails(id))
                            })
                        }
                        composable(Routes.SUBJECT_DETAILS) { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("subjectId")?.toLongOrNull() ?: 0L
                            SubjectDetailsScreen(
                                subjectId = id,
                                viewModel = viewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable(Routes.SCHEDULE) {
                            WeeklyScheduleScreen(viewModel = viewModel)
                        }
                        composable(Routes.BEHAVIOR) {
                            BehaviorScreen(viewModel = viewModel)
                        }
                        composable(Routes.SETTINGS) {
                            SettingsScreen(viewModel = viewModel)
                        }
                    }
                }
              }
            }
        }
    }
}
