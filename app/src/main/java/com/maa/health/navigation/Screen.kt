package com.maa.health.navigation

/**
 * Navigation destinations for Maa app
 *
 * Organized by feature modules with consistent naming
 */
sealed class Screen(val route: String) {

    // ═══════════════════════════════════════════════════════════════════════════
    // ONBOARDING
    // ═══════════════════════════════════════════════════════════════════════════

    data object Splash : Screen("splash")
    data object LanguageSelection : Screen("onboarding/language")
    data object PhoneAuth : Screen("onboarding/phone_auth")
    data object OtpVerification : Screen("onboarding/otp/{phoneNumber}") {
        fun createRoute(phoneNumber: String) = "onboarding/otp/$phoneNumber"
    }
    data object BiometricSetup : Screen("onboarding/biometric")
    data object ProfileSetup : Screen("onboarding/profile")
    data object LifecycleStageSelection : Screen("onboarding/lifecycle_stage")
    data object OnboardingComplete : Screen("onboarding/complete")

    // ═══════════════════════════════════════════════════════════════════════════
    // MAIN NAVIGATION (Bottom Nav Tabs)
    // ═══════════════════════════════════════════════════════════════════════════

    data object Home : Screen("home")
    data object TrackHub : Screen("track")        // Track tab hub
    data object LearnHub : Screen("learn")        // Learn tab hub
    data object CareHub : Screen("care")          // Care tab hub
    data object YouHub : Screen("you")            // You/Profile tab hub

    // Legacy routes (kept for compatibility)
    data object Insights : Screen("insights")
    data object History : Screen("history")
    data object Settings : Screen("settings")

    // ═══════════════════════════════════════════════════════════════════════════
    // BODY MAP & SYMPTOM TRIAGE
    // ═══════════════════════════════════════════════════════════════════════════

    data object BodyMap : Screen("body_map")
    data object SymptomEntry : Screen("symptom/{region}") {
        fun createRoute(region: String) = "symptom/$region"
    }
    data object SymptomDetails : Screen("symptom/{region}/details") {
        fun createRoute(region: String) = "symptom/$region/details"
    }
    data object TriageResult : Screen("triage/result/{symptomLogId}") {
        fun createRoute(symptomLogId: String) = "triage/result/$symptomLogId"
    }
    data object TriageHistory : Screen("triage/history")

    // ═══════════════════════════════════════════════════════════════════════════
    // PREGNANCY MODULE
    // ═══════════════════════════════════════════════════════════════════════════

    data object PregnancyHome : Screen("pregnancy")
    data object PregnancySetup : Screen("pregnancy/setup")
    data object WeekByWeek : Screen("pregnancy/week/{week}") {
        fun createRoute(week: Int) = "pregnancy/week/$week"
    }
    data object KickCounter : Screen("pregnancy/kick_counter")
    data object ContractionTimer : Screen("pregnancy/contraction_timer")
    data object BirthPlan : Screen("pregnancy/birth_plan")
    data object PregnancyDangerSigns : Screen("pregnancy/danger_signs")
    data object PregnancyChecklist : Screen("pregnancy/checklist")
    data object ANCSchedule : Screen("pregnancy/anc_schedule")

    // ═══════════════════════════════════════════════════════════════════════════
    // POSTPARTUM MODULE
    // ═══════════════════════════════════════════════════════════════════════════

    data object PostpartumHome : Screen("postpartum")
    data object PostpartumSetup : Screen("postpartum/setup")
    data object RecoveryTracker : Screen("postpartum/recovery")
    data object PostpartumChecklist : Screen("postpartum/checklist")
    data object BreastfeedingTracker : Screen("postpartum/breastfeeding")
    data object PostpartumMoodCheck : Screen("postpartum/mood_check")

    // ═══════════════════════════════════════════════════════════════════════════
    // CHILD CARE MODULE
    // ═══════════════════════════════════════════════════════════════════════════

    data object ChildCareHome : Screen("child_care")
    data object AddChild : Screen("child_care/add")
    data object ChildProfile : Screen("child_care/child/{childId}") {
        fun createRoute(childId: String) = "child_care/child/$childId"
    }
    data object ChildBodyMap : Screen("child_care/body_map/{childId}") {
        fun createRoute(childId: String) = "child_care/body_map/$childId"
    }
    data object VaccinationSchedule : Screen("child_care/vaccinations/{childId}") {
        fun createRoute(childId: String) = "child_care/vaccinations/$childId"
    }
    data object GrowthChart : Screen("child_care/growth/{childId}") {
        fun createRoute(childId: String) = "child_care/growth/$childId"
    }
    data object GrowthEntry : Screen("child_care/growth/{childId}/entry") {
        fun createRoute(childId: String) = "child_care/growth/$childId/entry"
    }
    data object Milestones : Screen("child_care/milestones/{childId}") {
        fun createRoute(childId: String) = "child_care/milestones/$childId"
    }
    data object FeedingLog : Screen("child_care/feeding/{childId}") {
        fun createRoute(childId: String) = "child_care/feeding/$childId"
    }
    data object SleepLog : Screen("child_care/sleep/{childId}") {
        fun createRoute(childId: String) = "child_care/sleep/$childId"
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CYCLE TRACKING MODULE
    // ═══════════════════════════════════════════════════════════════════════════

    data object CycleHome : Screen("cycle")
    data object CycleCalendar : Screen("cycle/calendar")
    data object PeriodLog : Screen("cycle/log")
    data object CycleInsights : Screen("cycle/insights")
    data object OvulationTracker : Screen("cycle/ovulation")
    data object CycleHistory : Screen("cycle/history")

    // ═══════════════════════════════════════════════════════════════════════════
    // MENTAL HEALTH MODULE
    // ═══════════════════════════════════════════════════════════════════════════

    data object MentalHealthHome : Screen("mental_health")
    data object MoodLog : Screen("mental_health/mood_log")
    data object MoodHistory : Screen("mental_health/mood_history")
    data object Screening : Screen("mental_health/screening/{type}") {
        fun createRoute(type: String) = "mental_health/screening/$type"
    }
    data object ScreeningResult : Screen("mental_health/screening_result/{resultId}") {
        fun createRoute(resultId: String) = "mental_health/screening_result/$resultId"
    }
    data object ScreeningHistory : Screen("mental_health/screening_history")
    data object CopingStrategies : Screen("mental_health/coping")
    data object BreathingExercise : Screen("mental_health/breathing")
    data object JournalEntry : Screen("mental_health/journal")
    data object CrisisSupport : Screen("mental_health/crisis")

    // ═══════════════════════════════════════════════════════════════════════════
    // ADOLESCENT MODULE
    // ═══════════════════════════════════════════════════════════════════════════

    data object AdolescentHome : Screen("adolescent")
    data object PubertyEducation : Screen("adolescent/puberty")
    data object FirstPeriodGuide : Screen("adolescent/first_period")
    data object BodyChanges : Screen("adolescent/body_changes")
    data object AdolescentMentalHealth : Screen("adolescent/mental_health")

    // ═══════════════════════════════════════════════════════════════════════════
    // MIDLIFE MODULE
    // ═══════════════════════════════════════════════════════════════════════════

    data object MidlifeHome : Screen("midlife")
    data object PerimenopauseTracker : Screen("midlife/perimenopause")
    data object MidlifeSymptoms : Screen("midlife/symptoms")
    data object BoneHealthTracker : Screen("midlife/bone_health")
    data object HealthScreenings : Screen("midlife/screenings")

    // ═══════════════════════════════════════════════════════════════════════════
    // ELDER CARE MODULE
    // ═══════════════════════════════════════════════════════════════════════════

    data object ElderHome : Screen("elder")
    data object MedicationManager : Screen("elder/medications")
    data object VitalSigns : Screen("elder/vitals")
    data object CaregiverMode : Screen("elder/caregiver")
    data object EmergencyContacts : Screen("elder/emergency_contacts")
    data object ElderHealthScreenings : Screen("elder/screenings")

    // ═══════════════════════════════════════════════════════════════════════════
    // EDUCATION & CONTENT
    // ═══════════════════════════════════════════════════════════════════════════

    data object HealthLibrary : Screen("education/library")
    data object ArticleDetail : Screen("education/article/{articleId}") {
        fun createRoute(articleId: String) = "education/article/$articleId"
    }
    data object VideoPlayer : Screen("education/video/{videoId}") {
        fun createRoute(videoId: String) = "education/video/$videoId"
    }
    data object FAQ : Screen("education/faq")
    data object AskQuestion : Screen("education/ask")

    // ═══════════════════════════════════════════════════════════════════════════
    // SETTINGS & PROFILE
    // ═══════════════════════════════════════════════════════════════════════════

    data object ProfileEdit : Screen("settings/profile")
    data object LanguageSettings : Screen("settings/language")
    data object NotificationSettings : Screen("settings/notifications")
    data object PrivacySettings : Screen("settings/privacy")
    data object DataExport : Screen("settings/data_export")
    data object Subscription : Screen("settings/subscription")
    data object FamilySharing : Screen("settings/family")
    data object HelpSupport : Screen("settings/help")
    data object About : Screen("settings/about")

    // ═══════════════════════════════════════════════════════════════════════════
    // EMERGENCY & CRISIS
    // ═══════════════════════════════════════════════════════════════════════════

    data object EmergencyScreen : Screen("emergency")
    data object NearbyFacilities : Screen("emergency/facilities")
    data object CrisisHelpline : Screen("emergency/helpline")
}

/**
 * Navigation arguments
 */
object NavArgs {
    const val PHONE_NUMBER = "phoneNumber"
    const val REGION = "region"
    const val SYMPTOM_LOG_ID = "symptomLogId"
    const val WEEK = "week"
    const val CHILD_ID = "childId"
    const val SCREENING_TYPE = "type"
    const val RESULT_ID = "resultId"
    const val ARTICLE_ID = "articleId"
    const val VIDEO_ID = "videoId"
}

/**
 * Bottom navigation items
 */
enum class BottomNavItem(
    val route: String,
    val label: String,
    val icon: String  // Will use icon resource IDs in actual implementation
) {
    HOME("home", "Home", "home"),
    INSIGHTS("insights", "Insights", "insights"),
    HISTORY("history", "History", "history"),
    SETTINGS("settings", "Settings", "settings")
}
