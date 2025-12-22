# COMPLETE BUILD SPECIFICATION: Maa - Women's Lifetime Health Companion

## PROJECT OVERVIEW

Build a flagship-quality Android application for women's health across the entire lifecycle (ages 13-65+). The app uses agentic AI to provide clinical-grade health screening, symptom triage, and personalized guidance. The app learns from every interaction and becomes smarter over time.

**App Name:** Maa (माँ) - meaning "Mother" in Hindi
**Platform:** Android only (Kotlin + Jetpack Compose)
**Min SDK:** API 24 (Android 7.0)
**Target Market:** India (rural, semi-urban, urban women)

---

## DESIGN PHILOSOPHY

### Core Principles

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         DESIGN COMMANDMENTS                                 │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. VISUAL FIRST → VOICE SECOND → TEXT LAST                                │
│     • Primary interaction: Tap on visual elements (SVG body, icons)        │
│     • Secondary: Voice input/output via Sarvam AI                          │
│     • Tertiary: Text input only when absolutely necessary                  │
│                                                                             │
│  2. PREMIUM MEDICAL REFERENCE QUALITY                                       │
│     • Think: Medical textbook, not consumer health app                     │
│     • Think: Anatomy atlas, not cartoon illustration                       │
│     • Think: Clinical tool, not wellness tracker                           │
│                                                                             │
│  3. RESTRAINED AND SOPHISTICATED                                            │
│     • NO emojis anywhere in the app                                        │
│     • NO glows, shadows, gradients, or neon colors                         │
│     • NO rounded bubbly UI elements                                        │
│     • NO gamification badges or streaks                                    │
│     • Warm colors over cold (earth tones, not clinical blues)              │
│                                                                             │
│  4. NOTHING STATIC                                                          │
│     • Every feature learns from user data                                  │
│     • Every recommendation is personalized                                 │
│     • Every prediction improves over time                                  │
│     • The app gets smarter with every interaction                          │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## COLOR PALETTE

```kotlin
object MaaColors {
    // ═══════════════════════════════════════════════════════════════════════
    // BACKGROUNDS (Warm, not clinical white)
    // ═══════════════════════════════════════════════════════════════════════
    val background = Color(0xFFFAF7F2)        // Warm cream - primary background
    val surface = Color(0xFFF5F0EB)           // Warm bone - cards, panels
    val surfaceVariant = Color(0xFFEDE6DD)    // Warm linen - secondary surfaces
    val surfaceElevated = Color(0xFFFFFFFF)   // Pure white - modals only
    
    // ═══════════════════════════════════════════════════════════════════════
    // TEXT (Warm blacks and grays, never pure black)
    // ═══════════════════════════════════════════════════════════════════════
    val textPrimary = Color(0xFF2D2926)       // Warm charcoal - headings
    val textSecondary = Color(0xFF5C5550)     // Warm gray - body text
    val textTertiary = Color(0xFF8A837C)      // Warm silver - captions
    val textDisabled = Color(0xFFB8B2AB)      // Light warm gray
    
    // ═══════════════════════════════════════════════════════════════════════
    // SVG BODY ILLUSTRATION COLORS
    // ═══════════════════════════════════════════════════════════════════════
    val bodyFill = Color(0xFFF5EDE5)          // Warm skin tone - body fill
    val bodyStroke = Color(0xFF3D3632)        // Warm charcoal - outlines
    val bodyStrokeLight = Color(0xFF8A837C)   // Light stroke for details
    val organFill = Color(0xFFE8DFD4)         // Slightly darker for organs
    val organHighlight = Color(0xFFD4C4B0)    // Highlighted organ
    
    // ═══════════════════════════════════════════════════════════════════════
    // SEMANTIC COLORS (Warm versions of standard colors)
    // ═══════════════════════════════════════════════════════════════════════
    val safe = Color(0xFF4A7C59)              // Forest green - healthy/normal
    val safeLight = Color(0xFFE8F0EA)         // Light green background
    
    val info = Color(0xFF5B7B8C)              // Warm slate blue - informational
    val infoLight = Color(0xFFE8EEF1)         // Light blue background
    
    val caution = Color(0xFFD4A84B)           // Amber gold - needs attention
    val cautionLight = Color(0xFFF8F3E6)      // Light amber background
    
    val warning = Color(0xFFCC7B4A)           // Terracotta - concerning
    val warningLight = Color(0xFFFAF0EB)      // Light terracotta background
    
    val danger = Color(0xFFB54D4D)            // Brick red - urgent/emergency
    val dangerLight = Color(0xFFFAEBEB)       // Light red background
    
    // ═══════════════════════════════════════════════════════════════════════
    // ACCENT & INTERACTIVE
    // ═══════════════════════════════════════════════════════════════════════
    val accent = Color(0xFF8B6F5C)            // Warm taupe - primary accent
    val accentLight = Color(0xFFC4A98C)       // Sand - secondary accent
    val accentDark = Color(0xFF5C4A3D)        // Dark taupe - pressed states
    
    val touchFeedback = Color(0x1A8B6F5C)     // 10% taupe - ripple effect
    val selectedRegion = Color(0x33D4A84B)   // 20% amber - selected body region
    val activeRegion = Color(0x4D8B6F5C)      // 30% taupe - active/focused
    
    // ═══════════════════════════════════════════════════════════════════════
    // LIFECYCLE STAGE COLORS (Subtle differentiation)
    // ═══════════════════════════════════════════════════════════════════════
    val stageAdolescence = Color(0xFFE8D4C8)  // Soft peach
    val stageReproductive = Color(0xFFD4C4B0) // Warm sand
    val stagePregnancy = Color(0xFFE2D5C7)    // Cream rose
    val stagePostpartum = Color(0xFFD8CFC4)   // Warm dove
    val stageChildCare = Color(0xFFDCE4D8)    // Soft sage
    val stageMidlife = Color(0xFFD4D0C8)      // Warm stone
    val stageElder = Color(0xFFE0DCD6)        // Silver birch
}
```

---

## TYPOGRAPHY SYSTEM

```kotlin
object MaaTypography {
    // ═══════════════════════════════════════════════════════════════════════
    // FONT FAMILIES
    // ═══════════════════════════════════════════════════════════════════════
    
    // Headings: Serif font for dignity, trustworthiness, medical authority
    // Use: Source Serif Pro (Google Fonts, supports Devanagari)
    val headingFamily = FontFamily(
        Font(R.font.source_serif_pro_regular, FontWeight.Normal),
        Font(R.font.source_serif_pro_semibold, FontWeight.SemiBold),
        Font(R.font.source_serif_pro_bold, FontWeight.Bold)
    )
    
    // Body: Sans-serif for clarity, readability, modern feel
    // Use: Source Sans Pro (Google Fonts, supports Devanagari)
    val bodyFamily = FontFamily(
        Font(R.font.source_sans_pro_regular, FontWeight.Normal),
        Font(R.font.source_sans_pro_medium, FontWeight.Medium),
        Font(R.font.source_sans_pro_semibold, FontWeight.SemiBold)
    )
    
    // Data: Monospace for medical data, measurements, scores
    // Use: JetBrains Mono (Google Fonts)
    val dataFamily = FontFamily(
        Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
        Font(R.font.jetbrains_mono_medium, FontWeight.Medium)
    )
    
    // ═══════════════════════════════════════════════════════════════════════
    // TYPE SCALE
    // ═══════════════════════════════════════════════════════════════════════
    
    // Display - Major screens, welcome messages
    val displayLarge = TextStyle(
        fontFamily = headingFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    )
    
    // Headlines - Section titles, screen titles
    val headlineLarge = TextStyle(
        fontFamily = headingFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    )
    
    val headlineMedium = TextStyle(
        fontFamily = headingFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )
    
    val headlineSmall = TextStyle(
        fontFamily = headingFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    )
    
    // Titles - Card titles, list headers
    val titleLarge = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp
    )
    
    val titleMedium = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    
    val titleSmall = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    
    // Body - Main content
    val bodyLarge = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    
    val bodyMedium = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    
    val bodySmall = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
    
    // Labels - Buttons, chips, small UI elements
    val labelLarge = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )
    
    val labelMedium = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    
    val labelSmall = TextStyle(
        fontFamily = bodyFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 10.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.5.sp
    )
    
    // Data - Medical values, scores, measurements
    val dataLarge = TextStyle(
        fontFamily = dataFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )
    
    val dataMedium = TextStyle(
        fontFamily = dataFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    
    val dataSmall = TextStyle(
        fontFamily = dataFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
}
```

---

## SVG SPECIFICATIONS

### Critical SVG Implementation Rules

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * SVG IMPLEMENTATION GUIDELINES
 * ════════════════════════════════════════════════════════════════════════════
 * 
 * Reference: Anthropic's Interactive Anatomy Explorer
 * 
 * 1. EMBED SVGs DIRECTLY INTO COMPOSABLES
 *    - Do NOT use fetch requests
 *    - Do NOT load from network
 *    - Embed SVG paths directly in Kotlin/Compose code
 *    - Use vector drawables or custom Canvas drawing
 * 
 * 2. SEMANTIC IDs ARE CRITICAL
 *    - Every tappable region MUST have a unique ID
 *    - IDs follow anatomical/functional naming
 *    - IDs are used for targeting, highlighting, and event handling
 *    - NEVER strip IDs during optimization
 * 
 * 3. DEFAULT VISIBILITY HANDLING
 *    - SVG elements default to fill:none, stroke:none (invisible)
 *    - After loading, IMMEDIATELY apply default fills and strokes
 *    - Apply styling BEFORE any user interaction
 *    - Remove any visibility:hidden attributes programmatically
 * 
 * 4. STYLE SPECIFICATIONS
 *    - Stroke width: 1.0-1.5dp for outlines
 *    - Stroke color: MaaColors.bodyStroke (#3D3632)
 *    - Fill color: MaaColors.bodyFill (#F5EDE5)
 *    - NO gradients, NO drop shadows, NO glow effects
 *    - Clean, precise, medical-illustration quality
 */
```

### Required SVG Assets

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            SVG ASSET MANIFEST                               │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  1. BODY ILLUSTRATIONS (Full-figure, tappable regions)                      │
│  ══════════════════════════════════════════════════════                     │
│                                                                             │
│  body_adolescent.svg                                                        │
│  ├── Adolescent female figure (13-19 years)                                │
│  ├── Style: Modest, age-appropriate, anatomically educational              │
│  ├── Tappable regions:                                                     │
│  │   ├── #head (mental health, headaches)                                  │
│  │   ├── #chest (breast development)                                       │
│  │   ├── #abdomen (menstrual, digestive)                                   │
│  │   ├── #pelvis (reproductive system)                                     │
│  │   └── #skin (full body - skin concerns)                                 │
│  └── Overlays: puberty stage indicators                                    │
│                                                                             │
│  body_adult_female.svg                                                      │
│  ├── Adult female figure (20-40 years, non-pregnant)                       │
│  ├── Style: Anatomically accurate, dignified                               │
│  ├── Tappable regions:                                                     │
│  │   ├── #head (mental health, migraines)                                  │
│  │   ├── #chest (breast health)                                            │
│  │   ├── #abdomen (digestive, reproductive)                                │
│  │   ├── #pelvis (reproductive system, urinary)                            │
│  │   ├── #hands (joint health)                                             │
│  │   └── #feet (circulation)                                               │
│  └── Overlays: cycle phase indicator                                       │
│                                                                             │
│  body_pregnant.svg                                                          │
│  ├── Pregnant female figure (gestational weeks 4-40)                       │
│  ├── Style: Shows pregnancy progression, anatomically accurate             │
│  ├── Tappable regions:                                                     │
│  │   ├── #head (headaches, vision, mental health)                          │
│  │   ├── #eyes (vision changes)                                            │
│  │   ├── #chest (breathing, breast changes)                                │
│  │   ├── #abdomen_upper (heartburn, breathing)                             │
│  │   ├── #uterus (baby, contractions, pain)                                │
│  │   ├── #pelvis (cervix, discharge, bleeding)                             │
│  │   ├── #hands (swelling, carpal tunnel)                                  │
│  │   └── #feet (swelling, circulation)                                     │
│  ├── Overlays:                                                             │
│  │   ├── fetus position (by week)                                          │
│  │   ├── uterus growth outline (by week)                                   │
│  │   └── danger zone highlighting                                          │
│  └── Dynamic: Updates based on gestational week                            │
│                                                                             │
│  body_postpartum.svg                                                        │
│  ├── Postpartum female figure                                              │
│  ├── Tappable regions:                                                     │
│  │   ├── #head (mental health, fatigue)                                    │
│  │   ├── #chest (breastfeeding, engorgement)                               │
│  │   ├── #abdomen (uterus involution, diastasis)                           │
│  │   ├── #pelvis (bleeding, healing, pelvic floor)                         │
│  │   └── #incision (if C-section - wound care)                             │
│  └── Overlays: recovery timeline indicators                                │
│                                                                             │
│  body_child.svg (0-5 years)                                                 │
│  ├── Child figure (age-morphing: infant → toddler → preschool)             │
│  ├── Style: Proportionally accurate for age                                │
│  ├── Tappable regions:                                                     │
│  │   ├── #head (fever, fontanelle for infants, ear)                        │
│  │   ├── #eyes (jaundice, redness)                                         │
│  │   ├── #mouth (thrush, teeth for older)                                  │
│  │   ├── #chest (breathing, cough)                                         │
│  │   ├── #abdomen (feeding, diarrhea, vomiting)                            │
│  │   ├── #skin (rash, pallor)                                              │
│  │   └── #diaper_area (rash, for infants)                                  │
│  └── Dynamic: Morphs based on child's current age                          │
│                                                                             │
│  body_midlife.svg                                                           │
│  ├── Midlife female figure (40-55 years)                                   │
│  ├── Tappable regions:                                                     │
│  │   ├── #head (hot flashes, mood, memory)                                 │
│  │   ├── #chest (breast screening, heart)                                  │
│  │   ├── #abdomen (weight, digestive)                                      │
│  │   ├── #pelvis (menopause symptoms, cervical)                            │
│  │   ├── #bones (joints, osteoporosis)                                     │
│  │   └── #heart_region (cardiovascular)                                    │
│  └── Overlays: menopause phase indicator                                   │
│                                                                             │
│  body_elder.svg                                                             │
│  ├── Elder female figure (55+ years)                                       │
│  ├── Tappable regions:                                                     │
│  │   ├── #head (cognitive, vision, hearing)                                │
│  │   ├── #heart (cardiovascular)                                           │
│  │   ├── #lungs (respiratory)                                              │
│  │   ├── #abdomen (digestive, diabetes)                                    │
│  │   ├── #joints (arthritis)                                               │
│  │   └── #feet (circulation, neuropathy)                                   │
│  └── Overlays: chronic condition indicators                                │
│                                                                             │
│  ─────────────────────────────────────────────────────────────────────────  │
│                                                                             │
│  2. ORGAN/SYSTEM DETAIL SVGs                                                │
│  ══════════════════════════════                                             │
│                                                                             │
│  reproductive_system.svg                                                    │
│  ├── Female reproductive anatomy (educational)                             │
│  ├── Regions: #uterus, #ovaries, #fallopian_tubes, #cervix, #vagina       │
│  └── Used for: body literacy, cycle education                             │
│                                                                             │
│  fetus_development/                                                         │
│  ├── fetus_week_04.svg through fetus_week_40.svg                           │
│  ├── Accurate fetal illustration per gestational week                      │
│  ├── Shows: size, proportions, visible development                        │
│  └── Size reference: actual size or fruit/object comparison               │
│                                                                             │
│  breast_anatomy.svg                                                         │
│  ├── Breast cross-section (educational)                                   │
│  ├── Regions: #milk_ducts, #nipple, #areola, #glandular_tissue            │
│  └── Used for: breastfeeding education, self-exam guidance                │
│                                                                             │
│  chest_breathing.svg                                                        │
│  ├── Chest/lung illustration for breathing assessment                     │
│  ├── Shows: normal breathing vs chest indrawing                           │
│  ├── Side-by-side comparison view                                         │
│  └── Used for: child pneumonia triage                                     │
│                                                                             │
│  dehydration_assessment.svg                                                 │
│  ├── Visual guide for dehydration signs                                   │
│  ├── Shows: eyes (normal vs sunken), skin pinch test, mouth               │
│  └── Used for: child diarrhea triage                                      │
│                                                                             │
│  ─────────────────────────────────────────────────────────────────────────  │
│                                                                             │
│  3. ICON SETS (Professional, minimal line art)                              │
│  ════════════════════════════════════════════                               │
│                                                                             │
│  icons_mood.svg                                                             │
│  ├── 5-point mood scale                                                    │
│  ├── Style: Abstract, dignified (NOT cartoon faces, NOT emojis)           │
│  ├── Think: Minimalist line art suggesting emotional state                │
│  ├── Examples:                                                             │
│  │   ├── mood_1: Serene - calm flowing lines                              │
│  │   ├── mood_2: Content - gentle balanced form                           │
│  │   ├── mood_3: Neutral - simple centered shape                          │
│  │   ├── mood_4: Distressed - slightly disrupted form                     │
│  │   └── mood_5: Crisis - fragmented, needs attention                     │
│  └── Each icon: 48x48dp viewBox                                            │
│                                                                             │
│  icons_symptoms.svg (Sprite sheet or individual)                            │
│  ├── 32 symptom icons, medical illustration style                         │
│  ├── Examples:                                                             │
│  │   ├── fever (thermometer)                                              │
│  │   ├── cough (chest with motion lines)                                  │
│  │   ├── breathing_fast (lungs with rate indicator)                       │
│  │   ├── diarrhea (stylized intestine)                                    │
│  │   ├── bleeding (droplet - not red, use stroke only)                    │
│  │   ├── headache (head with pain indicator)                              │
│  │   ├── swelling (limb with puffiness)                                   │
│  │   ├── vision_blurred (eye with blur)                                   │
│  │   ├── nausea (stomach with wave)                                       │
│  │   ├── fatigue (figure in rest)                                         │
│  │   └── ... (complete set for all tracked symptoms)                      │
│  └── Each icon: 48x48dp viewBox, 1.5dp stroke, no fill                    │
│                                                                             │
│  icons_actions.svg                                                          │
│  ├── Action/navigation icons                                              │
│  ├── Examples:                                                             │
│  │   ├── voice_input (microphone, minimal)                                │
│  │   ├── emergency (alert symbol)                                         │
│  │   ├── schedule (calendar, simple)                                      │
│  │   ├── track (chart line)                                               │
│  │   ├── learn (book/page)                                                │
│  │   ├── call_doctor (phone with medical cross)                           │
│  │   └── settings (gear, simple)                                          │
│  └── Each icon: 24x24dp viewBox                                            │
│                                                                             │
│  icons_lifecycle.svg                                                        │
│  ├── Lifecycle stage icons for navigation                                 │
│  ├── Style: Silhouette progression                                        │
│  └── One icon per stage (adolescent → elder)                              │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### SVG Implementation Code Pattern

```kotlin
/**
 * Interactive Body SVG Component
 * 
 * Implements the core body-map navigation interface.
 * Users tap anatomical regions to access related features.
 */
@Composable
fun InteractiveBodySvg(
    modifier: Modifier = Modifier,
    bodyType: BodyType,  // ADOLESCENT, ADULT, PREGNANT, etc.
    gestationalWeek: Int? = null,  // For pregnant body
    childAgeMonths: Int? = null,   // For child body
    selectedRegions: Set<String> = emptySet(),
    highlightedRegions: Map<String, HighlightState> = emptyMap(),
    onRegionTap: (regionId: String) -> Unit
) {
    // Body SVG paths embedded directly (example for pregnant body)
    val bodyPaths = remember(bodyType, gestationalWeek) {
        when (bodyType) {
            BodyType.PREGNANT -> PregnantBodyPaths.forWeek(gestationalWeek ?: 20)
            BodyType.CHILD -> ChildBodyPaths.forAge(childAgeMonths ?: 0)
            // ... other body types
        }
    }
    
    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    // Hit test against region paths
                    val tappedRegion = bodyPaths.hitTest(offset)
                    tappedRegion?.let { 
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onRegionTap(it) 
                    }
                }
            }
    ) {
        // Draw base body
        bodyPaths.regions.forEach { (regionId, path) ->
            val fillColor = when {
                regionId in selectedRegions -> MaaColors.selectedRegion
                highlightedRegions[regionId] == HighlightState.DANGER -> MaaColors.danger.copy(alpha = 0.3f)
                highlightedRegions[regionId] == HighlightState.WARNING -> MaaColors.warning.copy(alpha = 0.3f)
                highlightedRegions[regionId] == HighlightState.SAFE -> MaaColors.safe.copy(alpha = 0.2f)
                else -> MaaColors.bodyFill
            }
            
            drawPath(
                path = path,
                color = fillColor,
                style = Fill
            )
            drawPath(
                path = path,
                color = MaaColors.bodyStroke,
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
        
        // Draw overlays (fetus, uterus growth, etc.)
        bodyPaths.overlays.forEach { overlay ->
            drawPath(
                path = overlay.path,
                color = overlay.color,
                style = overlay.style
            )
        }
    }
}

enum class HighlightState {
    NORMAL,
    SELECTED,
    SAFE,
    CAUTION,
    WARNING,
    DANGER
}
```

---

## HAPTIC & SOUND FEEDBACK

```kotlin
/**
 * Haptic and Sound Feedback System
 * 
 * All interactions have physical-feeling feedback.
 * Sound is subtle, medical-instrument quality.
 * NOT notification sounds, NOT game sounds.
 */
object MaaFeedback {
    
    // ═══════════════════════════════════════════════════════════════════════
    // HAPTIC FEEDBACK
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Light tap - for button presses, toggles
     */
    fun onTap(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }
    
    /**
     * Medium - for selection confirmation, body region tap
     */
    fun onSelect(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
    
    /**
     * Strong - for warnings, important state changes
     */
    fun onWarning(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.REJECT)
        } else {
            view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }
    
    /**
     * Double pulse - for danger/emergency
     */
    fun onDanger(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(
                VibrationEffect.createWaveform(
                    longArrayOf(0, 100, 100, 100),
                    intArrayOf(0, 255, 0, 255),
                    -1
                )
            )
        }
    }
    
    /**
     * Success pulse - for completion, positive outcomes
     */
    fun onSuccess(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // SOUND FEEDBACK
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Sound pool for UI sounds
     * Sounds should be:
     * - Short (50-300ms)
     * - Subtle, not startling
     * - Medical/clinical quality (like pulse oximeter beeps)
     * - NOT notification sounds
     * - NOT game sounds
     */
    
    // Sound files (to be created):
    // - soft_tap.ogg: 50ms, very subtle click
    // - gentle_confirm.ogg: 200ms, warm completion tone
    // - alert_soft.ogg: 300ms, attention without alarm
    // - alert_urgent.ogg: 400ms, serious but not panic-inducing
    
    private var soundPool: SoundPool? = null
    private var soundTap: Int = 0
    private var soundConfirm: Int = 0
    private var soundAlert: Int = 0
    private var soundUrgent: Int = 0
    
    fun initialize(context: Context) {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        
        soundPool = SoundPool.Builder()
            .setMaxStreams(3)
            .setAudioAttributes(audioAttributes)
            .build()
        
        soundPool?.let { pool ->
            soundTap = pool.load(context, R.raw.soft_tap, 1)
            soundConfirm = pool.load(context, R.raw.gentle_confirm, 1)
            soundAlert = pool.load(context, R.raw.alert_soft, 1)
            soundUrgent = pool.load(context, R.raw.alert_urgent, 1)
        }
    }
    
    fun playTap() {
        soundPool?.play(soundTap, 0.3f, 0.3f, 1, 0, 1.0f)
    }
    
    fun playConfirm() {
        soundPool?.play(soundConfirm, 0.5f, 0.5f, 1, 0, 1.0f)
    }
    
    fun playAlert() {
        soundPool?.play(soundAlert, 0.7f, 0.7f, 1, 0, 1.0f)
    }
    
    fun playUrgent() {
        soundPool?.play(soundUrgent, 1.0f, 1.0f, 1, 0, 1.0f)
    }
}
```

---

## TECHNICAL ARCHITECTURE

### Tech Stack

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                           TECHNOLOGY STACK                                  │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  FRONTEND                                                                   │
│  ════════                                                                   │
│  • Language: Kotlin 1.9+                                                   │
│  • UI Framework: Jetpack Compose (Material 3)                              │
│  • Navigation: Compose Navigation                                          │
│  • State Management: ViewModel + StateFlow + Compose State                 │
│  • Dependency Injection: Hilt                                              │
│  • Image Loading: Coil                                                     │
│  • SVG Rendering: Custom Canvas + AndroidX Vector Drawable                 │
│                                                                             │
│  BACKEND / DATA                                                             │
│  ══════════════                                                             │
│  • Cloud Backend: Firebase                                                 │
│  │  ├── Authentication: Firebase Auth (Phone OTP)                          │
│  │  ├── Database: Cloud Firestore                                          │
│  │  ├── Functions: Cloud Functions (for AI orchestration)                  │
│  │  ├── Storage: Cloud Storage (for voice recordings, images)              │
│  │  └── Analytics: Firebase Analytics                                      │
│  │                                                                          │
│  • Local Database: Room (SQLite)                                           │
│  • Local Preferences: DataStore                                            │
│  • Sync: WorkManager for background sync                                   │
│                                                                             │
│  AI / VOICE                                                                 │
│  ══════════                                                                 │
│  • Voice I/O: Sarvam AI API                                                │
│  │  ├── Speech-to-Text (10 Indian languages)                               │
│  │  ├── Text-to-Speech (10 Indian languages)                               │
│  │  └── Cost: ₹1/minute                                                    │
│  │                                                                          │
│  • Medical AI: Google MedGemma                                             │
│  │  ├── MedGemma 4B (on-device for offline, multimodal)                   │
│  │  ├── MedGemma 27B (cloud via Vertex AI for complex reasoning)          │
│  │  └── Use: Clinical reasoning, symptom analysis, risk scoring           │
│  │                                                                          │
│  • Agent Orchestration: LangChain / Custom                                 │
│                                                                             │
│  PAYMENTS                                                                   │
│  ════════                                                                   │
│  • Provider: Razorpay                                                      │
│  • Methods: UPI (GPay, PhonePe, Paytm, BHIM), Cards, Netbanking            │
│  • Subscription: Razorpay Subscriptions API                                │
│                                                                             │
│  BIOMETRIC                                                                  │
│  ═════════                                                                  │
│  • Face ID / Fingerprint: AndroidX Biometric                               │
│  • Fallback: Phone OTP                                                     │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Project Structure

```
app/
├── src/main/
│   ├── java/com/maa/health/
│   │   │
│   │   ├── MaaApplication.kt
│   │   │
│   │   ├── di/                          # Dependency Injection
│   │   │   ├── AppModule.kt
│   │   │   ├── NetworkModule.kt
│   │   │   ├── DatabaseModule.kt
│   │   │   └── AIModule.kt
│   │   │
│   │   ├── data/
│   │   │   ├── local/
│   │   │   │   ├── database/
│   │   │   │   │   ├── MaaDatabase.kt
│   │   │   │   │   ├── dao/
│   │   │   │   │   │   ├── UserDao.kt
│   │   │   │   │   │   ├── CycleDao.kt
│   │   │   │   │   │   ├── MoodDao.kt
│   │   │   │   │   │   ├── SymptomDao.kt
│   │   │   │   │   │   ├── PregnancyDao.kt
│   │   │   │   │   │   ├── ChildDao.kt
│   │   │   │   │   │   └── MedicationDao.kt
│   │   │   │   │   └── entity/
│   │   │   │   │       ├── UserEntity.kt
│   │   │   │   │       ├── CycleEntity.kt
│   │   │   │   │       ├── MoodLogEntity.kt
│   │   │   │   │       └── ... (all data entities)
│   │   │   │   └── datastore/
│   │   │   │       ├── UserPreferences.kt
│   │   │   │       └── SettingsPreferences.kt
│   │   │   │
│   │   │   ├── remote/
│   │   │   │   ├── firebase/
│   │   │   │   │   ├── FirestoreService.kt
│   │   │   │   │   ├── AuthService.kt
│   │   │   │   │   └── StorageService.kt
│   │   │   │   ├── sarvam/
│   │   │   │   │   ├── SarvamApiService.kt
│   │   │   │   │   ├── SpeechToTextService.kt
│   │   │   │   │   └── TextToSpeechService.kt
│   │   │   │   ├── medgemma/
│   │   │   │   │   ├── MedGemmaService.kt
│   │   │   │   │   ├── MedGemmaLocalInference.kt
│   │   │   │   │   └── MedGemmaCloudService.kt
│   │   │   │   └── razorpay/
│   │   │   │       └── PaymentService.kt
│   │   │   │
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.kt
│   │   │   │   ├── HealthRepository.kt
│   │   │   │   ├── CycleRepository.kt
│   │   │   │   ├── PregnancyRepository.kt
│   │   │   │   ├── ChildRepository.kt
│   │   │   │   └── ScreeningRepository.kt
│   │   │   │
│   │   │   └── model/                   # Domain models
│   │   │       ├── User.kt
│   │   │       ├── LifecycleStage.kt
│   │   │       ├── Cycle.kt
│   │   │       ├── Pregnancy.kt
│   │   │       ├── Child.kt
│   │   │       ├── MoodLog.kt
│   │   │       ├── SymptomLog.kt
│   │   │       ├── ScreeningResult.kt
│   │   │       └── ... (all domain models)
│   │   │
│   │   ├── domain/
│   │   │   ├── usecase/
│   │   │   │   ├── auth/
│   │   │   │   │   ├── SendOtpUseCase.kt
│   │   │   │   │   ├── VerifyOtpUseCase.kt
│   │   │   │   │   └── BiometricAuthUseCase.kt
│   │   │   │   ├── screening/
│   │   │   │   │   ├── ConductEpdsUseCase.kt
│   │   │   │   │   ├── ConductPhq9UseCase.kt
│   │   │   │   │   ├── AssessDangerSignsUseCase.kt
│   │   │   │   │   └── TriageChildSymptomUseCase.kt
│   │   │   │   ├── tracking/
│   │   │   │   │   ├── LogCycleUseCase.kt
│   │   │   │   │   ├── LogMoodUseCase.kt
│   │   │   │   │   ├── LogSymptomUseCase.kt
│   │   │   │   │   └── CountKicksUseCase.kt
│   │   │   │   └── prediction/
│   │   │   │       ├── PredictPeriodUseCase.kt
│   │   │   │       ├── PredictOvulationUseCase.kt
│   │   │   │       └── AssessRiskUseCase.kt
│   │   │   │
│   │   │   └── agent/                   # AI Agent Architecture
│   │   │       ├── AgentOrchestrator.kt
│   │   │       ├── ScreeningAgent.kt
│   │   │       ├── TriageAgent.kt
│   │   │       ├── EducationAgent.kt
│   │   │       ├── ActionAgent.kt
│   │   │       └── LearningAgent.kt
│   │   │
│   │   ├── ui/
│   │   │   ├── theme/
│   │   │   │   ├── Color.kt
│   │   │   │   ├── Typography.kt
│   │   │   │   ├── Shape.kt
│   │   │   │   └── Theme.kt
│   │   │   │
│   │   │   ├── components/
│   │   │   │   ├── body/
│   │   │   │   │   ├── InteractiveBodySvg.kt
│   │   │   │   │   ├── BodyRegion.kt
│   │   │   │   │   ├── PregnantBodyPaths.kt
│   │   │   │   │   ├── ChildBodyPaths.kt
│   │   │   │   │   └── ... (all body SVG components)
│   │   │   │   ├── icons/
│   │   │   │   │   ├── MoodIcon.kt
│   │   │   │   │   ├── SymptomIcon.kt
│   │   │   │   │   └── ActionIcon.kt
│   │   │   │   ├── input/
│   │   │   │   │   ├── VoiceInputButton.kt
│   │   │   │   │   ├── VisualOptionGrid.kt
│   │   │   │   │   ├── SeveritySlider.kt
│   │   │   │   │   └── TimePicker.kt
│   │   │   │   ├── cards/
│   │   │   │   │   ├── InfoCard.kt
│   │   │   │   │   ├── AlertCard.kt
│   │   │   │   │   ├── ScreeningResultCard.kt
│   │   │   │   │   └── ActionCard.kt
│   │   │   │   ├── feedback/
│   │   │   │   │   └── MaaFeedback.kt
│   │   │   │   └── common/
│   │   │   │       ├── MaaButton.kt
│   │   │   │       ├── MaaTopBar.kt
│   │   │   │       ├── MaaBottomNav.kt
│   │   │   │       └── MaaDialog.kt
│   │   │   │
│   │   │   ├── navigation/
│   │   │   │   ├── MaaNavGraph.kt
│   │   │   │   ├── Screen.kt
│   │   │   │   └── NavActions.kt
│   │   │   │
│   │   │   └── screens/
│   │   │       ├── onboarding/
│   │   │       │   ├── LanguageSelectionScreen.kt
│   │   │       │   ├── PhoneAuthScreen.kt
│   │   │       │   ├── BiometricSetupScreen.kt
│   │   │       │   ├── ProfileSetupScreen.kt
│   │   │       │   └── OnboardingViewModel.kt
│   │   │       │
│   │   │       ├── home/
│   │   │       │   ├── HomeScreen.kt
│   │   │       │   ├── BodyMapSection.kt
│   │   │       │   ├── QuickActionsSection.kt
│   │   │       │   ├── UpcomingSection.kt
│   │   │       │   └── HomeViewModel.kt
│   │   │       │
│   │   │       ├── screening/
│   │   │       │   ├── mental/
│   │   │       │   │   ├── EpdsScreen.kt
│   │   │       │   │   ├── Phq9Screen.kt
│   │   │       │   │   ├── MoodCheckScreen.kt
│   │   │       │   │   └── MentalHealthViewModel.kt
│   │   │       │   ├── danger/
│   │   │       │   │   ├── DangerSignsScreen.kt
│   │   │       │   │   ├── DangerSignsResultScreen.kt
│   │   │       │   │   └── DangerSignsViewModel.kt
│   │   │       │   └── child/
│   │   │       │       ├── ChildTriageScreen.kt
│   │   │       │       ├── BreathingAssessmentScreen.kt
│   │   │       │       ├── DehydrationAssessmentScreen.kt
│   │   │       │       └── ChildTriageViewModel.kt
│   │   │       │
│   │   │       ├── tracking/
│   │   │       │   ├── cycle/
│   │   │       │   │   ├── CycleTrackingScreen.kt
│   │   │       │   │   ├── LogPeriodScreen.kt
│   │   │       │   │   └── CycleViewModel.kt
│   │   │       │   ├── pregnancy/
│   │   │       │   │   ├── PregnancyHomeScreen.kt
│   │   │       │   │   ├── KickCounterScreen.kt
│   │   │       │   │   ├── WeekByWeekScreen.kt
│   │   │       │   │   └── PregnancyViewModel.kt
│   │   │       │   ├── child/
│   │   │       │   │   ├── ChildHomeScreen.kt
│   │   │       │   │   ├── GrowthTrackingScreen.kt
│   │   │       │   │   ├── VaccinationScreen.kt
│   │   │       │   │   ├── MilestoneScreen.kt
│   │   │       │   │   └── ChildViewModel.kt
│   │   │       │   └── symptoms/
│   │   │       │       ├── SymptomLogScreen.kt
│   │   │       │       └── SymptomViewModel.kt
│   │   │       │
│   │   │       ├── education/
│   │   │       │   ├── BodyLiteracyScreen.kt
│   │   │       │   ├── TopicDetailScreen.kt
│   │   │       │   └── EducationViewModel.kt
│   │   │       │
│   │   │       ├── settings/
│   │   │       │   ├── SettingsScreen.kt
│   │   │       │   ├── ProfileScreen.kt
│   │   │       │   ├── LanguageScreen.kt
│   │   │       │   ├── SubscriptionScreen.kt
│   │   │       │   └── SettingsViewModel.kt
│   │   │       │
│   │   │       └── emergency/
│   │   │           ├── EmergencyScreen.kt
│   │   │           └── EmergencyViewModel.kt
│   │   │
│   │   └── util/
│   │       ├── DateUtils.kt
│   │       ├── ValidationUtils.kt
│   │       ├── PermissionUtils.kt
│   │       └── Extensions.kt
│   │
│   └── res/
│       ├── drawable/
│       │   ├── body_pregnant.xml        # Vector drawable (or raw SVG)
│       │   ├── body_child.xml
│       │   ├── ic_mood_1.xml
│       │   └── ... (all icons)
│       ├── font/
│       │   ├── source_serif_pro_regular.ttf
│       │   ├── source_serif_pro_semibold.ttf
│       │   ├── source_sans_pro_regular.ttf
│       │   ├── source_sans_pro_medium.ttf
│       │   └── jetbrains_mono_regular.ttf
│       ├── raw/
│       │   ├── soft_tap.ogg
│       │   ├── gentle_confirm.ogg
│       │   ├── alert_soft.ogg
│       │   └── alert_urgent.ogg
│       ├── values/
│       │   ├── strings.xml              # English
│       │   ├── strings_hi.xml           # Hindi
│       │   ├── strings_ta.xml           # Tamil
│       │   ├── strings_te.xml           # Telugu
│       │   ├── strings_bn.xml           # Bengali
│       │   └── strings_mr.xml           # Marathi
│       └── values-night/
│           └── colors.xml               # Dark theme (if applicable)
```

---

## AUTHENTICATION SYSTEM

### Phone OTP + Biometric Flow

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * AUTHENTICATION FLOWS
 * ════════════════════════════════════════════════════════════════════════════
 */

/**
 * FIRST-TIME USER FLOW
 * 
 * 1. Language Selection Screen
 *    - Visual grid of 10 language options
 *    - Each shows: Flag icon + Native script name
 *    - Example: 🇮🇳 हिन्दी, 🇮🇳 தமிழ், 🇮🇳 తెలుగు
 *    - Tap to select, no text input required
 * 
 * 2. Phone Number Screen
 *    - Large number pad (visual input)
 *    - +91 prefix pre-filled
 *    - Voice input option: "बोलकर नंबर बताएं"
 *    - Proceed button (disabled until 10 digits)
 * 
 * 3. OTP Verification Screen
 *    - 6-digit OTP input (large boxes)
 *    - Auto-read from SMS (with permission)
 *    - Resend option after 30 seconds
 *    - Voice: OTP read aloud if user requests
 * 
 * 4. Biometric Setup Screen (Optional)
 *    - "Make login faster with Face ID / Fingerprint?"
 *    - Visual demonstration of biometric
 *    - Yes / Skip options
 *    - If Yes: AndroidX Biometric enrollment
 * 
 * 5. Profile Setup Screen (Visual Selection)
 *    - "I am..." with visual options:
 *      ┌─────────┐  ┌─────────┐  ┌─────────┐
 *      │  [SVG]  │  │  [SVG]  │  │  [SVG]  │
 *      │ Teenager│  │ Pregnant│  │ Mother  │
 *      └─────────┘  └─────────┘  └─────────┘
 *    - If Pregnant: Visual week selector (slider with baby size)
 *    - If Mother: Child DOB picker
 *    - If Teenager: Age selection (13-19)
 */

/**
 * RETURNING USER FLOW
 * 
 * Option A: Biometric (if enrolled)
 *    - App opens → Biometric prompt immediately
 *    - Success → Home screen
 *    - Failure → Fallback to OTP
 * 
 * Option B: Phone + OTP (if biometric not enrolled or fails)
 *    - Phone number (remembered, just confirm)
 *    - OTP verification
 *    - Home screen
 * 
 * Session Duration: 30 days
 * Re-auth Required For:
 *    - Payments
 *    - Health record export
 *    - Profile changes
 *    - Adding family members
 */

// ════════════════════════════════════════════════════════════════════════════
// IMPLEMENTATION
// ════════════════════════════════════════════════════════════════════════════

@Composable
fun PhoneAuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
            .padding(24.dp)
    ) {
        // Header
        Text(
            text = stringResource(R.string.enter_phone),  // "अपना फ़ोन नंबर दर्ज करें"
            style = MaaTypography.headlineMedium,
            color = MaaColors.textPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.phone_subtitle),
            style = MaaTypography.bodyMedium,
            color = MaaColors.textSecondary
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Phone number display
        PhoneNumberDisplay(
            phoneNumber = uiState.phoneNumber,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Visual number pad (no keyboard)
        NumberPad(
            onDigitClick = { digit ->
                MaaFeedback.playTap()
                viewModel.onDigitEntered(digit)
            },
            onBackspace = {
                MaaFeedback.playTap()
                viewModel.onBackspace()
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Voice input option
        VoiceInputButton(
            label = stringResource(R.string.speak_number),  // "बोलकर नंबर बताएं"
            onVoiceResult = { spokenNumber ->
                viewModel.onVoiceInput(spokenNumber)
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Proceed button
        MaaButton(
            text = stringResource(R.string.proceed),
            enabled = uiState.phoneNumber.length == 10,
            onClick = {
                MaaFeedback.onSelect(LocalView.current)
                viewModel.sendOtp()
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BiometricSetupScreen(
    onComplete: (Boolean) -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        // Biometric illustration (SVG)
        BiometricIllustration(
            modifier = Modifier.size(200.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = stringResource(R.string.setup_biometric_title),
            style = MaaTypography.headlineMedium,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = stringResource(R.string.setup_biometric_subtitle),
            style = MaaTypography.bodyMedium,
            color = MaaColors.textSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Enable button
        MaaButton(
            text = stringResource(R.string.enable_biometric),
            onClick = {
                MaaFeedback.onSelect(context.findActivity()?.window?.decorView!!)
                // Trigger biometric enrollment
                BiometricManager.from(context).let { manager ->
                    if (manager.canAuthenticate(BIOMETRIC_STRONG) == BIOMETRIC_SUCCESS) {
                        onComplete(true)
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Skip button
        TextButton(
            onClick = { onComplete(false) }
        ) {
            Text(
                text = stringResource(R.string.skip_for_now),
                style = MaaTypography.labelLarge,
                color = MaaColors.textSecondary
            )
        }
    }
}
```

---

## PAYMENT SYSTEM (UPI)

### Subscription Model

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * SUBSCRIPTION & PAYMENT
 * ════════════════════════════════════════════════════════════════════════════
 */

/**
 * PRICING TIERS
 * 
 * FREE TIER (Always Available):
 * ─────────────────────────────
 * • Danger signs assessment (unlimited - safety critical)
 * • 1 mental health screening per month (EPDS or PHQ-9)
 * • 3 child symptom triages per month
 * • Basic pregnancy week-by-week info
 * • 5 minutes voice interaction per month
 * • Basic reminders (ANC, vaccines)
 * • Body literacy education
 * 
 * PREMIUM TIER (₹99/month or ₹899/year):
 * ──────────────────────────────────────
 * • Everything in Free, plus:
 * • Unlimited mental health screenings (EPDS, PHQ-9, GAD-7)
 * • Unlimited child symptom triage
 * • Unlimited voice interaction
 * • Personalized AI insights and predictions
 * • Detailed pregnancy tracking with kick counter
 * • Growth charts with trend analysis
 * • Medication reminders with interaction checker
 * • Health record PDF export
 * • Family member access (share with husband, mother)
 * • Full offline mode
 * • Priority support
 * 
 * FAMILY TIER (₹149/month or ₹1,299/year):
 * ────────────────────────────────────────
 * • Premium for 2 users (e.g., mother + daughter)
 * • Shared family dashboard
 * • Care coordination features
 * 
 * FREE TRIAL:
 * ───────────
 * • 7 days of Premium for new users
 * • No payment required to start
 * • Reminder on day 5 and day 7
 */

/**
 * PAYMENT FLOW (VISUAL)
 * 
 * 1. User taps "Upgrade" or hits free tier limit
 * 
 * 2. Subscription Selection Screen:
 *    ┌─────────────────────────────────────────────┐
 *    │                                             │
 *    │   Choose Your Plan                          │
 *    │                                             │
 *    │   ┌─────────────────────────────────────┐   │
 *    │   │  MONTHLY          YEARLY (Save 24%) │   │
 *    │   │  ₹99/mo           ₹899/yr           │   │
 *    │   │  [Selected]       [ ]               │   │
 *    │   └─────────────────────────────────────┘   │
 *    │                                             │
 *    │   ✓ Unlimited screenings                   │
 *    │   ✓ Unlimited voice                        │
 *    │   ✓ AI predictions                         │
 *    │   ✓ Full offline mode                      │
 *    │                                             │
 *    │   [ Start 7-Day Free Trial ]               │
 *    │                                             │
 *    └─────────────────────────────────────────────┘
 * 
 * 3. Payment Method Screen (Razorpay):
 *    ┌─────────────────────────────────────────────┐
 *    │                                             │
 *    │   Pay ₹99                                   │
 *    │                                             │
 *    │   ┌─────────┐ ┌─────────┐ ┌─────────┐     │
 *    │   │  GPay   │ │ PhonePe │ │  Paytm  │     │
 *    │   └─────────┘ └─────────┘ └─────────┘     │
 *    │                                             │
 *    │   ┌─────────┐ ┌─────────┐                 │
 *    │   │  BHIM   │ │ UPI ID  │                 │
 *    │   └─────────┘ └─────────┘                 │
 *    │                                             │
 *    │   ─────────── OR ───────────               │
 *    │                                             │
 *    │   [ Credit/Debit Card ]                    │
 *    │   [ Net Banking ]                          │
 *    │                                             │
 *    └─────────────────────────────────────────────┘
 * 
 * 4. UPI App Opens (GPay/PhonePe/Paytm)
 *    - User authenticates in their UPI app
 *    - Returns to Maa app on completion
 * 
 * 5. Success Screen:
 *    - Confirmation with subscription details
 *    - Receipt sent via SMS/WhatsApp (user choice)
 *    - Premium features unlocked immediately
 */

// ════════════════════════════════════════════════════════════════════════════
// IMPLEMENTATION
// ════════════════════════════════════════════════════════════════════════════

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.choose_plan),
            style = MaaTypography.headlineLarge
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Plan toggle: Monthly / Yearly
        PlanToggle(
            selectedPlan = uiState.selectedPlan,
            onPlanSelected = { viewModel.selectPlan(it) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Price display
        PriceCard(
            plan = uiState.selectedPlan,
            price = if (uiState.selectedPlan == Plan.MONTHLY) "₹99" else "₹899",
            period = if (uiState.selectedPlan == Plan.MONTHLY) "/month" else "/year",
            savings = if (uiState.selectedPlan == Plan.YEARLY) "Save ₹289" else null
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Features list (visual checkmarks)
        PremiumFeaturesList()
        
        Spacer(modifier = Modifier.weight(1f))
        
        // CTA Button
        MaaButton(
            text = stringResource(R.string.start_free_trial),
            onClick = {
                MaaFeedback.onSelect(LocalView.current)
                viewModel.startTrial()
            },
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = stringResource(R.string.trial_terms),  // "7-day free trial, then ₹99/month"
            style = MaaTypography.bodySmall,
            color = MaaColors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// Razorpay Integration
class PaymentService @Inject constructor(
    private val context: Context
) {
    private val razorpay = Checkout()
    
    init {
        Checkout.preload(context)
        razorpay.setKeyID(BuildConfig.RAZORPAY_KEY_ID)
    }
    
    fun startSubscription(
        activity: Activity,
        planId: String,  // Razorpay plan ID
        customerId: String,
        email: String,
        phone: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        val options = JSONObject().apply {
            put("name", "Maa Health")
            put("description", "Premium Subscription")
            put("subscription_id", planId)
            put("prefill", JSONObject().apply {
                put("email", email)
                put("contact", phone)
            })
            put("theme", JSONObject().apply {
                put("color", "#8B6F5C")  // MaaColors.accent
            })
        }
        
        razorpay.open(activity, options)
    }
}
```

---

## LANGUAGE SUPPORT

### Sarvam AI Integration

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * LANGUAGE & VOICE SYSTEM
 * ════════════════════════════════════════════════════════════════════════════
 */

/**
 * SUPPORTED LANGUAGES (via Sarvam AI)
 * 
 * All 10 languages support:
 * • Speech-to-Text (STT): Voice input
 * • Text-to-Speech (TTS): Voice output
 * • UI strings: Fully translated
 * 
 * Cost: ₹1 per minute of voice interaction
 */

enum class SupportedLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val sarvamCode: String
) {
    HINDI("hi", "हिन्दी", "Hindi", "hi-IN"),
    TAMIL("ta", "தமிழ்", "Tamil", "ta-IN"),
    TELUGU("te", "తెలుగు", "Telugu", "te-IN"),
    BENGALI("bn", "বাংলা", "Bengali", "bn-IN"),
    MARATHI("mr", "मराठी", "Marathi", "mr-IN"),
    KANNADA("kn", "ಕನ್ನಡ", "Kannada", "kn-IN"),
    MALAYALAM("ml", "മലയാളം", "Malayalam", "ml-IN"),
    GUJARATI("gu", "ગુજરાતી", "Gujarati", "gu-IN"),
    ODIA("or", "ଓଡ଼ିଆ", "Odia", "or-IN"),
    PUNJABI("pa", "ਪੰਜਾਬੀ", "Punjabi", "pa-IN")
}

/**
 * VOICE INPUT COMPONENT
 * 
 * Visual-first principle:
 * 1. Primary action is always visual (tap)
 * 2. Voice is secondary option shown as mic icon
 * 3. Voice prompt in user's language
 */
@Composable
fun VoiceInputButton(
    label: String,  // e.g., "या बोलकर बताएं" (or speak to tell)
    language: SupportedLanguage = LocalLanguage.current,
    onVoiceResult: (String) -> Unit,
    onListeningStateChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var isListening by remember { mutableStateOf(false) }
    val sarvamService = LocalSarvamService.current
    
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaaColors.surface)
            .clickable {
                isListening = true
                onListeningStateChange(true)
                MaaFeedback.onSelect(LocalView.current)
            }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Microphone icon (SVG)
        Icon(
            painter = painterResource(R.drawable.ic_microphone),
            contentDescription = null,
            tint = if (isListening) MaaColors.accent else MaaColors.textSecondary,
            modifier = Modifier.size(24.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Text(
            text = if (isListening) 
                stringResource(R.string.listening) 
            else 
                label,
            style = MaaTypography.labelLarge,
            color = MaaColors.textSecondary
        )
        
        if (isListening) {
            Spacer(modifier = Modifier.width(8.dp))
            // Listening animation (pulsing circles)
            ListeningAnimation()
        }
    }
    
    // Handle voice recording
    LaunchedEffect(isListening) {
        if (isListening) {
            try {
                val result = sarvamService.speechToText(language.sarvamCode)
                onVoiceResult(result)
            } finally {
                isListening = false
                onListeningStateChange(false)
            }
        }
    }
}

/**
 * SARVAM AI SERVICE
 */
class SarvamService @Inject constructor(
    private val apiKey: String
) {
    private val baseUrl = "https://api.sarvam.ai/v1"
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $apiKey")
                .build()
            chain.proceed(request)
        }
        .build()
    
    /**
     * Speech-to-Text
     * Records audio and returns transcribed text
     */
    suspend fun speechToText(
        languageCode: String,
        audioFile: File? = null  // If null, records from mic
    ): String = withContext(Dispatchers.IO) {
        val audio = audioFile ?: recordAudio()
        
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file", 
                "audio.wav",
                audio.asRequestBody("audio/wav".toMediaType())
            )
            .addFormDataPart("language", languageCode)
            .addFormDataPart("model", "sarvam-1")
            .build()
        
        val request = Request.Builder()
            .url("$baseUrl/speech-to-text")
            .post(requestBody)
            .build()
        
        val response = client.newCall(request).execute()
        val json = JSONObject(response.body?.string() ?: "")
        json.getString("transcript")
    }
    
    /**
     * Text-to-Speech
     * Converts text to audio and plays it
     */
    suspend fun textToSpeech(
        text: String,
        languageCode: String,
        voice: String = "meera"  // Female voice
    ): ByteArray = withContext(Dispatchers.IO) {
        val requestBody = JSONObject().apply {
            put("text", text)
            put("language", languageCode)
            put("voice", voice)
            put("model", "sarvam-1")
        }.toString().toRequestBody("application/json".toMediaType())
        
        val request = Request.Builder()
            .url("$baseUrl/text-to-speech")
            .post(requestBody)
            .build()
        
        val response = client.newCall(request).execute()
        response.body?.bytes() ?: ByteArray(0)
    }
    
    private suspend fun recordAudio(): File {
        // Implementation using AudioRecord
        // Records until silence detected or max duration (30s)
        TODO("Implement audio recording")
    }
}
```

---

## AGENTIC AI ARCHITECTURE

### Agent System

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * AGENTIC AI SYSTEM
 * ════════════════════════════════════════════════════════════════════════════
 * 
 * The app uses an agentic architecture where specialized AI agents handle
 * different aspects of health management. All agents learn from user data
 * and improve over time.
 * 
 * NOTHING IS STATIC. Every feature is powered by AI that:
 * • Learns from user's patterns
 * • Makes personalized predictions
 * • Provides contextual recommendations
 * • Adapts to user's communication style
 */

/**
 * AGENT HIERARCHY
 * 
 * ┌─────────────────────────────────────────────────────────────────────────┐
 * │                        ORCHESTRATOR AGENT                               │
 * │         (Routes requests, maintains context, prioritizes)               │
 * └─────────────────────────────────────────────────────────────────────────┘
 *                                    │
 *         ┌──────────────────────────┼──────────────────────────┐
 *         ▼                          ▼                          ▼
 * ┌───────────────┐        ┌───────────────┐        ┌───────────────┐
 * │  SCREENING    │        │    TRIAGE     │        │  EDUCATION    │
 * │    AGENT      │        │    AGENT      │        │    AGENT      │
 * ├───────────────┤        ├───────────────┤        ├───────────────┤
 * │ • EPDS        │        │ • Danger signs│        │ • Body literacy│
 * │ • PHQ-9       │        │ • Pneumonia   │        │ • Week-by-week│
 * │ • GAD-7       │        │ • Diarrhea    │        │ • Nutrition   │
 * │ • PHQ-A       │        │ • Fever       │        │ • Milestones  │
 * │ • Risk scoring│        │ • Emergency   │        │ • Myth-busting│
 * └───────────────┘        └───────────────┘        └───────────────┘
 *         │                          │                          │
 *         └──────────────────────────┼──────────────────────────┘
 *                                    ▼
 *                          ┌───────────────┐
 *                          │    ACTION     │
 *                          │    AGENT      │
 *                          ├───────────────┤
 *                          │ • Reminders   │
 *                          │ • Referrals   │
 *                          │ • Escalation  │
 *                          │ • Booking     │
 *                          └───────────────┘
 *                                    │
 *                                    ▼
 *                          ┌───────────────┐
 *                          │   LEARNING    │
 *                          │    AGENT      │
 *                          ├───────────────┤
 *                          │ • Pattern mine│
 *                          │ • Model update│
 *                          │ • Personalize │
 *                          └───────────────┘
 */

// ════════════════════════════════════════════════════════════════════════════
// AGENT IMPLEMENTATIONS
// ════════════════════════════════════════════════════════════════════════════

/**
 * Orchestrator Agent
 * Central coordinator that routes requests and maintains context
 */
class OrchestratorAgent @Inject constructor(
    private val screeningAgent: ScreeningAgent,
    private val triageAgent: TriageAgent,
    private val educationAgent: EducationAgent,
    private val actionAgent: ActionAgent,
    private val learningAgent: LearningAgent,
    private val userRepository: UserRepository,
    private val healthRepository: HealthRepository
) {
    /**
     * Process any user input (body tap, voice, symptom selection)
     * Routes to appropriate agent and maintains conversation context
     */
    suspend fun process(input: UserInput): AgentResponse {
        // Get user context
        val userContext = userRepository.getCurrentUserContext()
        val healthContext = healthRepository.getRecentHealthData(days = 30)
        
        // Determine intent and route
        val intent = classifyIntent(input, userContext)
        
        return when (intent) {
            Intent.MENTAL_HEALTH_CHECK -> screeningAgent.conductScreening(
                type = ScreeningType.fromContext(userContext),
                context = healthContext
            )
            
            Intent.SYMPTOM_REPORT -> triageAgent.assessSymptoms(
                symptoms = input.symptoms,
                userContext = userContext
            )
            
            Intent.BODY_REGION_TAP -> {
                val region = input.bodyRegion!!
                routeBodyRegionTap(region, userContext)
            }
            
            Intent.EDUCATION_REQUEST -> educationAgent.provideEducation(
                topic = input.topic,
                userContext = userContext
            )
            
            Intent.TRACK_DATA -> {
                val result = healthRepository.logData(input.trackingData!!)
                learningAgent.processNewData(result)
                actionAgent.checkForActions(userContext)
            }
            
            else -> educationAgent.handleGenericQuery(input)
        }
    }
    
    private fun routeBodyRegionTap(
        region: BodyRegion,
        context: UserContext
    ): AgentResponse {
        // Based on lifecycle stage and region, determine action
        return when (context.lifecycleStage) {
            LifecycleStage.PREGNANT -> when (region) {
                BodyRegion.HEAD -> screeningAgent.promptMentalHealthCheck(context)
                BodyRegion.ABDOMEN -> triageAgent.assessPregnancySymptoms(context)
                BodyRegion.FEET -> triageAgent.assessSwelling(context)
                else -> educationAgent.explainRegion(region, context)
            }
            LifecycleStage.CHILD_CARE -> when (region) {
                BodyRegion.CHEST -> triageAgent.assessBreathing(context)
                BodyRegion.ABDOMEN -> triageAgent.assessDiarrhea(context)
                else -> educationAgent.explainChildRegion(region, context)
            }
            // ... other stages
        }
    }
}

/**
 * Screening Agent
 * Conducts validated mental health screenings (EPDS, PHQ-9, etc.)
 */
class ScreeningAgent @Inject constructor(
    private val medGemmaService: MedGemmaService,
    private val screeningRepository: ScreeningRepository
) {
    /**
     * Conduct a screening assessment
     * Questions are presented visually, learned patterns inform interpretation
     */
    suspend fun conductScreening(
        type: ScreeningType,
        context: HealthContext
    ): ScreeningSession {
        // Get previous screenings for trend analysis
        val previousScores = screeningRepository.getPreviousScores(type, limit = 5)
        
        // Create session with adaptive questioning
        return ScreeningSession(
            type = type,
            questions = getAdaptiveQuestions(type, previousScores),
            interpretation = { responses ->
                interpretWithContext(type, responses, previousScores, context)
            }
        )
    }
    
    private fun interpretWithContext(
        type: ScreeningType,
        responses: List<Int>,
        previousScores: List<ScreeningScore>,
        context: HealthContext
    ): ScreeningInterpretation {
        val currentScore = responses.sum()
        
        // Use MedGemma for nuanced interpretation
        val analysis = medGemmaService.analyzeScreening(
            type = type,
            currentScore = currentScore,
            previousScores = previousScores,
            context = context
        )
        
        // Detect trends
        val trend = when {
            previousScores.isEmpty() -> Trend.BASELINE
            currentScore > previousScores.last().score + 3 -> Trend.WORSENING
            currentScore < previousScores.last().score - 3 -> Trend.IMPROVING
            else -> Trend.STABLE
        }
        
        return ScreeningInterpretation(
            score = currentScore,
            severity = analysis.severity,
            trend = trend,
            insights = analysis.insights,  // Personalized, not generic
            recommendations = analysis.recommendations,
            needsEscalation = analysis.needsEscalation
        )
    }
}

/**
 * Triage Agent
 * Assesses symptoms and determines urgency using IMCI protocols
 */
class TriageAgent @Inject constructor(
    private val medGemmaService: MedGemmaService,
    private val healthRepository: HealthRepository
) {
    /**
     * Assess danger signs in pregnancy
     * Returns urgency level and recommended action
     */
    suspend fun assessDangerSigns(
        symptoms: List<Symptom>,
        gestationalWeek: Int,
        context: HealthContext
    ): TriageResult {
        // WHO danger signs checklist
        val dangerSignsPresent = symptoms.filter { it.isDangerSign }
        
        // Get user's baseline for comparison
        val baseline = healthRepository.getSymptomBaseline()
        
        // MedGemma analysis for nuanced assessment
        val analysis = medGemmaService.triagePregnancy(
            symptoms = symptoms,
            week = gestationalWeek,
            baseline = baseline,
            history = context.previousSymptoms
        )
        
        return TriageResult(
            urgency = analysis.urgency,  // EMERGENCY, URGENT, ROUTINE, SELF_CARE
            assessment = analysis.assessment,
            action = when (analysis.urgency) {
                Urgency.EMERGENCY -> Action.CALL_EMERGENCY
                Urgency.URGENT -> Action.GO_TO_HOSPITAL_TODAY
                Urgency.ROUTINE -> Action.SCHEDULE_VISIT
                Urgency.SELF_CARE -> Action.HOME_MANAGEMENT
            },
            instructions = analysis.instructions,
            warningSignsToWatch = analysis.warningSignsToWatch
        )
    }
    
    /**
     * Assess child symptoms using IMCI protocol
     * Visual-first: Uses breathing rate counter, visual comparisons
     */
    suspend fun assessChildSymptoms(
        symptoms: List<Symptom>,
        childAgeMonths: Int,
        measurements: ChildMeasurements?  // breathing rate, temp, etc.
    ): ChildTriageResult {
        // IMCI age-specific thresholds
        val breathingThreshold = when {
            childAgeMonths < 2 -> 60
            childAgeMonths < 12 -> 50
            else -> 40
        }
        
        // Classify pneumonia
        val pneumoniaClassification = when {
            measurements?.breathingRate ?: 0 >= breathingThreshold && 
                symptoms.contains(Symptom.CHEST_INDRAWING) -> 
                    PneumoniaClassification.SEVERE
            measurements?.breathingRate ?: 0 >= breathingThreshold ->
                    PneumoniaClassification.PNEUMONIA
            else -> PneumoniaClassification.NO_PNEUMONIA
        }
        
        // Classify dehydration
        val dehydrationClassification = classifyDehydration(symptoms)
        
        // MedGemma for overall assessment
        val analysis = medGemmaService.triageChild(
            symptoms = symptoms,
            ageMonths = childAgeMonths,
            pneumonia = pneumoniaClassification,
            dehydration = dehydrationClassification
        )
        
        return ChildTriageResult(
            overallUrgency = analysis.urgency,
            pneumoniaClassification = pneumoniaClassification,
            dehydrationClassification = dehydrationClassification,
            action = analysis.recommendedAction,
            homeCare = analysis.homeCareInstructions,
            medicationDosing = calculateDosing(childAgeMonths, analysis.medications)
        )
    }
}

/**
 * Learning Agent
 * Continuously learns from user data to improve predictions
 */
class LearningAgent @Inject constructor(
    private val patternRepository: PatternRepository,
    private val predictionModel: PredictionModel
) {
    /**
     * Process new data and update user patterns
     */
    suspend fun processNewData(data: HealthData) {
        when (data) {
            is CycleData -> {
                patternRepository.updateCyclePattern(data)
                predictionModel.updateCyclePrediction(data)
            }
            is MoodData -> {
                patternRepository.updateMoodPattern(data)
                // Correlate with cycle, sleep, other factors
                patternRepository.findMoodCorrelations(data)
            }
            is SymptomData -> {
                patternRepository.logSymptom(data)
                // Check for patterns (recurring, worsening, etc.)
                patternRepository.analyzeSymptomPatterns()
            }
            is MedicationData -> {
                patternRepository.updateAdherence(data)
                // Correlate side effects with timing
            }
        }
    }
    
    /**
     * Generate personalized prediction
     */
    suspend fun predict(type: PredictionType, context: UserContext): Prediction {
        return when (type) {
            PredictionType.NEXT_PERIOD -> {
                val pattern = patternRepository.getCyclePattern()
                Prediction(
                    value = pattern.predictedNextPeriod,
                    confidence = pattern.confidence,
                    basis = "Based on your last ${pattern.cyclesAnalyzed} cycles"
                )
            }
            PredictionType.OVULATION -> {
                val pattern = patternRepository.getCyclePattern()
                Prediction(
                    value = pattern.predictedOvulation,
                    confidence = pattern.ovulationConfidence,
                    basis = "Your typical ovulation is day ${pattern.averageOvulationDay}"
                )
            }
            PredictionType.MOOD_DIP -> {
                val pattern = patternRepository.getMoodPattern()
                Prediction(
                    value = pattern.predictedDipDate,
                    confidence = pattern.confidence,
                    basis = "You often feel low around day ${pattern.typicalDipDay} of your cycle"
                )
            }
            // ... other predictions
        }
    }
}

/**
 * MedGemma Service
 * Interface to Google's MedGemma models for clinical reasoning
 */
class MedGemmaService @Inject constructor(
    private val localModel: MedGemma4BLocal,  // On-device for offline
    private val cloudModel: MedGemma27BCloud   // Cloud for complex reasoning
) {
    /**
     * Analyze symptoms with clinical reasoning
     * Uses 4B locally for quick response, 27B for complex cases
     */
    suspend fun analyzeSymptoms(
        symptoms: List<Symptom>,
        context: HealthContext
    ): SymptomAnalysis {
        // Quick local analysis
        val localAnalysis = localModel.analyze(symptoms.toPrompt())
        
        // If complex or high-risk, use cloud model
        return if (localAnalysis.needsDeepAnalysis || localAnalysis.isHighRisk) {
            cloudModel.analyze(
                symptoms = symptoms,
                context = context,
                localAnalysis = localAnalysis
            )
        } else {
            localAnalysis
        }
    }
    
    /**
     * Check drug interactions
     * Critical for elder care module with polypharmacy
     */
    suspend fun checkDrugInteractions(
        medications: List<Medication>
    ): List<DrugInteraction> {
        return cloudModel.checkInteractions(medications)
    }
}
```

---

## LIFECYCLE STAGES - COMPLETE FEATURES

### Stage 1: Adolescence (Ages 13-19)

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * ADOLESCENCE MODULE (Ages 13-19)
 * ════════════════════════════════════════════════════════════════════════════
 */

/**
 * FEATURES:
 * 
 * 1. MENSTRUAL HEALTH TRACKING (Agentic)
 *    ─────────────────────────────────────
 *    Visual Input:
 *    • Calendar tap to log period start/end
 *    • Flow intensity: Visual scale (light drops → heavy)
 *    • Symptoms: Icon grid (cramps, headache, bloating, mood)
 *    
 *    Agent Learning:
 *    • Learns YOUR cycle length (not assumed 28 days)
 *    • Predicts YOUR next period with confidence interval
 *    • Detects irregularities: "Your cycles have varied 21-45 days 
 *      for 4 months. This may indicate PCOS. Learn more?"
 *    • Correlates symptoms with cycle phase
 *    • Predicts YOUR symptoms: "Based on pattern, you may have 
 *      cramps tomorrow. Start pain relief today?"
 *    
 *    Proactive:
 *    • Period reminder 2 days before predicted date
 *    • "Your period is 10 days late. Take a test or see doctor?"
 *    • Iron/nutrition tips during heavy flow days
 * 
 * 2. MENTAL HEALTH SCREENING (Agentic)
 *    ────────────────────────────────────
 *    Screening Tools:
 *    • PHQ-A (Patient Health Questionnaire - Adolescent)
 *    • GAD-7 (Anxiety)
 *    • PSC-17 (General behavioral health)
 *    
 *    Visual Input:
 *    • Mood icons (5-point scale, abstract shapes)
 *    • Tap-based responses to screening questions
 *    • Voice option for those who prefer speaking
 *    
 *    Agent Learning:
 *    • Tracks mood patterns by time, day, cycle phase
 *    • Identifies triggers: "Your mood drops before exams"
 *    • Learns which coping strategies work for YOU
 *    • Detects concerning trends: "Mood declining for 2 weeks"
 *    
 *    Proactive:
 *    • "Exam week coming. Last time your anxiety increased.
 *      Try the breathing exercise that helped before?"
 *    • Crisis detection with helpline access
 * 
 * 3. NUTRITION & ANEMIA PREVENTION (Agentic)
 *    ─────────────────────────────────────────
 *    Visual Input:
 *    • Food logging with visual icons
 *    • Symptom tracking: fatigue, pallor, breathlessness
 *    
 *    Agent Learning:
 *    • Estimates iron intake from logged foods
 *    • Correlates diet with symptoms
 *    • Personalized suggestions based on preferences
 *    
 *    Proactive:
 *    • "You haven't had iron-rich foods in 5 days"
 *    • IFA supplement reminders
 *    • Anemia risk scoring without blood test
 * 
 * 4. BODY LITERACY (Education Agent)
 *    ────────────────────────────────
 *    Visual:
 *    • Interactive body SVG (adolescent figure)
 *    • Tap regions to learn about changes
 *    • Puberty timeline personalized to user's stage
 *    
 *    Content:
 *    • Reproductive anatomy (age-appropriate)
 *    • Breast development stages
 *    • Menstrual cycle explained
 *    • Normal vs. concerning changes
 *    
 *    Myth-Busting:
 *    • Contextual corrections when myths detected
 *    • "Can I exercise during period?" → Evidence-based answer
 * 
 * 5. SAFETY (Always Available)
 *    ─────────────────────────
 *    • Good touch/bad touch education
 *    • POCSO Act awareness (age-appropriate)
 *    • "Something happened" safe disclosure pathway
 *    • Childline (1098) quick access
 *    • Women Helpline (181) quick access
 */

// Visual Flow: Period Logging
@Composable
fun LogPeriodScreen(viewModel: CycleViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
    ) {
        // Calendar visual (tap dates)
        PeriodCalendar(
            selectedDates = uiState.selectedDates,
            predictedPeriod = uiState.prediction,
            onDateTap = { date ->
                MaaFeedback.onTap(LocalView.current)
                viewModel.toggleDate(date)
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Flow intensity (visual scale)
        Text(
            text = stringResource(R.string.flow_today),
            style = MaaTypography.titleMedium
        )
        
        FlowIntensitySelector(
            selected = uiState.flowIntensity,
            onSelect = { viewModel.setFlowIntensity(it) }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Symptoms (icon grid)
        Text(
            text = stringResource(R.string.symptoms),
            style = MaaTypography.titleMedium
        )
        
        SymptomIconGrid(
            symptoms = PeriodSymptom.values(),
            selected = uiState.selectedSymptoms,
            onToggle = { viewModel.toggleSymptom(it) }
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Voice option
        VoiceInputButton(
            label = stringResource(R.string.or_speak),
            onVoiceResult = { viewModel.processVoiceInput(it) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        MaaButton(
            text = stringResource(R.string.save),
            onClick = { viewModel.savePeriodLog() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun FlowIntensitySelector(
    selected: FlowIntensity?,
    onSelect: (FlowIntensity) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        FlowIntensity.values().forEach { intensity ->
            FlowIntensityOption(
                intensity = intensity,
                isSelected = intensity == selected,
                onClick = { 
                    MaaFeedback.onTap(LocalView.current)
                    onSelect(intensity) 
                }
            )
        }
    }
}

@Composable
fun FlowIntensityOption(
    intensity: FlowIntensity,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaaColors.selectedRegion else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Visual representation (SVG drops)
        when (intensity) {
            FlowIntensity.SPOTTING -> Icon(/* 1 small drop */)
            FlowIntensity.LIGHT -> Icon(/* 2 drops */)
            FlowIntensity.MEDIUM -> Icon(/* 3 drops */)
            FlowIntensity.HEAVY -> Icon(/* 4 filled drops */)
        }
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = stringResource(intensity.labelRes),
            style = MaaTypography.labelSmall,
            color = if (isSelected) MaaColors.textPrimary else MaaColors.textSecondary
        )
    }
}
```

### Stage 2: Reproductive Years (Ages 18-40)

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * REPRODUCTIVE YEARS MODULE (Ages 18-40)
 * ════════════════════════════════════════════════════════════════════════════
 */

/**
 * FEATURES:
 * 
 * 1. FERTILITY AWARENESS (Agentic)
 *    ─────────────────────────────
 *    Modes:
 *    • AVOID: Preventing pregnancy
 *    • NEUTRAL: Just tracking
 *    • TTC: Trying to conceive
 *    
 *    Agent Learning:
 *    • Learns YOUR ovulation pattern (not assumed day 14)
 *    • Tracks: cycle length, cervical mucus, BBT (if logged)
 *    • Calculates YOUR fertile window with confidence
 *    
 *    TTC Mode Intelligence:
 *    • Tracks months trying
 *    • At 6 months: Tips to optimize
 *    • At 12 months (6 if >35): Suggests evaluation
 *    • Lifestyle correlations: sleep, stress → cycle impact
 * 
 * 2. PCOS MANAGEMENT (Agentic)
 *    ────────────────────────────
 *    Risk Detection:
 *    • Cycle irregularity analysis
 *    • Symptom tracking: acne, hair growth, weight
 *    • Family history (asked once)
 *    
 *    If PCOS Suspected/Confirmed:
 *    • Personalized cycle predictions (accounting for irregularity)
 *    • Weight tracking with PCOS-specific targets
 *    • Diet suggestions (low GI) learning your preferences
 *    • Symptom improvement tracking over time
 * 
 * 3. CONTRACEPTION GUIDANCE (Agentic)
 *    ─────────────────────────────────
 *    • Decision support based on preferences
 *    • Side effect tracking with correlation
 *    • Refill reminders (learns YOUR purchase pattern)
 *    • Method switching guidance
 *    • Emergency contraception quick access
 * 
 * 4. PRE-CONCEPTION HEALTH (Triggered when TTC mode)
 *    ───────────────────────────────────────────────
 *    Personalized checklist:
 *    • Folic acid reminder
 *    • Anemia check (based on symptom history)
 *    • Vaccination status
 *    • Chronic condition optimization
 *    • Lifestyle review
 * 
 * 5. MENTAL WELLNESS (Agentic)
 *    ──────────────────────────
 *    • PHQ-9, GAD-7, PSS
 *    • Cycle-mood correlation (PMDD detection)
 *    • Work-life stress tracking
 *    • Relationship health (DV screening, sensitive)
 * 
 * 6. SEXUAL HEALTH
 *    ──────────────
 *    • STI symptom checker
 *    • UTI tracking with pattern detection
 *    • Testing reminders
 */
```

### Stage 3: Pregnancy (Conception to 40 Weeks)

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * PREGNANCY MODULE (Conception to 40 Weeks)
 * ════════════════════════════════════════════════════════════════════════════
 */

/**
 * CORE INTERFACE: Interactive Pregnant Body SVG
 * 
 * The pregnant body SVG is the central navigation interface.
 * It updates dynamically based on gestational week.
 * 
 * ┌────────────────────────────────────────────────────────────────────┐
 * │                                                                    │
 * │                    Week 24 of 40                                   │
 * │                                                                    │
 * │                      ┌───────┐                                     │
 * │                      │ HEAD  │ ◄─── Tap for mental health,        │
 * │                      │       │      headaches, vision              │
 * │                      ├───────┤                                     │
 * │                      │ CHEST │ ◄─── Breathing, heart palpitations  │
 * │                      │       │                                     │
 * │                  ╭───┴───────┴───╮                                 │
 * │                  │               │                                 │
 * │                  │    UTERUS     │ ◄─── Baby development,          │
 * │                  │   (shows      │      kick counter,              │
 * │                  │    fetus)     │      contractions               │
 * │                  │               │                                 │
 * │                  ╰───────────────╯                                 │
 * │                      │ PELVIS│ ◄─── Bleeding, discharge            │
 * │                      ├───────┤                                     │
 * │                  ┌───┘       └───┐                                 │
 * │                  │   LEGS/FEET   │ ◄─── Swelling assessment        │
 * │                  └───────────────┘                                 │
 * │                                                                    │
 * │   [🎤 Voice]                           [Next ANC: 3 days]         │
 * │                                                                    │
 * └────────────────────────────────────────────────────────────────────┘
 */

/**
 * FEATURES:
 * 
 * 1. DANGER SIGNS ASSESSMENT (Always Free, Critical)
 *    ─────────────────────────────────────────────────
 *    Visual Input:
 *    • Tap body region where symptom is
 *    • Visual symptom icons (bleeding, headache, vision, etc.)
 *    • Severity scale (visual, not numeric)
 *    
 *    WHO Danger Signs Monitored:
 *    • Vaginal bleeding
 *    • Severe headache
 *    • Blurred vision
 *    • High fever
 *    • Severe abdominal pain
 *    • Reduced fetal movement
 *    • Sudden swelling
 *    • Convulsions
 *    • Difficulty breathing
 *    • Leaking fluid
 *    
 *    Agent Intelligence:
 *    • Correlates multiple symptoms (headache + swelling = preeclampsia?)
 *    • Compares to YOUR baseline
 *    • Urgency classification: Emergency / Urgent / Routine / Self-care
 *    
 *    Actions:
 *    • Emergency: Direct call to ambulance/hospital
 *    • Urgent: "Go to hospital today" with directions
 *    • Routine: "Discuss at next ANC visit"
 *    • Self-care: Home management guidance
 * 
 * 2. MENTAL HEALTH SCREENING (Agentic)
 *    ────────────────────────────────────
 *    • EPDS (Edinburgh Postnatal Depression Scale)
 *    • Continuous mood monitoring
 *    • Trend detection: "Your mood has dropped for 2 weeks"
 *    • Risk stratification based on history, support system
 *    • Bonding assessment (late pregnancy baseline)
 *    
 *    Visual Input:
 *    • Mood icons (tap-based)
 *    • EPDS questions with visual response options
 *    • Voice option for answers
 * 
 * 3. KICK COUNTER (Agentic)
 *    ───────────────────────
 *    Visual Interface:
 *    • Large tap area for counting kicks
 *    • Timer display
 *    • Historical comparison
 *    
 *    Agent Learning:
 *    • Learns YOUR baby's movement pattern
 *    • Tracks: time of day, after meals, activity level
 *    • Personalized threshold (not generic "10 kicks")
 *    • Alert if movements 50% below YOUR baseline
 *    
 *    Proactive:
 *    • "Baby is usually active at 9pm. Want to count now?"
 *    • "Movements are lower than your usual. How are you feeling?"
 * 
 * 4. ANC COMPANION (Agentic)
 *    ─────────────────────────
 *    • Visit scheduler with reminders
 *    • Pre-visit prep: "Things to discuss based on your logs"
 *    • Post-visit logging: weight, BP, test results
 *    • Question list builder
 *    
 *    Agent Intelligence:
 *    • Tracks YOUR visit pattern vs. recommended
 *    • Identifies gaps: "You haven't had GDM screening yet"
 *    • Trend analysis: BP trend, weight trend
 * 
 * 5. NUTRITION & SUPPLEMENTS (Agentic)
 *    ─────────────────────────────────
 *    • IFA tracking with adherence scoring
 *    • Calcium reminders
 *    • Weight tracking with personalized targets
 *    
 *    Agent Learning:
 *    • Learns when YOU actually take pills
 *    • Adjusts reminder time to your pattern
 *    • Tracks side effects, suggests solutions
 * 
 * 6. WEEK-BY-WEEK (Education Agent)
 *    ────────────────────────────────
 *    Visual:
 *    • Fetus SVG that updates each week
 *    • Size comparison (fruit metaphor as overlay)
 *    • Development highlights
 *    
 *    Content:
 *    • What's happening this week
 *    • What to expect
 *    • Personalized based on YOUR logged symptoms
 * 
 * 7. BIRTH PREPARATION (from 32 weeks)
 *    ─────────────────────────────────
 *    • Hospital bag checklist (visual, tappable)
 *    • Birth plan builder
 *    • Labor signs education
 *    • Contraction timer with pattern analysis
 *    • "When to go" decision support
 */

// Example: Danger Signs Assessment
@Composable
fun DangerSignsScreen(
    viewModel: DangerSignsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
    ) {
        // Header with urgency indicator
        DangerSignsHeader(urgencyLevel = uiState.currentUrgency)
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Left: Body SVG for region selection
            InteractiveBodySvg(
                bodyType = BodyType.PREGNANT,
                gestationalWeek = uiState.gestationalWeek,
                selectedRegions = uiState.selectedRegions,
                highlightedRegions = uiState.dangerRegions,
                onRegionTap = { region ->
                    MaaFeedback.onSelect(LocalView.current)
                    viewModel.selectRegion(region)
                },
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight()
            )
            
            // Right: Symptom selection for region
            DangerSignsPanel(
                selectedRegion = uiState.selectedRegions.firstOrNull(),
                symptoms = uiState.availableSymptoms,
                selectedSymptoms = uiState.reportedSymptoms,
                onSymptomToggle = { viewModel.toggleSymptom(it) },
                modifier = Modifier
                    .weight(0.6f)
                    .fillMaxHeight()
            )
        }
        
        // Bottom: Voice option + Submit
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            VoiceInputButton(
                label = stringResource(R.string.describe_symptoms),
                onVoiceResult = { viewModel.processVoiceInput(it) }
            )
            
            MaaButton(
                text = stringResource(R.string.assess),
                onClick = { viewModel.assessSymptoms() }
            )
        }
    }
}
```

### Stage 4: Postpartum (0-12 Months After Birth)

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * POSTPARTUM MODULE (0-12 Months After Birth)
 * ════════════════════════════════════════════════════════════════════════════
 */

/**
 * FEATURES:
 * 
 * 1. POSTPARTUM DEPRESSION SCREENING (Critical, Agentic)
 *    ─────────────────────────────────────────────────────
 *    Timeline:
 *    • Day 3-5: Monitor for baby blues (normalize but watch)
 *    • Week 2: Screen if blues persist
 *    • Week 6: Full EPDS
 *    • Month 3, 6: Follow-up EPDS
 *    
 *    Agent Intelligence:
 *    • Distinguishes baby blues vs PPD
 *    • Correlates with: sleep deprivation, feeding challenges, support
 *    • Bonding assessment (PBQ)
 *    • Postpartum psychosis red flags: confusion, hallucinations
 *    
 *    Proactive:
 *    • "Baby blues usually resolve by day 14. Yours have continued.
 *      Let's do a deeper check."
 *    • Daily mood check (visual, single tap)
 * 
 * 2. BREASTFEEDING SUPPORT (Agentic)
 *    ────────────────────────────────
 *    Visual Tracking:
 *    • Feeding timer with breast indicator (left/right)
 *    • Duration tracking
 *    • Problem logging with visual icons
 *    
 *    Agent Learning:
 *    • Learns YOUR baby's feeding pattern
 *    • Detects cluster feeding, growth spurts
 *    • Problem pattern detection: repeated nipple pain → latch issue
 *    
 *    Proactive:
 *    • "Baby fed 8 times in 24 hours, averaging 20 min. Healthy!"
 *    • "You logged low supply 3 times. Here are evidence-based tips..."
 *    • "Watch for these mastitis signs: [visual guide]"
 * 
 * 3. NEWBORN DANGER SIGNS (Triage Agent)
 *    ─────────────────────────────────────
 *    WHO/IMNCI Danger Signs:
 *    • Not feeding well
 *    • Convulsions
 *    • Fast breathing (>60/min)
 *    • Severe chest indrawing
 *    • Temperature abnormal
 *    • Jaundice (visual assessment via photo)
 *    • Umbilical redness/pus
 *    
 *    Visual Assessment:
 *    • Photo-based jaundice estimation
 *    • Breathing video analysis
 *    • Visual comparison guides
 * 
 * 4. PHYSICAL RECOVERY (Agentic)
 *    ────────────────────────────
 *    Tracks:
 *    • Lochia (bleeding) with visual color guide
 *    • C-section incision healing
 *    • Perineal recovery
 *    • Pelvic floor symptoms
 *    
 *    Agent Learning:
 *    • Normal vs concerning patterns
 *    • "Your bleeding has reduced normally. Color change expected."
 *    • "Incision pain increasing? Watch for these signs..."
 * 
 * 5. CONTRACEPTION RETURN
 *    ─────────────────────
 *    • Method selection support
 *    • Fertility return tracking
 *    • LAM effectiveness calculation
 *    • Seamless transition to cycle tracking when menses return
 */
```

### Stage 5: Child Care (Ages 0-5 Years)

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * CHILD CARE MODULE (Ages 0-5 Years)
 * ════════════════════════════════════════════════════════════════════════════
 */

/**
 * CORE INTERFACE: Interactive Child Body SVG
 * 
 * Body illustration morphs based on child's age.
 * Tap regions to assess symptoms or learn.
 * 
 * FEATURES:
 * 
 * 1. SYMPTOM TRIAGE (IMCI-Based, Agentic)
 *    ─────────────────────────────────────
 *    Visual Flow:
 *    a) "What's wrong with baby?" → Symptom icon grid
 *    b) Tap symptom → Guided assessment
 *    c) Visual severity indicators
 *    d) Age-specific thresholds (agent knows child's exact age)
 *    
 *    PNEUMONIA TRIAGE:
 *    • Breathing rate counter (tap per breath, 60-second timer)
 *    • Chest indrawing visual comparison
 *    • Wheeze/stridor detection (audio recording option)
 *    • Classification: No pneumonia / Pneumonia / Severe pneumonia
 *    • Action: Home care / Go to PHC / Emergency
 *    
 *    DIARRHEA + DEHYDRATION:
 *    • Visual dehydration assessment
 *    • Eyes comparison (normal vs sunken)
 *    • Skin pinch test demonstration
 *    • Classification: No / Some / Severe dehydration
 *    • ORS calculator (uses child's weight)
 *    
 *    FEVER:
 *    • Age-based risk stratification
 *    • Paracetamol dosing (uses child's weight)
 *    • Danger sign check (rash, stiff neck, etc.)
 *    
 *    Agent Learning:
 *    • "3rd respiratory episode in 2 months. Consider asthma evaluation."
 *    • "5 diarrhea episodes in 6 months. Review hygiene, check vaccination."
 *    • Tracks what treatments work for YOUR child
 * 
 * 2. VACCINATION TRACKER (Agentic)
 *    ─────────────────────────────────
 *    Visual:
 *    • Full India NIS schedule with due dates
 *    • Visual timeline with completed/upcoming
 *    • Tap to log (with photo of card option)
 *    
 *    Agent Intelligence:
 *    • Tracks ACTUAL doses, not just due dates
 *    • Catch-up scheduling for missed doses
 *    • Side effect tracking + pattern
 *    • "Penta-2 due. Last time baby had mild fever - this is normal."
 *    • Camp notifications: "Vaccination camp in your area on [date]"
 * 
 * 3. GROWTH MONITORING (Agentic)
 *    ─────────────────────────────
 *    Visual:
 *    • WHO growth chart with trend line
 *    • Percentile tracking
 *    
 *    Measurements:
 *    • Weight-for-age (underweight)
 *    • Height-for-age (stunting)
 *    • Weight-for-height (wasting)
 *    • Head circumference (0-2 years)
 *    
 *    Agent Intelligence:
 *    • Trend detection: "Weight gain slowed last 2 months"
 *    • Root cause exploration: "Has feeding changed? Any illness?"
 *    • Predictive: "At current trajectory, may be underweight in 3 months"
 *    • Correlates illness episodes with growth dips
 * 
 * 4. DEVELOPMENTAL MILESTONES (Agentic)
 *    ─────────────────────────────────────
 *    Domains:
 *    • Gross motor
 *    • Fine motor
 *    • Language
 *    • Social-emotional
 *    • Cognitive
 *    
 *    Visual:
 *    • Milestone cards with illustrations
 *    • Tap to log achievement
 *    • Activities to encourage next milestone
 *    
 *    Screening:
 *    • M-CHAT at 18, 24 months (autism screening)
 *    • ASQ-3 elements for developmental delay
 *    • Red flag detection with early intervention referral
 * 
 * 5. FEEDING & NUTRITION (Agentic)
 *    ─────────────────────────────────
 *    Age-based (agent knows exact age):
 *    • 0-6 mo: Breastfeeding support
 *    • 6 mo: Complementary feeding introduction
 *    • 6-12 mo: Texture progression, allergen introduction
 *    • 12-24 mo: Family food transition
 *    • 2-5 yr: Picky eater support
 *    
 *    Agent Learning:
 *    • Food preferences (accepted/rejected)
 *    • Allergy tracking + pattern
 *    • Meal suggestions based on accepted foods
 */

// Example: Child Symptom Triage
@Composable
fun ChildTriageScreen(
    viewModel: ChildTriageViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when (uiState.currentStep) {
        TriageStep.SYMPTOM_SELECTION -> {
            SymptomSelectionGrid(
                childAge = uiState.childAgeMonths,
                onSymptomSelect = { viewModel.selectSymptom(it) }
            )
        }
        
        TriageStep.BREATHING_ASSESSMENT -> {
            BreathingAssessmentScreen(
                childAge = uiState.childAgeMonths,
                onComplete = { viewModel.completeBreathingAssessment(it) }
            )
        }
        
        TriageStep.DEHYDRATION_ASSESSMENT -> {
            DehydrationAssessmentScreen(
                onComplete = { viewModel.completeDehydrationAssessment(it) }
            )
        }
        
        TriageStep.RESULT -> {
            TriageResultScreen(
                result = uiState.triageResult,
                onAction = { viewModel.handleAction(it) }
            )
        }
    }
}

@Composable
fun BreathingAssessmentScreen(
    childAge: Int,
    onComplete: (BreathingAssessment) -> Unit
) {
    var breathCount by remember { mutableStateOf(0) }
    var isCountingActive by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableStateOf(60) }
    var chestIndrawing by remember { mutableStateOf<Boolean?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaaColors.background)
            .padding(24.dp)
    ) {
        Text(
            text = stringResource(R.string.breathing_assessment),
            style = MaaTypography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Breathing counter
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaaColors.surface)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.count_breaths_instruction),
                    style = MaaTypography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Large tap area
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCountingActive) MaaColors.accent else MaaColors.surfaceVariant
                        )
                        .clickable {
                            if (!isCountingActive) {
                                isCountingActive = true
                            }
                            breathCount++
                            MaaFeedback.onTap(LocalView.current)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = breathCount.toString(),
                            style = MaaTypography.dataLarge,
                            color = Color.White
                        )
                        Text(
                            text = stringResource(R.string.breaths),
                            style = MaaTypography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Timer
                Text(
                    text = "$secondsRemaining ${stringResource(R.string.seconds)}",
                    style = MaaTypography.dataMedium,
                    color = MaaColors.textSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Chest indrawing visual comparison
        Text(
            text = stringResource(R.string.chest_indrawing_question),
            style = MaaTypography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Normal breathing SVG
            ChestComparisonCard(
                title = stringResource(R.string.normal),
                svgResource = R.drawable.chest_normal,
                isSelected = chestIndrawing == false,
                onClick = { chestIndrawing = false }
            )
            
            // Chest indrawing SVG
            ChestComparisonCard(
                title = stringResource(R.string.indrawing),
                svgResource = R.drawable.chest_indrawing,
                isSelected = chestIndrawing == true,
                onClick = { chestIndrawing = true }
            )
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Result interpretation (shows after counting complete)
        if (!isCountingActive && breathCount > 0) {
            val threshold = when {
                childAge < 2 -> 60
                childAge < 12 -> 50
                else -> 40
            }
            val breathsPerMinute = breathCount  // Assuming 60-second count
            
            ResultCard(
                breathsPerMinute = breathsPerMinute,
                threshold = threshold,
                hasChestIndrawing = chestIndrawing ?: false
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        MaaButton(
            text = stringResource(R.string.continue_btn),
            enabled = !isCountingActive && breathCount > 0 && chestIndrawing != null,
            onClick = {
                onComplete(
                    BreathingAssessment(
                        breathsPerMinute = breathCount,
                        chestIndrawing = chestIndrawing!!,
                        childAgeMonths = childAge
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
    
    // Timer countdown
    LaunchedEffect(isCountingActive) {
        if (isCountingActive) {
            while (secondsRemaining > 0) {
                delay(1000)
                secondsRemaining--
            }
            isCountingActive = false
        }
    }
}
```

### Stage 6: Midlife (Ages 40-55)

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * MIDLIFE MODULE (Ages 40-55)
 * ════════════════════════════════════════════════════════════════════════════
 */

/**
 * FEATURES:
 * 
 * 1. PERIMENOPAUSE MANAGEMENT (Agentic)
 *    ─────────────────────────────────────
 *    Symptom Tracking:
 *    • Hot flashes (frequency, severity, triggers)
 *    • Night sweats
 *    • Irregular periods
 *    • Mood changes
 *    • Brain fog
 *    • Vaginal dryness
 *    • Weight changes
 *    
 *    Agent Learning:
 *    • Tracks YOUR symptom pattern
 *    • Identifies triggers: "Hot flashes worse after coffee"
 *    • Phase estimation: "Symptoms suggest late perimenopause"
 *    • Intervention tracking: What helps YOUR symptoms
 *    • HRT information: Personalized risk/benefit
 *    
 *    Menopause Determination:
 *    • Tracks cycle intervals
 *    • After 12 months: "You've reached menopause"
 * 
 * 2. CANCER SCREENING (Reminders + Education)
 *    ─────────────────────────────────────────
 *    Cervical:
 *    • VIA/Pap smear scheduling
 *    • HPV testing guidance
 *    • Facility finder
 *    
 *    Breast:
 *    • Self-exam reminders + visual guide (via body SVG)
 *    • Mammogram scheduling (40+)
 *    • Risk assessment
 * 
 * 3. NCD PREVENTION (Agentic)
 *    ──────────────────────────
 *    Tracking:
 *    • Blood pressure (home monitoring)
 *    • Blood sugar
 *    • Weight, waist circumference
 *    • Cholesterol
 *    • Thyroid
 *    
 *    Agent Intelligence:
 *    • Trend analysis: "BP creeping up over 2 weeks"
 *    • CVD risk calculator (using YOUR data)
 *    • Medication reminders with adherence tracking
 *    • Lifestyle correlation: "BP higher on stressed days"
 * 
 * 4. MENTAL WELLNESS (Agentic)
 *    ──────────────────────────
 *    Midlife-specific:
 *    • Empty nest syndrome
 *    • Caregiver stress (aging parents)
 *    • Career transitions
 *    • Hormonal mood impacts
 *    
 *    Screening:
 *    • PHQ-9, GAD-7
 *    • Caregiver burnout scale
 * 
 * 5. BONE HEALTH
 *    ───────────────
 *    • Risk assessment
 *    • DEXA scan reminders
 *    • Calcium + Vitamin D tracking
 *    • Exercise guidance
 */
```

### Stage 7: Elder Care (Ages 55+)

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * ELDER CARE MODULE (Ages 55+)
 * ════════════════════════════════════════════════════════════════════════════
 * 
 * NOTE: This module integrates with myguide.health ecosystem
 */

/**
 * FEATURES:
 * 
 * 1. CHRONIC DISEASE MANAGEMENT (Agentic)
 *    ─────────────────────────────────────
 *    Multi-Condition Tracking:
 *    • Diabetes: Glucose, HbA1c, foot checks
 *    • Hypertension: BP, medication adherence
 *    • Heart disease: Symptoms, activity tolerance
 *    • Arthritis: Pain, mobility
 *    • Thyroid: Symptoms, TSH
 *    
 *    Agent Learning:
 *    • Correlates all conditions
 *    • Pattern detection across medications
 *    • Early warning signs
 *    • Lifestyle connections
 * 
 * 2. MEDICATION MANAGEMENT (Agentic - Critical)
 *    ──────────────────────────────────────────
 *    Features:
 *    • Complete medication list
 *    • Drug interaction checker (MedGemma-powered)
 *    • Side effect tracking + correlation
 *    • Refill reminders
 *    • Adherence tracking
 *    
 *    Agent Intelligence:
 *    • Learns YOUR schedule
 *    • "You take morning pills at 7:30am. Reminder set for 7:25am."
 *    • "New medication added. Checking interactions..."
 *    • "You logged dizziness 4 times since starting new BP medicine."
 * 
 * 3. COGNITIVE HEALTH (Agentic)
 *    ────────────────────────────
 *    Screening:
 *    • Mini-Cog
 *    • AD8 (caregiver-reported)
 *    
 *    Daily Engagement:
 *    • Memory games (adapted difficulty)
 *    • Word puzzles
 *    • Calculation exercises
 *    
 *    Agent Learning:
 *    • Tracks game performance over time
 *    • Detects decline patterns
 *    • Early warning for evaluation
 * 
 * 4. CAREGIVER CONNECT
 *    ────────────────────
 *    • Family dashboard sharing
 *    • Appointment sync
 *    • Alert system for concerning changes
 *    • Care coordination
 * 
 * 5. SAFETY & EMERGENCY
 *    ────────────────────
 *    • SOS button (one-tap emergency)
 *    • Fall detection (if device supports)
 *    • Location sharing
 *    • Emergency contacts with auto-dial
 *    • Medical ID: Conditions, allergies, medications
 */
```

---

## DATA MODELS

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * CORE DATA MODELS
 * ════════════════════════════════════════════════════════════════════════════
 */

// User Profile
data class User(
    val id: String,
    val phoneNumber: String,
    val language: SupportedLanguage,
    val lifecycleStage: LifecycleStage,
    val dateOfBirth: LocalDate?,
    val biometricEnabled: Boolean,
    val subscriptionStatus: SubscriptionStatus,
    val createdAt: Instant,
    val lastActiveAt: Instant
)

enum class LifecycleStage {
    ADOLESCENCE,      // 13-19
    REPRODUCTIVE,     // 18-40, not pregnant
    PREGNANCY,        // Pregnant
    POSTPARTUM,       // 0-12 months after birth
    CHILD_CARE,       // Has child 0-5
    MIDLIFE,          // 40-55
    ELDER             // 55+
}

// Pregnancy Profile
data class Pregnancy(
    val id: String,
    val userId: String,
    val lmpDate: LocalDate?,           // Last menstrual period
    val eddDate: LocalDate?,           // Estimated due date
    val gestationalWeek: Int,          // Calculated
    val isHighRisk: Boolean,
    val riskFactors: List<RiskFactor>,
    val deliveryDate: LocalDate?,      // If delivered
    val outcome: PregnancyOutcome?
)

// Child Profile
data class Child(
    val id: String,
    val userId: String,
    val dateOfBirth: LocalDate,
    val gender: Gender,
    val ageMonths: Int,               // Calculated
    val birthWeight: Float?,
    val currentWeight: Float?,
    val currentHeight: Float?
)

// Cycle Data
data class CycleLog(
    val id: String,
    val userId: String,
    val startDate: LocalDate,
    val endDate: LocalDate?,
    val cycleLength: Int?,            // Calculated
    val flowDays: List<FlowDay>,
    val symptoms: List<CycleSymptom>
)

data class FlowDay(
    val date: LocalDate,
    val intensity: FlowIntensity,
    val symptoms: List<Symptom>
)

enum class FlowIntensity {
    SPOTTING, LIGHT, MEDIUM, HEAVY
}

// Mood/Mental Health
data class MoodLog(
    val id: String,
    val userId: String,
    val timestamp: Instant,
    val moodScore: Int,               // 1-5
    val energyLevel: Int?,            // 1-5
    val sleepQuality: Int?,           // 1-5
    val anxietyLevel: Int?,           // 1-5
    val notes: String?,
    val voiceNoteUrl: String?,
    val detectedKeywords: List<String>
)

data class ScreeningResult(
    val id: String,
    val userId: String,
    val screeningType: ScreeningType,
    val timestamp: Instant,
    val score: Int,
    val severity: Severity,
    val responses: List<Int>,
    val interpretation: String,
    val recommendations: List<String>,
    val needsFollowUp: Boolean
)

enum class ScreeningType {
    EPDS,       // Edinburgh Postnatal Depression Scale
    PHQ9,       // Patient Health Questionnaire-9
    PHQ_A,      // Adolescent version
    GAD7,       // Generalized Anxiety Disorder
    PSS,        // Perceived Stress Scale
    M_CHAT,     // Modified Checklist for Autism in Toddlers
    ASQ3        // Ages and Stages Questionnaire
}

// Symptom/Triage
data class SymptomLog(
    val id: String,
    val userId: String,
    val childId: String?,             // If child-related
    val pregnancyId: String?,         // If pregnancy-related
    val timestamp: Instant,
    val bodyRegion: BodyRegion,
    val symptomType: SymptomType,
    val severity: Severity,
    val duration: Duration?,
    val measurements: Map<String, Float>?,  // e.g., breathing rate, temp
    val triageResult: TriageResult?
)

data class TriageResult(
    val urgency: Urgency,
    val classification: String,       // e.g., "Pneumonia", "Severe dehydration"
    val action: Action,
    val instructions: List<String>,
    val referralNeeded: Boolean
)

enum class Urgency {
    EMERGENCY,    // Call ambulance / Go now
    URGENT,       // Go to hospital today
    ROUTINE,      // Schedule visit
    SELF_CARE     // Home management
}

// Vaccination
data class VaccinationRecord(
    val id: String,
    val childId: String,
    val vaccineType: VaccineType,
    val dueDate: LocalDate,
    val givenDate: LocalDate?,
    val batch: String?,
    val facility: String?,
    val sideEffects: List<SideEffect>?
)

// Growth
data class GrowthMeasurement(
    val id: String,
    val childId: String,
    val date: LocalDate,
    val weight: Float,                // kg
    val height: Float,                // cm
    val headCircumference: Float?,    // cm
    val weightForAgePercentile: Int,
    val heightForAgePercentile: Int,
    val weightForHeightPercentile: Int
)

// Patterns (Learned by Agent)
data class CyclePattern(
    val userId: String,
    val averageCycleLength: Int,
    val cycleVariability: Int,        // Standard deviation
    val averageOvulationDay: Int,
    val predictedNextPeriod: LocalDate,
    val predictedOvulation: LocalDate,
    val confidence: Float,            // 0-1
    val cyclesAnalyzed: Int,
    val irregularityFlag: Boolean,
    val pcosRiskFlag: Boolean
)

data class MoodPattern(
    val userId: String,
    val averageMoodScore: Float,
    val moodTrend: Trend,
    val cycleMoodCorrelation: Float?, // PMDD indicator
    val triggers: List<String>,
    val effectiveCopingStrategies: List<String>,
    val lastUpdated: Instant
)

enum class Trend {
    IMPROVING, STABLE, DECLINING, BASELINE
}
```

---

## OFFLINE CAPABILITIES

```kotlin
/**
 * ════════════════════════════════════════════════════════════════════════════
 * OFFLINE MODE
 * ════════════════════════════════════════════════════════════════════════════
 */

/**
 * OFFLINE SUPPORT BY FEATURE:
 * 
 * ALWAYS AVAILABLE OFFLINE (Critical Safety):
 * ───────────────────────────────────────────
 * • Danger signs assessment (full protocol)
 * • Child symptom triage (IMCI protocol)
 * • Emergency contacts + SOS
 * • Basic educational content (cached)
 * 
 * AVAILABLE OFFLINE (Premium Only):
 * ──────────────────────────────────
 * • All screening questionnaires (EPDS, PHQ-9, etc.)
 * • Kick counter
 * • Symptom logging (syncs when online)
 * • Cycle tracking + predictions
 * • Growth chart + plotting
 * • Vaccination schedule
 * • Medication reminders
 * 
 * REQUIRES INTERNET:
 * ──────────────────
 * • Voice interaction (Sarvam API)
 * • MedGemma 27B complex analysis
 * • Telemedicine booking
 * • Data sync
 * • Payment
 * 
 * IMPLEMENTATION:
 * • Room database for local storage
 * • WorkManager for background sync
 * • MedGemma 4B on-device for basic AI
 * • Cached content for education
 */

@Database(
    entities = [
        UserEntity::class,
        PregnancyEntity::class,
        ChildEntity::class,
        CycleLogEntity::class,
        MoodLogEntity::class,
        SymptomLogEntity::class,
        ScreeningResultEntity::class,
        VaccinationEntity::class,
        GrowthMeasurementEntity::class,
        MedicationEntity::class,
        // Patterns
        CyclePatternEntity::class,
        MoodPatternEntity::class,
        // Cached content
        EducationContentEntity::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class MaaDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun cycleDao(): CycleDao
    abstract fun moodDao(): MoodDao
    abstract fun symptomDao(): SymptomDao
    abstract fun pregnancyDao(): PregnancyDao
    abstract fun childDao(): ChildDao
    abstract fun screeningDao(): ScreeningDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun growthDao(): GrowthDao
    abstract fun medicationDao(): MedicationDao
    abstract fun patternDao(): PatternDao
    abstract fun contentDao(): ContentDao
}
```

---

## FREE VS PAID FEATURE MATRIX

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         FREE vs PREMIUM FEATURES                            │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  FEATURE                              │  FREE          │  PREMIUM (₹99/mo) │
│  ─────────────────────────────────────│────────────────│───────────────────│
│                                       │                │                   │
│  SAFETY (Always Free)                 │                │                   │
│  ─────────────────────                │                │                   │
│  Danger signs assessment              │  ✓ Unlimited   │  ✓ Unlimited      │
│  Child symptom triage                 │  ✓ 3/month     │  ✓ Unlimited      │
│  Emergency contacts + SOS             │  ✓ Always      │  ✓ Always         │
│                                       │                │                   │
│  MENTAL HEALTH                        │                │                   │
│  ─────────────────                    │                │                   │
│  EPDS/PHQ-9 screening                 │  ✓ 1/month     │  ✓ Unlimited      │
│  Daily mood tracking                  │  ✓ Unlimited   │  ✓ Unlimited      │
│  AI-powered insights                  │  ✗            │  ✓ Full           │
│  Trend analysis                       │  ✗            │  ✓ Full           │
│                                       │                │                   │
│  TRACKING                             │                │                   │
│  ─────────                            │                │                   │
│  Cycle tracking (basic)               │  ✓ Unlimited   │  ✓ Unlimited      │
│  Period predictions (AI)              │  ✗            │  ✓ Full           │
│  Pregnancy week-by-week               │  ✓ Basic       │  ✓ Detailed       │
│  Kick counter                         │  ✗            │  ✓ Full           │
│  Growth charts                        │  ✓ Basic       │  ✓ Trend analysis │
│  Vaccination tracker                  │  ✓ Schedule    │  ✓ Full + alerts  │
│                                       │                │                   │
│  VOICE                                │                │                   │
│  ──────                               │                │                   │
│  Voice interaction                    │  ✓ 5 min/mo    │  ✓ Unlimited      │
│                                       │                │                   │
│  AI FEATURES                          │                │                   │
│  ────────────                         │                │                   │
│  Personalized predictions             │  ✗            │  ✓ Full           │
│  Pattern detection                    │  ✗            │  ✓ Full           │
│  Correlation insights                 │  ✗            │  ✓ Full           │
│  MedGemma analysis                    │  ✓ Basic       │  ✓ Full           │
│                                       │                │                   │
│  OTHER                                │                │                   │
│  ──────                               │                │                   │
│  Offline mode (full)                  │  ✗            │  ✓ Full           │
│  Health record export (PDF)           │  ✗            │  ✓ Full           │
│  Family member access                 │  ✗            │  ✓ Up to 2        │
│  Priority support                     │  ✗            │  ✓ Full           │
│                                       │                │                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## BUILD INSTRUCTIONS

```
════════════════════════════════════════════════════════════════════════════════
                              BUILD CHECKLIST
════════════════════════════════════════════════════════════════════════════════

PRE-REQUISITES:
□ Android Studio Hedgehog or later
□ Kotlin 1.9+
□ Minimum SDK 24, Target SDK 34
□ Firebase project setup
□ Sarvam AI API key
□ Google Cloud project for MedGemma
□ Razorpay account

STEP 1: PROJECT SETUP
□ Create new Android project with Compose
□ Add Hilt dependency injection
□ Configure Firebase (Auth, Firestore, Storage)
□ Add Room database
□ Configure ProGuard for release

STEP 2: DESIGN SYSTEM
□ Implement color palette (MaaColors)
□ Implement typography (MaaTypography)
□ Add font files (Source Serif Pro, Source Sans Pro, JetBrains Mono)
□ Create haptic/sound feedback system
□ Add sound files (soft_tap.ogg, etc.)

STEP 3: SVG ASSETS
□ Create/source body SVG illustrations (adolescent, adult, pregnant, child, etc.)
□ Create mood icons (5-point scale, abstract)
□ Create symptom icons (32 set)
□ Create action icons
□ Ensure all SVGs have semantic IDs for targeting
□ Implement InteractiveBodySvg component

STEP 4: AUTHENTICATION
□ Implement phone OTP flow (Firebase Auth)
□ Implement biometric enrollment (AndroidX Biometric)
□ Implement session management

STEP 5: PAYMENTS
□ Integrate Razorpay SDK
□ Implement subscription flow
□ Implement free trial
□ Handle subscription status

STEP 6: VOICE (Sarvam AI)
□ Implement SarvamService
□ Implement VoiceInputButton component
□ Implement voice recording
□ Implement playback (TTS)

STEP 7: AI (MedGemma)
□ Set up MedGemma 4B on-device inference
□ Set up MedGemma 27B cloud via Vertex AI
□ Implement agent architecture
□ Test clinical reasoning

STEP 8: FEATURES BY LIFECYCLE
□ Implement Adolescence module
□ Implement Reproductive module
□ Implement Pregnancy module
□ Implement Postpartum module
□ Implement Child Care module
□ Implement Midlife module
□ Implement Elder Care module

STEP 9: TESTING
□ Unit tests for business logic
□ UI tests for critical flows
□ Integration tests for API
□ Accessibility testing
□ Performance testing on low-end devices

STEP 10: LOCALIZATION
□ Add string resources for all 10 languages
□ Test RTL layouts (if any)
□ Test voice I/O in all languages

STEP 11: RELEASE
□ Generate signed APK/AAB
□ Prepare Play Store listing
□ Submit for review

════════════════════════════════════════════════════════════════════════════════
```

---

## QUALITY CHECKLIST

```
Before shipping ANY screen, verify:

□ NO emojis anywhere
□ Warm color palette (no pure whites, no blues, no neons)
□ Serif headings, sans body, mono for data
□ Haptic feedback on every tap
□ Sound on key actions (subtle, medical quality)
□ Educational content on every screen
□ Bilingual labels (English + selected language)
□ Voice option available on all input screens
□ Works offline for critical features
□ Accessibility: 48dp touch targets minimum
□ Loads quickly (<2s on mid-range device)
□ Visual-first, text-last input design
□ Body SVG has semantic IDs and responds to tap
□ Agent provides personalized, not generic responses
```

---

**END OF SPECIFICATION**

This document contains everything needed to build the Maa app.
Iterate until flagship quality is achieved.
