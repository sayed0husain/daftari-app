package com.daftari.app.ui.navigation

object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val SUBJECTS = "subjects"
    const val SUBJECT_DETAILS = "subject_details/{subjectId}"
    const val SCHEDULE = "schedule"
    const val BEHAVIOR = "behavior"
    const val SETTINGS = "settings"

    fun subjectDetails(subjectId: Long) = "subject_details/$subjectId"
}
