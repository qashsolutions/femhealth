package com.maa.health.data.remote.guidance

import com.maa.health.data.model.Severity
import com.maa.health.data.model.SymptomType
import com.maa.health.data.model.Urgency
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for ActionableGuidanceService
 *
 * Tests the tiered guidance system for underserved communities:
 * - Danger sign detection (Red tier)
 * - Monitoring guidance (Yellow tier)
 * - Home care guidance (Green tier)
 * - WHO/IMCI validated recommendations
 */
class ActionableGuidanceServiceTest {

    private lateinit var service: ActionableGuidanceService

    @Before
    fun setup() {
        service = ActionableGuidanceService()
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // DANGER SIGN DETECTION TESTS (RED TIER)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `getGuidance returns RED tier for convulsions`() {
        // Given
        val symptoms = listOf(SymptomType.CONVULSIONS)
        val context = GuidanceContext(
            isPregnant = false,
            isPostpartum = false,
            hasChild = true,
            childAgeMonths = 12
        )

        // When
        val guidance = service.getGuidance(symptoms, Severity.SEVERE, context)

        // Then
        assertEquals(GuidanceTier.RED, guidance.tier)
        assertTrue(guidance.actionRequired)
        assertTrue(guidance.emergencyNumbers.contains("108"))
    }

    @Test
    fun `getGuidance returns RED tier for difficulty breathing in child`() {
        // Given
        val symptoms = listOf(SymptomType.DIFFICULTY_BREATHING)
        val context = GuidanceContext(
            isPregnant = false,
            isPostpartum = false,
            hasChild = true,
            childAgeMonths = 6
        )

        // When
        val guidance = service.getGuidance(symptoms, Severity.SEVERE, context)

        // Then
        assertEquals(GuidanceTier.RED, guidance.tier)
        assertNotNull(guidance.immediateActions)
        assertTrue(guidance.immediateActions!!.isNotEmpty())
    }

    @Test
    fun `getGuidance returns RED tier for pregnancy with severe bleeding`() {
        // Given
        val symptoms = listOf(SymptomType.HEAVY_BLEEDING)
        val context = GuidanceContext(
            isPregnant = true,
            isPostpartum = false,
            hasChild = false,
            pregnancyWeeks = 20
        )

        // When
        val guidance = service.getGuidance(symptoms, Severity.SEVERE, context)

        // Then
        assertEquals(GuidanceTier.RED, guidance.tier)
        assertTrue(guidance.emergencyNumbers.contains("108"))
    }

    @Test
    fun `getGuidance returns RED tier for not feeding in infant`() {
        // Given
        val symptoms = listOf(SymptomType.NOT_FEEDING)
        val context = GuidanceContext(
            isPregnant = false,
            isPostpartum = false,
            hasChild = true,
            childAgeMonths = 2
        )

        // When
        val guidance = service.getGuidance(symptoms, Severity.SEVERE, context)

        // Then
        assertEquals(GuidanceTier.RED, guidance.tier)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // MONITORING GUIDANCE TESTS (YELLOW TIER)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `getGuidance returns YELLOW tier for moderate fever`() {
        // Given
        val symptoms = listOf(SymptomType.FEVER)
        val context = GuidanceContext(
            isPregnant = false,
            isPostpartum = false,
            hasChild = true,
            childAgeMonths = 24
        )

        // When
        val guidance = service.getGuidance(symptoms, Severity.MODERATE, context)

        // Then
        assertEquals(GuidanceTier.YELLOW, guidance.tier)
        assertNotNull(guidance.monitoringAdvice)
        assertNotNull(guidance.whenToSeekCare)
    }

    @Test
    fun `getGuidance returns YELLOW tier for persistent symptoms`() {
        // Given
        val symptoms = listOf(SymptomType.HEADACHE, SymptomType.FATIGUE)
        val context = GuidanceContext(
            isPregnant = false,
            isPostpartum = true,
            hasChild = false,
            daysPostpartum = 14
        )

        // When
        val guidance = service.getGuidance(symptoms, Severity.MODERATE, context)

        // Then
        assertEquals(GuidanceTier.YELLOW, guidance.tier)
        assertTrue(guidance.suggestsASHAVisit)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HOME CARE GUIDANCE TESTS (GREEN TIER)
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `getGuidance returns GREEN tier for mild symptoms`() {
        // Given
        val symptoms = listOf(SymptomType.FATIGUE)
        val context = GuidanceContext(
            isPregnant = false,
            isPostpartum = false,
            hasChild = false
        )

        // When
        val guidance = service.getGuidance(symptoms, Severity.MILD, context)

        // Then
        assertEquals(GuidanceTier.GREEN, guidance.tier)
        assertNotNull(guidance.homeCareAdvice)
        assertFalse(guidance.actionRequired)
    }

    @Test
    fun `getGuidance provides home care steps for mild fever`() {
        // Given
        val symptoms = listOf(SymptomType.FEVER)
        val context = GuidanceContext(
            isPregnant = false,
            isPostpartum = false,
            hasChild = true,
            childAgeMonths = 36
        )

        // When
        val guidance = service.getGuidance(symptoms, Severity.MILD, context)

        // Then
        assertTrue(guidance.homeCareAdvice?.isNotEmpty() == true)
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // WHO/IMCI HOME CARE GUIDANCE TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `getHomeCareGuidance returns correct fever guidance`() {
        // When
        val guidance = service.getHomeCareGuidance(HomeCareScenario.FEVER_CHILD)

        // Then
        assertEquals("Managing Fever at Home", guidance.title)
        assertTrue(guidance.source.contains("WHO") || guidance.source.contains("IMCI"))
        assertTrue(guidance.steps.isNotEmpty())
        assertTrue(guidance.warningSignsToReturn.isNotEmpty())
    }

    @Test
    fun `getHomeCareGuidance returns correct diarrhea guidance`() {
        // When
        val guidance = service.getHomeCareGuidance(HomeCareScenario.DIARRHEA_CHILD)

        // Then
        assertTrue(guidance.steps.any { it.instruction.contains("ORS", ignoreCase = true) })
        assertTrue(guidance.steps.any { it.instruction.contains("fluid", ignoreCase = true) })
    }

    @Test
    fun `getHomeCareGuidance includes reasons for each step`() {
        // When
        val guidance = service.getHomeCareGuidance(HomeCareScenario.FEVER_CHILD)

        // Then
        assertTrue(guidance.steps.all { it.reason.isNotEmpty() })
    }

    @Test
    fun `getHomeCareGuidance includes warning signs`() {
        // When
        val guidance = service.getHomeCareGuidance(HomeCareScenario.COUGH_COLD)

        // Then
        assertTrue(guidance.warningSignsToReturn.isNotEmpty())
        assertTrue(guidance.warningSignsToReturn.any {
            it.contains("breathing", ignoreCase = true) ||
            it.contains("worse", ignoreCase = true)
        })
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EDUCATIONAL CONTENT TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `getEducationalContext returns content for pregnancy topics`() {
        // When
        val content = service.getEducationalContext(EducationalTopic.PREGNANCY_NUTRITION)

        // Then
        assertNotNull(content)
        assertTrue(content.title.isNotEmpty())
        assertTrue(content.keyPoints.isNotEmpty())
    }

    @Test
    fun `getEducationalContext uses simple language`() {
        // When
        val content = service.getEducationalContext(EducationalTopic.BREASTFEEDING)

        // Then
        // Check for simple language indicators
        assertFalse(content.explanation.contains("lactation", ignoreCase = true))
        assertTrue(content.explanation.length < 500)  // Not too long
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HEALTHCARE OPTIONS TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `getHealthcareOptions returns emergency numbers for urgent cases`() {
        // When
        val options = service.getHealthcareOptions(Urgency.EMERGENCY, null)

        // Then
        assertTrue(options.emergencyNumbers.contains("108"))
        assertTrue(options.emergencyNumbers.contains("104") || options.helplines.isNotEmpty())
    }

    @Test
    fun `getHealthcareOptions includes ASHA worker reference`() {
        // When
        val options = service.getHealthcareOptions(Urgency.SOON, null)

        // Then
        assertTrue(
            options.localResources.any { it.contains("ASHA", ignoreCase = true) } ||
            options.description.contains("ASHA", ignoreCase = true)
        )
    }

    @Test
    fun `getHealthcareOptions includes PHC for non-emergency`() {
        // When
        val options = service.getHealthcareOptions(Urgency.SOON, null)

        // Then
        assertTrue(
            options.localResources.any { it.contains("PHC", ignoreCase = true) } ||
            options.description.contains("health center", ignoreCase = true)
        )
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // EDGE CASE TESTS
    // ═══════════════════════════════════════════════════════════════════════════

    @Test
    fun `getGuidance handles empty symptom list`() {
        // Given
        val symptoms = emptyList<SymptomType>()
        val context = GuidanceContext(
            isPregnant = false,
            isPostpartum = false,
            hasChild = false
        )

        // When
        val guidance = service.getGuidance(symptoms, Severity.MILD, context)

        // Then
        assertEquals(GuidanceTier.GREEN, guidance.tier)
        assertFalse(guidance.actionRequired)
    }

    @Test
    fun `getGuidance handles multiple danger signs`() {
        // Given - multiple danger signs should still result in RED
        val symptoms = listOf(
            SymptomType.CONVULSIONS,
            SymptomType.DIFFICULTY_BREATHING,
            SymptomType.LETHARGY
        )
        val context = GuidanceContext(
            isPregnant = false,
            isPostpartum = false,
            hasChild = true,
            childAgeMonths = 3
        )

        // When
        val guidance = service.getGuidance(symptoms, Severity.SEVERE, context)

        // Then
        assertEquals(GuidanceTier.RED, guidance.tier)
        assertTrue(guidance.actionRequired)
    }

    @Test
    fun `getGuidance is conservative - escalates borderline cases`() {
        // Given - moderate severity with concerning context
        val symptoms = listOf(SymptomType.FEVER)
        val context = GuidanceContext(
            isPregnant = true,  // Pregnancy makes fever more concerning
            isPostpartum = false,
            hasChild = false,
            pregnancyWeeks = 30
        )

        // When
        val guidance = service.getGuidance(symptoms, Severity.MODERATE, context)

        // Then - should escalate to at least YELLOW for pregnancy
        assertTrue(guidance.tier == GuidanceTier.YELLOW || guidance.tier == GuidanceTier.RED)
    }
}
