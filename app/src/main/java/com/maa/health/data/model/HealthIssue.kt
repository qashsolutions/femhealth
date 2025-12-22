package com.maa.health.data.model

import java.time.Instant

/**
 * Represents a detected health issue for notifications
 */
data class HealthIssue(
    val type: HealthIssueType,
    val severity: Severity,
    val title: String,
    val description: String,
    val recommendation: String,
    val timestamp: Instant,
    val requiresImmediateAction: Boolean = false
)

/**
 * Types of health issues that can be detected
 */
enum class HealthIssueType {
    DANGER_SIGN,
    RECURRING_SYMPTOM,
    MOOD_DECLINE,
    PMDD_INDICATOR,
    CYCLE_IRREGULARITY,
    PCOS_RISK,
    MISSED_MEDICATION,
    OVERDUE_VACCINATION,
    SCREENING_DUE
}

/**
 * Monitored conditions that users can enable/disable
 */
data class MonitoredCondition(
    val id: String,
    val name: String,
    val nameHindi: String,
    val description: String,
    val isEnabled: Boolean = true,
    val category: ConditionCategory
)

/**
 * Categories of monitored conditions
 */
enum class ConditionCategory {
    PREGNANCY,
    CYCLE,
    MENTAL_HEALTH,
    CHILD_HEALTH,
    GENERAL
}

/**
 * Default monitored conditions for the app
 */
object DefaultMonitoredConditions {
    val conditions = listOf(
        MonitoredCondition(
            id = "danger_signs",
            name = "Danger Signs",
            nameHindi = "खतरे के संकेत",
            description = "Alert when danger signs are logged",
            category = ConditionCategory.PREGNANCY
        ),
        MonitoredCondition(
            id = "recurring_symptoms",
            name = "Recurring Symptoms",
            nameHindi = "बार-बार होने वाले लक्षण",
            description = "Notify when symptoms recur frequently",
            category = ConditionCategory.GENERAL
        ),
        MonitoredCondition(
            id = "mood_trends",
            name = "Mood Trends",
            nameHindi = "मूड ट्रेंड",
            description = "Track mood patterns and alert on concerns",
            category = ConditionCategory.MENTAL_HEALTH
        ),
        MonitoredCondition(
            id = "cycle_irregularity",
            name = "Cycle Irregularity",
            nameHindi = "चक्र अनियमितता",
            description = "Monitor cycle patterns for irregularities",
            category = ConditionCategory.CYCLE
        ),
        MonitoredCondition(
            id = "pcos_indicators",
            name = "PCOS Indicators",
            nameHindi = "पीसीओएस संकेतक",
            description = "Watch for potential PCOS symptoms",
            category = ConditionCategory.CYCLE
        ),
        MonitoredCondition(
            id = "pmdd_correlation",
            name = "PMDD Correlation",
            nameHindi = "पीएमडीडी सहसंबंध",
            description = "Track mood-cycle correlation",
            category = ConditionCategory.MENTAL_HEALTH
        ),
        MonitoredCondition(
            id = "medication_adherence",
            name = "Medication Reminders",
            nameHindi = "दवा अनुस्मारक",
            description = "Remind when medications are missed",
            category = ConditionCategory.GENERAL
        ),
        MonitoredCondition(
            id = "vaccination_schedule",
            name = "Vaccination Alerts",
            nameHindi = "टीकाकरण अलर्ट",
            description = "Notify when vaccinations are due",
            category = ConditionCategory.CHILD_HEALTH
        ),
        MonitoredCondition(
            id = "growth_milestones",
            name = "Growth Milestones",
            nameHindi = "विकास के पड़ाव",
            description = "Track child development milestones",
            category = ConditionCategory.CHILD_HEALTH
        ),
        MonitoredCondition(
            id = "screening_reminders",
            name = "Mental Health Screenings",
            nameHindi = "मानसिक स्वास्थ्य जांच",
            description = "Periodic mental health check-in reminders",
            category = ConditionCategory.MENTAL_HEALTH
        )
    )
}
