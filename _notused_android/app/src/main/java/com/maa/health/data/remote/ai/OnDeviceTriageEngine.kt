package com.maa.health.data.remote.ai

import com.maa.health.data.model.Action
import com.maa.health.data.model.BodyRegion
import com.maa.health.data.model.Severity
import com.maa.health.data.model.SymptomType
import com.maa.health.data.model.TriageResult
import com.maa.health.data.model.Urgency
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * On-Device Rule-Based Triage Engine
 *
 * Runs entirely on-device with zero network dependency for:
 * - Latency-critical danger sign detection (WHO pregnancy, IMCI child health)
 * - Offline triage capabilities in areas with poor connectivity
 * - Privacy-sensitive processing that never leaves the device
 *
 * This engine uses validated clinical rules (WHO/IMCI protocols),
 * not a machine learning model. No model download required.
 *
 * Inference time: <50ms (rule-based, no ML overhead)
 */
@Singleton
class OnDeviceTriageEngine @Inject constructor() {

    /**
     * Quick triage for common symptom patterns
     */
    suspend fun quickTriage(
        symptoms: List<SymptomType>,
        bodyRegion: BodyRegion,
        severity: Severity,
        context: TriageContext
    ): LocalInferenceResult = withContext(Dispatchers.Default) {
        // Check for immediate danger signs first
        val dangerResult = checkDangerSigns(
            symptoms,
            context.isPregnant,
            context.childAgeMonths
        )

        if (dangerResult.hasDangerSigns) {
            return@withContext LocalInferenceResult(
                result = TriageResult(
                    urgency = dangerResult.urgency,
                    classification = "Danger Sign Detected",
                    action = if (dangerResult.urgency == Urgency.EMERGENCY) {
                        Action.CALL_EMERGENCY
                    } else {
                        Action.GO_TO_HOSPITAL_NOW
                    },
                    instructions = listOf(dangerResult.immediateAction ?: "Seek immediate care"),
                    referralNeeded = true,
                    warningSignsToWatch = dangerResult.dangerSigns
                ),
                confidence = 0.95f
            )
        }

        // Rule-based triage for common patterns
        performRuleBasedTriage(symptoms, bodyRegion, severity, context)
    }

    /**
     * Check for WHO/IMCI danger signs
     * This runs entirely on-device for immediate response
     */
    fun checkDangerSigns(
        symptoms: List<SymptomType>,
        isPregnant: Boolean,
        childAgeMonths: Int?
    ): DangerSignResult {
        val dangerSigns = mutableListOf<String>()
        var urgency = Urgency.SELF_CARE
        var immediateAction: String? = null

        // Pregnancy danger signs (WHO)
        if (isPregnant) {
            if (SymptomType.BLEEDING in symptoms) {
                dangerSigns.add("Vaginal bleeding during pregnancy")
                urgency = Urgency.EMERGENCY
                immediateAction = "Go to hospital immediately. Do not wait."
            }
            if (SymptomType.CONVULSIONS in symptoms) {
                dangerSigns.add("Convulsions/seizures")
                urgency = Urgency.EMERGENCY
                immediateAction = "Call emergency services. Keep airways clear."
            }
            if (SymptomType.SEVERE_HEADACHE in symptoms && SymptomType.BLURRED_VISION in symptoms) {
                dangerSigns.add("Severe headache with vision changes - possible pre-eclampsia")
                urgency = Urgency.EMERGENCY
                immediateAction = "Go to hospital immediately. This may be pre-eclampsia."
            }
            if (SymptomType.LEAKING_FLUID in symptoms) {
                dangerSigns.add("Leaking fluid - possible rupture of membranes")
                urgency = maxOf(urgency, Urgency.URGENT)
                immediateAction = immediateAction ?: "Go to hospital today. Note the color of fluid."
            }
            if (SymptomType.REDUCED_FETAL_MOVEMENT in symptoms) {
                dangerSigns.add("Reduced fetal movement")
                urgency = maxOf(urgency, Urgency.URGENT)
                immediateAction = immediateAction ?: "Go to hospital today for fetal assessment."
            }
            if (SymptomType.DIFFICULTY_BREATHING in symptoms) {
                dangerSigns.add("Difficulty breathing")
                urgency = maxOf(urgency, Urgency.URGENT)
                immediateAction = immediateAction ?: "Seek medical care immediately."
            }
        }

        // Child danger signs (IMCI)
        if (childAgeMonths != null) {
            if (SymptomType.NOT_FEEDING in symptoms) {
                dangerSigns.add("Not able to feed")
                urgency = maxOf(urgency, Urgency.EMERGENCY)
                immediateAction = immediateAction ?: "Take child to hospital immediately."
            }
            if (SymptomType.CONVULSIONS in symptoms) {
                dangerSigns.add("Convulsions in child")
                urgency = Urgency.EMERGENCY
                immediateAction = "Call emergency. Keep child safe, don't put anything in mouth."
            }
            if (SymptomType.LETHARGY in symptoms) {
                dangerSigns.add("Unusually sleepy or difficult to wake")
                urgency = maxOf(urgency, Urgency.EMERGENCY)
                immediateAction = immediateAction ?: "Take child to hospital immediately."
            }
            if (SymptomType.CHEST_INDRAWING in symptoms) {
                dangerSigns.add("Chest indrawing - severe pneumonia sign")
                urgency = maxOf(urgency, Urgency.EMERGENCY)
                immediateAction = immediateAction ?: "Take child to hospital immediately."
            }
            if (SymptomType.FAST_BREATHING in symptoms && childAgeMonths < 2) {
                dangerSigns.add("Fast breathing in young infant")
                urgency = maxOf(urgency, Urgency.URGENT)
                immediateAction = immediateAction ?: "Take baby to hospital today."
            }
        }

        // General danger signs
        if (SymptomType.CONVULSIONS in symptoms && !isPregnant && childAgeMonths == null) {
            dangerSigns.add("Convulsions/seizures")
            urgency = Urgency.EMERGENCY
            immediateAction = "Call emergency services immediately."
        }
        if (SymptomType.DIFFICULTY_BREATHING in symptoms && !isPregnant) {
            dangerSigns.add("Difficulty breathing")
            urgency = maxOf(urgency, Urgency.URGENT)
            immediateAction = immediateAction ?: "Seek medical care immediately."
        }

        return DangerSignResult(
            hasDangerSigns = dangerSigns.isNotEmpty(),
            dangerSigns = dangerSigns,
            urgency = urgency,
            immediateAction = immediateAction
        )
    }

    /**
     * Rule-based triage for common symptom combinations
     */
    private fun performRuleBasedTriage(
        symptoms: List<SymptomType>,
        bodyRegion: BodyRegion,
        severity: Severity,
        context: TriageContext
    ): LocalInferenceResult {
        // Fever handling
        if (SymptomType.FEVER in symptoms) {
            val result = triageFever(symptoms, severity, context)
            return LocalInferenceResult(result, 0.80f)
        }

        // Gastrointestinal symptoms
        if (SymptomType.DIARRHEA in symptoms || SymptomType.VOMITING in symptoms) {
            val result = triageGI(symptoms, severity, context)
            return LocalInferenceResult(result, 0.75f)
        }

        // Pain-based triage
        if (SymptomType.PAIN in symptoms || SymptomType.HEADACHE in symptoms) {
            val result = triagePain(symptoms, bodyRegion, severity, context)
            return LocalInferenceResult(result, 0.70f)
        }

        // Default: recommend consultation for unclear cases
        return LocalInferenceResult(
            result = TriageResult(
                urgency = when (severity) {
                    Severity.SEVERE -> Urgency.URGENT
                    Severity.MODERATELY_SEVERE -> Urgency.URGENT
                    Severity.MODERATE -> Urgency.ROUTINE
                    else -> Urgency.SELF_CARE
                },
                classification = "Needs assessment",
                action = when (severity) {
                    Severity.SEVERE -> Action.GO_TO_HOSPITAL_TODAY
                    Severity.MODERATELY_SEVERE -> Action.GO_TO_HOSPITAL_TODAY
                    Severity.MODERATE -> Action.SCHEDULE_VISIT
                    else -> Action.HOME_MANAGEMENT
                },
                instructions = listOf(
                    "Monitor symptoms closely",
                    "Note any changes or new symptoms",
                    "Seek care if symptoms worsen"
                ),
                referralNeeded = severity >= Severity.MODERATE
            ),
            confidence = 0.50f  // Low confidence triggers cloud escalation
        )
    }

    private fun triageFever(
        symptoms: List<SymptomType>,
        severity: Severity,
        context: TriageContext
    ): TriageResult {
        val isYoungInfant = context.childAgeMonths != null && context.childAgeMonths < 2

        if (isYoungInfant) {
            return TriageResult(
                urgency = Urgency.URGENT,
                classification = "Fever in young infant",
                action = Action.GO_TO_HOSPITAL_TODAY,
                instructions = listOf(
                    "Take baby to hospital today",
                    "Keep baby well-hydrated",
                    "Do not give any medication without doctor's advice"
                ),
                referralNeeded = true
            )
        }

        if (severity >= Severity.MODERATELY_SEVERE) {
            return TriageResult(
                urgency = Urgency.URGENT,
                classification = "High fever requiring assessment",
                action = Action.GO_TO_HOSPITAL_TODAY,
                instructions = listOf(
                    "Go to hospital today",
                    "Give paracetamol as appropriate for age/weight",
                    "Keep well-hydrated",
                    "Apply cool cloth to forehead"
                ),
                referralNeeded = true,
                warningSignsToWatch = listOf(
                    "Stiff neck",
                    "Rash that doesn't fade with pressure",
                    "Difficulty breathing",
                    "Confusion or unusual behavior"
                )
            )
        }

        return TriageResult(
            urgency = Urgency.SELF_CARE,
            classification = "Fever - home management",
            action = Action.HOME_MANAGEMENT,
            instructions = listOf(
                "Give paracetamol as directed for age/weight",
                "Drink plenty of fluids",
                "Rest",
                "Apply cool cloth if comfortable",
                "Seek care if fever persists more than 3 days"
            ),
            referralNeeded = false,
            warningSignsToWatch = listOf(
                "Fever above 39C (102F)",
                "Fever lasting more than 3 days",
                "New symptoms developing",
                "Not drinking fluids"
            )
        )
    }

    private fun triageGI(
        symptoms: List<SymptomType>,
        severity: Severity,
        context: TriageContext
    ): TriageResult {
        val hasBothVomitingAndDiarrhea =
            SymptomType.VOMITING in symptoms && SymptomType.DIARRHEA in symptoms
        val isChild = context.childAgeMonths != null

        if (severity >= Severity.SEVERE) {
            return TriageResult(
                urgency = Urgency.URGENT,
                classification = "Possible severe dehydration",
                action = Action.GO_TO_HOSPITAL_NOW,
                instructions = listOf(
                    "Go to hospital immediately",
                    "Give ORS sips if able to drink",
                    "Do not give anti-diarrheal medicines to children"
                ),
                referralNeeded = true
            )
        }

        if (hasBothVomitingAndDiarrhea || severity >= Severity.MODERATE) {
            return TriageResult(
                urgency = if (isChild) Urgency.URGENT else Urgency.ROUTINE,
                classification = "Gastroenteritis - needs monitoring",
                action = if (isChild) Action.GO_TO_HOSPITAL_TODAY else Action.SCHEDULE_VISIT,
                instructions = listOf(
                    "Start ORS immediately",
                    "Small frequent sips",
                    "Continue breastfeeding if applicable",
                    "BRAT diet when ready (bananas, rice, applesauce, toast)",
                    "Monitor for signs of dehydration"
                ),
                referralNeeded = isChild,
                warningSignsToWatch = listOf(
                    "No urine for 6+ hours",
                    "Very dry mouth",
                    "Crying without tears",
                    "Sunken eyes",
                    "Blood in stool"
                )
            )
        }

        return TriageResult(
            urgency = Urgency.SELF_CARE,
            classification = "Mild gastroenteritis",
            action = Action.HOME_MANAGEMENT,
            instructions = listOf(
                "Give ORS or clear fluids",
                "Rest the stomach for 1-2 hours after vomiting",
                "Slowly reintroduce bland foods",
                "Wash hands frequently to prevent spread"
            ),
            referralNeeded = false,
            warningSignsToWatch = listOf(
                "Symptoms lasting more than 2 days",
                "Blood in stool or vomit",
                "High fever",
                "Severe abdominal pain"
            )
        )
    }

    private fun triagePain(
        symptoms: List<SymptomType>,
        bodyRegion: BodyRegion,
        severity: Severity,
        context: TriageContext
    ): TriageResult {
        if (severity >= Severity.SEVERE) {
            return TriageResult(
                urgency = Urgency.URGENT,
                classification = "Severe pain requiring assessment",
                action = Action.GO_TO_HOSPITAL_TODAY,
                instructions = listOf(
                    "Seek medical care today",
                    "Note when pain started and any triggers",
                    "Avoid eating until evaluated if abdominal pain"
                ),
                referralNeeded = true
            )
        }

        val instructions = when (bodyRegion) {
            BodyRegion.HEAD -> listOf(
                "Rest in a quiet, dark room",
                "Apply cool cloth to forehead",
                "Stay hydrated",
                "Take paracetamol if needed"
            )
            BodyRegion.ABDOMEN, BodyRegion.ABDOMEN_UPPER -> listOf(
                "Rest",
                "Avoid heavy meals",
                "Apply warm compress if comfortable",
                "Note relation to meals"
            )
            BodyRegion.CHEST -> listOf(
                "Rest",
                "Avoid exertion",
                "Seek immediate care if pain spreads to arm or jaw",
                "Note if pain changes with breathing"
            )
            else -> listOf(
                "Rest the affected area",
                "Apply ice for swelling, heat for stiffness",
                "Take paracetamol if needed"
            )
        }

        return TriageResult(
            urgency = if (severity >= Severity.MODERATE) Urgency.ROUTINE else Urgency.SELF_CARE,
            classification = "Pain - home management with monitoring",
            action = if (severity >= Severity.MODERATE) Action.SCHEDULE_VISIT else Action.HOME_MANAGEMENT,
            instructions = instructions,
            referralNeeded = severity >= Severity.MODERATE,
            warningSignsToWatch = listOf(
                "Pain getting worse",
                "Fever developing",
                "New symptoms",
                "Pain not responding to medication"
            )
        )
    }
}
