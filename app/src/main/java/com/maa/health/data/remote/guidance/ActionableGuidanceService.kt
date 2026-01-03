package com.maa.health.data.remote.guidance

import com.maa.health.data.model.BodyRegion
import com.maa.health.data.model.Severity
import com.maa.health.data.model.SymptomType
import com.maa.health.data.model.Urgency
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Actionable Guidance Service
 *
 * Provides helpful, actionable guidance for users with limited healthcare access.
 *
 * PHILOSOPHY:
 * - We EMPOWER with knowledge, we don't diagnose
 * - We provide OPTIONS, not prescriptions
 * - We use VALIDATED guidance (WHO, IMCI, IMNCI)
 * - We are CONSERVATIVE - when in doubt, escalate
 * - We CONNECT to real help (ASHA, PHC, helplines)
 *
 * WHAT WE SAY:
 * ✓ "These symptoms are commonly seen in conditions like..."
 * ✓ "WHO recommends... for this situation"
 * ✓ "Here's what you can safely do at home"
 * ✓ "Watch for these warning signs"
 * ✓ "Your nearest health center is..."
 *
 * WHAT WE NEVER SAY:
 * ✗ "You have [disease]"
 * ✗ "Take [medication]"
 * ✗ "Don't worry, it's nothing"
 */
@Singleton
class ActionableGuidanceService @Inject constructor() {

    /**
     * Get tiered guidance based on symptoms
     * Returns Red (urgent), Yellow (monitor), or Green (reassurance) guidance
     */
    fun getGuidance(
        symptoms: List<SymptomType>,
        severity: Severity,
        context: GuidanceContext
    ): ActionableGuidance {
        // Check for danger signs first (always Red tier)
        val dangerSignCheck = checkDangerSigns(symptoms, context)
        if (dangerSignCheck.hasDangerSigns) {
            return createRedTierGuidance(dangerSignCheck, context)
        }

        // Determine tier based on severity and symptom combination
        val tier = determineTier(symptoms, severity, context)

        return when (tier) {
            GuidanceTier.RED -> createRedTierGuidance(dangerSignCheck, context)
            GuidanceTier.YELLOW -> createYellowTierGuidance(symptoms, severity, context)
            GuidanceTier.GREEN -> createGreenTierGuidance(symptoms, context)
        }
    }

    /**
     * Get home care guidance based on WHO/UNICEF recommendations
     * These are VALIDATED public health measures, not medical advice
     */
    fun getHomeCareGuidance(
        situation: HomeCareScenario
    ): HomeCareGuidance {
        return when (situation) {
            HomeCareScenario.FEVER_CHILD -> HomeCareGuidance(
                title = "Managing Fever at Home",
                source = "Based on WHO/UNICEF IMCI Guidelines",
                steps = listOf(
                    HomeCareStep(
                        instruction = "Keep the child lightly dressed",
                        reason = "Heavy clothing can increase body temperature",
                        icon = "clothing"
                    ),
                    HomeCareStep(
                        instruction = "Give plenty of fluids - breastmilk, clean water, or ORS",
                        reason = "Fever increases fluid loss. Preventing dehydration is important.",
                        icon = "water"
                    ),
                    HomeCareStep(
                        instruction = "Sponge with lukewarm (not cold) water if temperature is very high",
                        reason = "Helps bring down temperature gradually. Cold water can cause shivering which increases temperature.",
                        icon = "sponge"
                    ),
                    HomeCareStep(
                        instruction = "Continue feeding - breast milk or regular food",
                        reason = "The child needs energy to fight infection",
                        icon = "food"
                    )
                ),
                warningSignsToReturn = listOf(
                    "Fever lasts more than 3 days",
                    "Child becomes less active or difficult to wake",
                    "Child refuses to eat or drink",
                    "Child has fast or difficult breathing",
                    "Child has convulsions (fits)"
                ),
                whatThisMeans = "Fever is the body's natural response to infection. Most fevers in children are caused by viral infections that improve on their own. These steps help keep your child comfortable while the body fights the infection.",
                disclaimer = "This guidance is for mild fever without danger signs. If your child has any warning signs or you are worried, please seek care immediately."
            )

            HomeCareScenario.DIARRHEA_CHILD -> HomeCareGuidance(
                title = "Managing Diarrhea at Home",
                source = "Based on WHO/UNICEF ORS and Zinc Guidelines",
                steps = listOf(
                    HomeCareStep(
                        instruction = "Give ORS (Oral Rehydration Solution) after each loose stool",
                        reason = "Diarrhea causes loss of water and salts. ORS replaces both.",
                        icon = "ors",
                        additionalInfo = "How to make ORS at home: 1 liter clean water + 6 level teaspoons sugar + ½ level teaspoon salt. Mix well."
                    ),
                    HomeCareStep(
                        instruction = "Continue breastfeeding or regular feeding",
                        reason = "Food helps the gut recover faster. Starving does not help.",
                        icon = "breastfeed"
                    ),
                    HomeCareStep(
                        instruction = "Give zinc supplements if available (for children)",
                        reason = "Zinc reduces duration and severity of diarrhea. Available at health centers.",
                        icon = "zinc"
                    ),
                    HomeCareStep(
                        instruction = "Wash hands with soap after using toilet and before preparing food",
                        reason = "Prevents spread to others and reinfection",
                        icon = "handwash"
                    )
                ),
                warningSignsToReturn = listOf(
                    "Blood in stool",
                    "Child has sunken eyes or very dry mouth",
                    "Child is very thirsty or drinking eagerly",
                    "Child is lethargic or unconscious",
                    "Diarrhea continues for more than 3 days",
                    "Child vomits everything"
                ),
                whatThisMeans = "Most diarrhea in children is caused by infections that improve in a few days. The main danger is dehydration (loss of water). ORS prevents this. The body will clear the infection on its own.",
                disclaimer = "This guidance is for watery diarrhea without danger signs. If you see blood in stool or signs of severe dehydration, seek care immediately."
            )

            HomeCareScenario.MORNING_SICKNESS -> HomeCareGuidance(
                title = "Managing Nausea in Early Pregnancy",
                source = "Based on standard prenatal care guidelines",
                steps = listOf(
                    HomeCareStep(
                        instruction = "Eat small, frequent meals instead of large meals",
                        reason = "An empty stomach can make nausea worse",
                        icon = "small_meals"
                    ),
                    HomeCareStep(
                        instruction = "Keep dry crackers or toast by your bed - eat before getting up",
                        reason = "Eating something bland before rising can reduce morning nausea",
                        icon = "crackers"
                    ),
                    HomeCareStep(
                        instruction = "Avoid strong smells, spicy or greasy foods",
                        reason = "These can trigger nausea in pregnancy",
                        icon = "avoid_smell"
                    ),
                    HomeCareStep(
                        instruction = "Stay hydrated - sip water, coconut water, or ginger tea",
                        reason = "Dehydration can worsen nausea. Ginger may help reduce nausea.",
                        icon = "hydrate"
                    ),
                    HomeCareStep(
                        instruction = "Get fresh air and rest when possible",
                        reason = "Fatigue can make nausea worse",
                        icon = "rest"
                    )
                ),
                warningSignsToReturn = listOf(
                    "You cannot keep any food or water down for 24 hours",
                    "You are urinating very little or urine is dark",
                    "You feel dizzy or faint",
                    "You are losing weight",
                    "You have severe abdominal pain"
                ),
                whatThisMeans = "Nausea in early pregnancy (often called morning sickness) is very common - it affects up to 80% of pregnant women. It usually improves after 12-14 weeks. It's actually a sign of a healthy pregnancy.",
                disclaimer = "Mild to moderate nausea is normal in pregnancy. However, severe vomiting (hyperemesis gravidarum) needs medical care. If you have warning signs, please seek care."
            )

            HomeCareScenario.POSTPARTUM_MOOD -> HomeCareGuidance(
                title = "Taking Care of Your Emotional Wellbeing After Delivery",
                source = "Based on maternal mental health guidelines",
                steps = listOf(
                    HomeCareStep(
                        instruction = "Rest when baby rests - sleep is essential for recovery",
                        reason = "Sleep deprivation significantly affects mood. Even short naps help.",
                        icon = "sleep"
                    ),
                    HomeCareStep(
                        instruction = "Accept help from family - you don't have to do everything alone",
                        reason = "Having support reduces stress and gives you time to recover",
                        icon = "support"
                    ),
                    HomeCareStep(
                        instruction = "Talk to someone you trust about how you're feeling",
                        reason = "Sharing feelings reduces their intensity. You are not alone.",
                        icon = "talk"
                    ),
                    HomeCareStep(
                        instruction = "Get some sunlight and gentle movement when possible",
                        reason = "Sunlight and light exercise naturally improve mood",
                        icon = "sunlight"
                    ),
                    HomeCareStep(
                        instruction = "Eat nutritious food and stay hydrated",
                        reason = "Your body is recovering. Good nutrition supports mental health too.",
                        icon = "nutrition"
                    )
                ),
                warningSignsToReturn = listOf(
                    "Thoughts of harming yourself or your baby",
                    "Feeling hopeless or worthless for more than 2 weeks",
                    "Unable to care for yourself or your baby",
                    "Severe anxiety or panic attacks",
                    "Hearing or seeing things that aren't there"
                ),
                whatThisMeans = "Feeling emotional after delivery is extremely common - your hormones are changing dramatically, you're sleep deprived, and caring for a newborn is challenging. 'Baby blues' (mild sadness, mood swings, crying) affects up to 80% of new mothers and usually improves within 2 weeks.",
                disclaimer = "Baby blues is normal and temporary. However, if feelings persist beyond 2 weeks or are severe, this may be postpartum depression which needs support. You deserve help - please reach out."
            )

            HomeCareScenario.PERIOD_PAIN -> HomeCareGuidance(
                title = "Managing Menstrual Cramps",
                source = "Based on gynecological care guidelines",
                steps = listOf(
                    HomeCareStep(
                        instruction = "Apply heat to lower abdomen - hot water bottle or warm cloth",
                        reason = "Heat relaxes the muscles that are cramping",
                        icon = "heat"
                    ),
                    HomeCareStep(
                        instruction = "Gentle movement or stretching can help",
                        reason = "Light activity increases blood flow and releases natural pain relievers",
                        icon = "stretch"
                    ),
                    HomeCareStep(
                        instruction = "Drink warm fluids - water, herbal tea",
                        reason = "Staying hydrated and warmth from inside can reduce cramping",
                        icon = "warm_drink"
                    ),
                    HomeCareStep(
                        instruction = "Rest in a comfortable position",
                        reason = "Your body is working hard. Rest helps.",
                        icon = "rest"
                    )
                ),
                warningSignsToReturn = listOf(
                    "Pain is so severe you cannot do normal activities",
                    "Pain medications don't help at all",
                    "Very heavy bleeding (soaking a pad every hour)",
                    "Fever along with period pain",
                    "Pain at times other than your period"
                ),
                whatThisMeans = "Menstrual cramps happen because the uterus contracts to shed its lining. Some discomfort is normal. The level of pain varies - some women have mild cramps, others have more severe pain.",
                disclaimer = "Mild to moderate cramps are common. However, very severe pain that interferes with daily life may indicate conditions like endometriosis that need evaluation."
            )
        }
    }

    /**
     * Get educational content that empowers users to understand their health
     * This builds health literacy without diagnosing
     */
    fun getEducationalContext(
        topic: EducationalTopic
    ): EducationalContent {
        return when (topic) {
            EducationalTopic.UNDERSTANDING_FEVER -> EducationalContent(
                title = "Understanding Fever",
                sections = listOf(
                    EducationalSection(
                        heading = "What is fever?",
                        content = "Fever is when body temperature rises above normal (usually above 38°C or 100.4°F). It's not a disease itself - it's a sign that your body is fighting an infection."
                    ),
                    EducationalSection(
                        heading = "Why does fever happen?",
                        content = "When germs enter your body, your immune system raises your temperature. This is actually helpful - most germs don't survive well at higher temperatures. Fever is your body's defense."
                    ),
                    EducationalSection(
                        heading = "When is fever serious?",
                        content = "Fever itself is usually not dangerous in older children and adults. What matters is: the cause of fever, how the person is behaving (alert vs very sick), and whether there are other concerning signs."
                    ),
                    EducationalSection(
                        heading = "Questions to ask yourself",
                        content = "• Is the person drinking fluids?\n• Is the person responsive and making eye contact?\n• Is breathing normal?\n• Are there any rashes or other symptoms?\n• How many days has the fever lasted?"
                    )
                ),
                keyTakeaway = "Fever is usually your body's healthy response to infection. Focus on keeping comfortable, staying hydrated, and watching for warning signs rather than just the number on the thermometer."
            )

            EducationalTopic.UNDERSTANDING_PREGNANCY_SYMPTOMS -> EducationalContent(
                title = "Normal vs Concerning Pregnancy Symptoms",
                sections = listOf(
                    EducationalSection(
                        heading = "Common normal symptoms",
                        content = "• Nausea (morning sickness) - especially in first 3 months\n• Tiredness - your body is working hard\n• Frequent urination - baby pressing on bladder\n• Breast tenderness - hormonal changes\n• Mild swelling of feet late in pregnancy"
                    ),
                    EducationalSection(
                        heading = "Symptoms that need immediate attention",
                        content = "• Vaginal bleeding - any amount\n• Severe headache with vision changes\n• Sudden swelling of face or hands\n• Baby moving less than usual\n• Leaking fluid from vagina\n• Severe abdominal pain"
                    ),
                    EducationalSection(
                        heading = "Why these danger signs matter",
                        content = "Bleeding can indicate placenta problems. Headache with vision changes can be pre-eclampsia (high BP in pregnancy). Reduced baby movement needs checking. These need urgent evaluation - don't wait."
                    )
                ),
                keyTakeaway = "Most pregnancy symptoms are normal and temporary. Learn the danger signs so you know when to seek help immediately vs when you can wait for your next checkup."
            )

            EducationalTopic.UNDERSTANDING_MENTAL_HEALTH -> EducationalContent(
                title = "Understanding Your Emotional Health",
                sections = listOf(
                    EducationalSection(
                        heading = "Emotions are information",
                        content = "Feeling sad, anxious, or overwhelmed are normal human emotions. They become concerning when they persist, are very intense, or stop you from functioning."
                    ),
                    EducationalSection(
                        heading = "Common experiences that aren't weakness",
                        content = "• Feeling sad after loss or difficulty\n• Anxiety about health, family, or money\n• Mood changes during periods or pregnancy\n• Feeling overwhelmed as a new mother\n\nThese are human experiences, not character flaws."
                    ),
                    EducationalSection(
                        heading = "When to seek support",
                        content = "Consider reaching out if:\n• Feelings last more than 2 weeks\n• You can't do daily activities\n• You're using alcohol/substances to cope\n• You have thoughts of harming yourself\n• Others are expressing concern"
                    ),
                    EducationalSection(
                        heading = "Getting help is strength",
                        content = "Seeking help for emotional struggles is a sign of courage, not weakness. Mental health is as important as physical health. Treatment works and help is available."
                    )
                ),
                keyTakeaway = "Your feelings are valid. Mental health exists on a spectrum. Most people experience difficult times. Help is available and recovery is possible."
            )

            EducationalTopic.UNDERSTANDING_CHILD_ILLNESS -> EducationalContent(
                title = "When is My Child Sick Enough to Worry?",
                sections = listOf(
                    EducationalSection(
                        heading = "Children get sick often",
                        content = "Young children typically get 6-8 infections per year. This is normal and helps build immunity. Most are mild and improve on their own."
                    ),
                    EducationalSection(
                        heading = "What to watch, not just measure",
                        content = "More important than the thermometer reading:\n• Is the child drinking fluids?\n• Is the child responsive to you?\n• How is the child breathing?\n• Is the child making tears/urinating?\n• Is the child very different from usual?"
                    ),
                    EducationalSection(
                        heading = "IMCI danger signs (seek care immediately)",
                        content = "• Cannot drink or breastfeed\n• Vomits everything\n• Has had convulsions\n• Is very sleepy or unconscious\n• Has fast or difficult breathing\n• Has severe malnutrition"
                    )
                ),
                keyTakeaway = "Trust your instincts - you know your child best. Watch behavior more than numbers. When in doubt, seek care. It's better to check and be reassured than to wait too long."
            )
        }
    }

    /**
     * Get healthcare access options based on location
     */
    fun getHealthcareOptions(
        urgency: Urgency,
        location: String?
    ): HealthcareOptions {
        return HealthcareOptions(
            immediateOptions = when (urgency) {
                Urgency.EMERGENCY -> listOf(
                    HealthcareOption(
                        type = "Emergency",
                        name = "Call 108 (Emergency Ambulance)",
                        description = "Free ambulance service available 24/7",
                        action = "CALL",
                        contact = "108",
                        priority = 1
                    ),
                    HealthcareOption(
                        type = "Emergency",
                        name = "Go to nearest hospital emergency",
                        description = "Don't wait - go now. Take someone with you if possible.",
                        action = "GO",
                        priority = 2
                    )
                )
                Urgency.URGENT -> listOf(
                    HealthcareOption(
                        type = "Health Center",
                        name = "Visit Primary Health Center (PHC)",
                        description = "Government health center - services are free or low cost",
                        action = "VISIT",
                        priority = 1
                    ),
                    HealthcareOption(
                        type = "Community Health Worker",
                        name = "Contact your ASHA worker",
                        description = "ASHA didi can assess and guide you to the right care",
                        action = "CONTACT",
                        priority = 2
                    )
                )
                Urgency.ROUTINE -> listOf(
                    HealthcareOption(
                        type = "Community Health Worker",
                        name = "Talk to your ASHA worker",
                        description = "She can answer questions and arrange PHC visit if needed",
                        action = "CONTACT",
                        priority = 1
                    ),
                    HealthcareOption(
                        type = "Helpline",
                        name = "Call 104 Health Helpline",
                        description = "Free health advice over phone, available 24/7",
                        action = "CALL",
                        contact = "104",
                        priority = 2
                    )
                )
                Urgency.SELF_CARE -> listOf(
                    HealthcareOption(
                        type = "Helpline",
                        name = "Call 104 if you have questions",
                        description = "Free health advice over phone",
                        action = "CALL",
                        contact = "104",
                        priority = 1
                    )
                )
            },
            helplines = listOf(
                Helpline("108", "Emergency Ambulance", "24/7 free ambulance service"),
                Helpline("104", "Health Helpline", "Free health information and advice"),
                Helpline("1098", "Childline", "For child-related emergencies"),
                Helpline("181", "Women Helpline", "For women in distress"),
                Helpline("9152987821", "iCall", "Free mental health counseling")
            ),
            disclaimer = "These are general options. Actual availability may vary by location. In emergency, don't delay - go to the nearest hospital."
        )
    }

    // Private helper methods

    private fun checkDangerSigns(
        symptoms: List<SymptomType>,
        context: GuidanceContext
    ): DangerSignResult {
        val dangerSigns = mutableListOf<String>()

        // Pregnancy danger signs (WHO)
        if (context.isPregnant) {
            if (SymptomType.BLEEDING in symptoms) {
                dangerSigns.add("Vaginal bleeding during pregnancy")
            }
            if (SymptomType.CONVULSIONS in symptoms) {
                dangerSigns.add("Convulsions/fits")
            }
            if (SymptomType.SEVERE_HEADACHE in symptoms && SymptomType.BLURRED_VISION in symptoms) {
                dangerSigns.add("Severe headache with vision problems (possible pre-eclampsia)")
            }
            if (SymptomType.LEAKING_FLUID in symptoms) {
                dangerSigns.add("Leaking fluid (possible ruptured membranes)")
            }
            if (SymptomType.REDUCED_FETAL_MOVEMENT in symptoms) {
                dangerSigns.add("Baby moving less than usual")
            }
        }

        // Child danger signs (IMCI)
        context.childAgeMonths?.let { age ->
            if (SymptomType.NOT_FEEDING in symptoms) {
                dangerSigns.add("Child not able to drink or breastfeed")
            }
            if (SymptomType.CONVULSIONS in symptoms) {
                dangerSigns.add("Child has had convulsions")
            }
            if (SymptomType.LETHARGY in symptoms) {
                dangerSigns.add("Child is very sleepy or unconscious")
            }
            if (SymptomType.CHEST_INDRAWING in symptoms) {
                dangerSigns.add("Child has chest indrawing (severe pneumonia sign)")
            }
            if (age < 2 && SymptomType.FAST_BREATHING in symptoms) {
                dangerSigns.add("Fast breathing in young infant")
            }
        }

        // General danger signs
        if (SymptomType.DIFFICULTY_BREATHING in symptoms) {
            dangerSigns.add("Difficulty breathing")
        }

        return DangerSignResult(
            hasDangerSigns = dangerSigns.isNotEmpty(),
            dangerSigns = dangerSigns
        )
    }

    private fun determineTier(
        symptoms: List<SymptomType>,
        severity: Severity,
        context: GuidanceContext
    ): GuidanceTier {
        // Severe symptoms = Yellow minimum
        if (severity == Severity.SEVERE) {
            return GuidanceTier.YELLOW
        }

        // Moderate with concerning symptoms = Yellow
        if (severity == Severity.MODERATE || severity == Severity.MODERATELY_SEVERE) {
            return GuidanceTier.YELLOW
        }

        // Young infants with fever = Yellow
        if (context.childAgeMonths != null && context.childAgeMonths < 2 && SymptomType.FEVER in symptoms) {
            return GuidanceTier.YELLOW
        }

        // Mild symptoms = Green
        return GuidanceTier.GREEN
    }

    private fun createRedTierGuidance(
        dangerSignResult: DangerSignResult,
        context: GuidanceContext
    ): ActionableGuidance {
        return ActionableGuidance(
            tier = GuidanceTier.RED,
            headline = "Please Seek Care Immediately",
            urgency = Urgency.EMERGENCY,
            explanation = "The symptoms you described include danger signs that need immediate medical attention. Please don't wait.",
            dangerSigns = dangerSignResult.dangerSigns,
            immediateActions = listOf(
                "Call 108 for ambulance OR go to nearest hospital now",
                "Take someone with you if possible",
                "Bring any medications you are taking",
                "Don't eat or drink anything until you see a doctor (in case surgery is needed)"
            ),
            whatNotToDo = listOf(
                "Don't wait to see if it gets better",
                "Don't take any new medications without medical advice",
                "Don't travel alone if possible"
            ),
            possibleReasons = emptyList(), // Don't speculate in emergencies
            homeCare = null, // No home care for emergencies
            whenToSeekCare = "NOW - Please go immediately",
            healthcareOptions = getHealthcareOptions(Urgency.EMERGENCY, null),
            disclaimer = "This guidance is based on recognized danger signs. These symptoms can indicate serious conditions that need immediate evaluation. Even if it turns out to be less serious, it's always right to check."
        )
    }

    private fun createYellowTierGuidance(
        symptoms: List<SymptomType>,
        severity: Severity,
        context: GuidanceContext
    ): ActionableGuidance {
        val possibleReasons = getPossibleReasons(symptoms, context)

        return ActionableGuidance(
            tier = GuidanceTier.YELLOW,
            headline = "These Symptoms Need Attention",
            urgency = Urgency.URGENT,
            explanation = "Based on what you've described, it would be good to have this checked by a healthcare provider within the next day or two.",
            dangerSigns = emptyList(),
            immediateActions = listOf(
                "Plan to visit PHC or see a healthcare provider soon",
                "Contact your ASHA worker who can help arrange this",
                "Keep monitoring symptoms - write down any changes",
                "Stay hydrated and rest"
            ),
            whatNotToDo = listOf(
                "Don't ignore if symptoms worsen",
                "Don't take medications not prescribed for you"
            ),
            possibleReasons = possibleReasons,
            homeCare = getRelevantHomeCare(symptoms, context),
            whenToSeekCare = "Within 24-48 hours, or sooner if symptoms worsen",
            healthcareOptions = getHealthcareOptions(Urgency.URGENT, null),
            warningSignsToWatch = getWarningSignsToWatch(symptoms, context),
            disclaimer = "These are possible reasons based on common patterns. Only a healthcare provider can determine the actual cause after examination. When in doubt, seek care sooner."
        )
    }

    private fun createGreenTierGuidance(
        symptoms: List<SymptomType>,
        context: GuidanceContext
    ): ActionableGuidance {
        val possibleReasons = getPossibleReasons(symptoms, context)

        return ActionableGuidance(
            tier = GuidanceTier.GREEN,
            headline = "This Can Likely Be Managed at Home",
            urgency = Urgency.SELF_CARE,
            explanation = "Based on what you've described, these symptoms are often manageable at home. Here's what might help and what to watch for.",
            dangerSigns = emptyList(),
            immediateActions = emptyList(),
            whatNotToDo = listOf(
                "Don't ignore if symptoms persist more than a few days",
                "Don't hesitate to seek care if you're worried"
            ),
            possibleReasons = possibleReasons,
            homeCare = getRelevantHomeCare(symptoms, context),
            whenToSeekCare = "If symptoms don't improve in 2-3 days, or if you're worried at any time",
            healthcareOptions = getHealthcareOptions(Urgency.SELF_CARE, null),
            warningSignsToWatch = getWarningSignsToWatch(symptoms, context),
            disclaimer = "This guidance is for mild symptoms. You know your body best - if something feels wrong, it's always okay to seek care. Trust your instincts."
        )
    }

    private fun getPossibleReasons(
        symptoms: List<SymptomType>,
        context: GuidanceContext
    ): List<PossibleReason> {
        // Return educational information about what commonly causes these symptoms
        // NOT diagnosis, but health literacy

        val reasons = mutableListOf<PossibleReason>()

        if (SymptomType.FEVER in symptoms) {
            reasons.add(
                PossibleReason(
                    commonName = "Viral Infection",
                    explanation = "Most fevers are caused by viral infections (like cold or flu). These usually improve on their own in 3-5 days.",
                    likelihood = "Very Common"
                )
            )
            reasons.add(
                PossibleReason(
                    commonName = "Bacterial Infection",
                    explanation = "Some fevers are caused by bacterial infections that may need antibiotics. A healthcare provider can determine this.",
                    likelihood = "Common"
                )
            )
        }

        if (SymptomType.HEADACHE in symptoms) {
            reasons.add(
                PossibleReason(
                    commonName = "Tension Headache",
                    explanation = "Stress, poor sleep, or dehydration commonly cause headaches. Rest and hydration often help.",
                    likelihood = "Very Common"
                )
            )
        }

        if (SymptomType.NAUSEA in symptoms && context.isPregnant) {
            reasons.add(
                PossibleReason(
                    commonName = "Morning Sickness",
                    explanation = "Nausea in pregnancy is very common, especially in the first 3 months. It's caused by pregnancy hormones and usually improves.",
                    likelihood = "Very Common in Pregnancy"
                )
            )
        }

        return reasons
    }

    private fun getRelevantHomeCare(
        symptoms: List<SymptomType>,
        context: GuidanceContext
    ): HomeCareGuidance? {
        // Return appropriate home care based on symptoms
        return when {
            context.childAgeMonths != null && SymptomType.FEVER in symptoms ->
                getHomeCareGuidance(HomeCareScenario.FEVER_CHILD)
            context.childAgeMonths != null && SymptomType.DIARRHEA in symptoms ->
                getHomeCareGuidance(HomeCareScenario.DIARRHEA_CHILD)
            context.isPregnant && SymptomType.NAUSEA in symptoms ->
                getHomeCareGuidance(HomeCareScenario.MORNING_SICKNESS)
            else -> null
        }
    }

    private fun getWarningSignsToWatch(
        symptoms: List<SymptomType>,
        context: GuidanceContext
    ): List<String> {
        val warnings = mutableListOf<String>()

        warnings.add("Symptoms getting worse instead of better")
        warnings.add("New symptoms developing")

        if (SymptomType.FEVER in symptoms) {
            warnings.add("Fever lasting more than 3 days")
            warnings.add("Very high fever (over 39°C/102°F)")
        }

        if (context.isPregnant) {
            warnings.add("Any vaginal bleeding")
            warnings.add("Severe headache or vision changes")
            warnings.add("Baby moving less than usual")
        }

        if (context.childAgeMonths != null) {
            warnings.add("Child stops eating or drinking")
            warnings.add("Child becomes very sleepy or hard to wake")
            warnings.add("Child has difficulty breathing")
        }

        return warnings
    }
}

// Data classes

enum class GuidanceTier {
    RED,    // Seek care immediately
    YELLOW, // Seek care soon / monitor closely
    GREEN   // Can manage at home / reassurance
}

data class GuidanceContext(
    val isPregnant: Boolean = false,
    val gestationalWeek: Int? = null,
    val isPostpartum: Boolean = false,
    val postpartumWeeks: Int? = null,
    val childAgeMonths: Int? = null,
    val existingConditions: List<String> = emptyList()
)

data class ActionableGuidance(
    val tier: GuidanceTier,
    val headline: String,
    val urgency: Urgency,
    val explanation: String,
    val dangerSigns: List<String>,
    val immediateActions: List<String>,
    val whatNotToDo: List<String>,
    val possibleReasons: List<PossibleReason>,
    val homeCare: HomeCareGuidance?,
    val whenToSeekCare: String,
    val healthcareOptions: HealthcareOptions,
    val warningSignsToWatch: List<String> = emptyList(),
    val disclaimer: String
)

data class PossibleReason(
    val commonName: String,
    val explanation: String,
    val likelihood: String  // "Very Common", "Common", "Less Common"
)

data class DangerSignResult(
    val hasDangerSigns: Boolean,
    val dangerSigns: List<String>
)

enum class HomeCareScenario {
    FEVER_CHILD,
    DIARRHEA_CHILD,
    MORNING_SICKNESS,
    POSTPARTUM_MOOD,
    PERIOD_PAIN
}

data class HomeCareGuidance(
    val title: String,
    val source: String,
    val steps: List<HomeCareStep>,
    val warningSignsToReturn: List<String>,
    val whatThisMeans: String,
    val disclaimer: String
)

data class HomeCareStep(
    val instruction: String,
    val reason: String,
    val icon: String,
    val additionalInfo: String? = null
)

enum class EducationalTopic {
    UNDERSTANDING_FEVER,
    UNDERSTANDING_PREGNANCY_SYMPTOMS,
    UNDERSTANDING_MENTAL_HEALTH,
    UNDERSTANDING_CHILD_ILLNESS
}

data class EducationalContent(
    val title: String,
    val sections: List<EducationalSection>,
    val keyTakeaway: String
)

data class EducationalSection(
    val heading: String,
    val content: String
)

data class HealthcareOptions(
    val immediateOptions: List<HealthcareOption>,
    val helplines: List<Helpline>,
    val disclaimer: String
)

data class HealthcareOption(
    val type: String,
    val name: String,
    val description: String,
    val action: String,  // "CALL", "VISIT", "CONTACT", "GO"
    val contact: String? = null,
    val priority: Int
)

data class Helpline(
    val number: String,
    val name: String,
    val description: String
)
