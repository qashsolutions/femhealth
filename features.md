# Maa Health - Features

Maa (meaning "Mother" in Hindi) is a comprehensive women's health companion covering all lifecycle stages from adolescence through elder years. The app is completely free, supports 22 Indian languages, and integrates clinical-grade AI for symptom triage, mental health screening, and personalized health education.

---

## AI Features

### AI Architecture (Claude + Gemini with On-Device Triage)

The app uses a three-tier AI architecture defined in `MaaAIService.kt`, `ClaudeAIService.kt`, `GeminiAIService.kt`, and `OnDeviceTriageEngine.kt`.

**On-device rule-based triage engine (OnDeviceTriageEngine)**
- Runs entirely on-device with zero network dependency and zero model download
- Uses validated WHO pregnancy danger sign protocols and IMCI child health protocols
- Handles rule-based triage for fever, gastrointestinal symptoms, and pain with confidence scoring
- Inference time: <50ms (pure rule-based, no ML overhead)
- Automatically escalates to cloud AI when confidence is below 85%

**Primary cloud AI: Claude Opus 4.6 (Anthropic)**
- Model: `claude-opus-4-6` via Anthropic Messages API
- 1M token context window for complex multi-turn reasoning
- Handles complex triage decisions when on-device confidence is insufficient
- Interprets validated mental health screening instruments (EPDS, PHQ-9, PHQ-A, GAD-7) with severity classification, recommendations, and crisis resource routing
- Streams personalized health education content based on user lifecycle stage, language, and literacy level
- Analyzes symptom patterns over time to detect mood trends, cycle correlations, and recurring issues
- Excels at culturally appropriate health communication for South Asian and African contexts
- Safety-conscious medical guidance with careful reasoning

**Backup cloud AI: Gemini 3 Flash (Google)**
- Model: `gemini-3-flash` via Gemini API
- Automatic failover when Claude API is unavailable or returns errors
- Near-Pro-level reasoning at lower latency for high-availability
- Includes network-failure fallback that returns conservative triage guidance
- Same validated screening algorithms as Claude (EPDS, PHQ-9, PHQ-A, GAD-7 scoring is deterministic)

**AI-powered symptom triage**
- Multi-step assessment: body region selection, symptom identification, severity rating, contextual evaluation
- Urgency classification into four levels: Emergency, Urgent, Routine, Self-Care
- Action routing: call emergency, go to hospital now, go to hospital today, schedule visit, or home management
- Context-aware decisions factoring in pregnancy status, gestational week, postpartum state, child age, chronic conditions, and current medications
- Confidence-scored results with automatic cloud escalation for ambiguous cases
- Three-layer failover: on-device rules, Claude (primary), Gemini (backup)

**AI-powered screening interpretation**
- EPDS scoring with self-harm detection on question 10, triggering emergency-level resources (iCall, Vandrevala Foundation helplines)
- PHQ-9 scoring across five severity tiers (none through severe) with self-harm flagging on question 9
- GAD-7 anxiety scoring with tiered recommendations
- PHQ-A adolescent-specific interpretation with age-appropriate resources (NIMHANS helpline)
- All interpretations include follow-up scheduling, coping recommendations, and localized support resources

### Sarvam AI Voice Integration

Defined in `SarvamApiService.kt`, the app integrates Sarvam AI for multilingual voice interaction.

**Speech-to-Text (STT)**
- Converts voice input to text in 11 Indian languages
- Uses the Sarvam-1 model optimized for Indian accents, dialects, and code-mixing (e.g., Hindi-English)
- Enables voice-first interaction for users with low text literacy

**Text-to-Speech (TTS)**
- Converts app text and AI responses to natural speech
- Default female voice ("meera") across all supported languages
- Enables the app to read out triage instructions, screening results, and health education

**Supported voice languages**: Hindi, Bengali, Kannada, Malayalam, Marathi, Odia, Punjabi, Tamil, Telugu, Gujarati, English

### Background Health Monitoring AI

Defined in `HealthMonitoringService.kt`, a background service runs every 6 hours via WorkManager to analyze health data and trigger notifications.

- Detects logged danger signs within the past 24 hours and triggers immediate alerts
- Identifies recurring symptom patterns (3+ occurrences in 7 days)
- Monitors mood trends for sustained decline and triggers screening recommendations
- Correlates mood with menstrual cycle data to detect potential PMDD (correlation threshold >0.7)
- Flags cycle irregularities and PCOS risk indicators from learned cycle patterns
- Delivers AI-generated push notifications through categorized channels (danger alerts vs. health insights)

### AI-Driven Cycle Predictions

The `CyclePattern` model learns from logged data to generate personalized predictions.

- Learns individual average cycle length and variability (standard deviation)
- Predicts next period date and ovulation day based on personal history, not assumed day-14 ovulation
- Calculates fertile windows with confidence scores
- Flags cycle irregularity when variability exceeds thresholds
- Detects PCOS risk from cycle pattern analysis
- Improves prediction accuracy as more cycles are logged

---

## Health Features by Lifecycle Stage

### Adolescence (Ages 13-19)

- **Period tracking**: Calendar-based logging with flow intensity (spotting/light/medium/heavy) and 9 symptom types (cramps, headache, bloating, breast tenderness, mood swings, fatigue, acne, back pain, nausea)
- **Mental health screening**: PHQ-A (adolescent depression) and GAD-7 (anxiety) with age-appropriate resources
- **Mood tracking**: 5-point scale with energy, sleep quality, anxiety levels, and optional voice notes
- **Nutrition and anemia prevention**: Iron-rich food tracking, IFA supplement reminders, fatigue/pallor monitoring
- **Body literacy education**: Interactive SVG-based reproductive anatomy with age-appropriate content and myth-busting
- **Safety module**: Good/bad touch education, POCSO awareness, Childline (1098) access

### Reproductive Years (Ages 18-40)

- **Fertility awareness**: Three modes (avoid pregnancy, neutral tracking, trying to conceive) with personalized ovulation learning and fertile window calculation
- **PCOS management**: Cycle irregularity analysis, weight tracking, low-GI diet suggestions, symptom clustering
- **Contraception guidance**: Method selection support, side effect tracking, refill reminders, emergency contraception access
- **Pre-conception health**: Folic acid reminders, anemia screening, vaccination tracking
- **Mental wellness**: PHQ-9, GAD-7, PSS screenings with PMDD detection from cycle-mood correlation
- **Sexual health**: STI symptom tracking, UTI monitoring, testing reminders

### Pregnancy

- **Week-by-week tracking**: Interactive pregnant body SVG that updates per gestational week showing fetus position and development, with size comparisons
- **Danger signs assessment**: WHO-defined danger sign detection covering vaginal bleeding, severe headache, blurred vision (pre-eclampsia), convulsions, leaking fluid, reduced fetal movement, and difficulty breathing -- with urgency classification and emergency routing
- **Kick counter**: Tap interface for fetal movement counting with session duration tracking and 10-kick completion detection
- **Contraction timer**: Records contraction start/end times, calculates duration and intervals between contractions, maintains session history
- **ANC companion**: Visit scheduling with proactive reminders, pre/post-visit tracking, visit gap detection
- **Risk factor tracking**: 12 risk factors including previous cesarean, maternal age, hypertension, diabetes, anemia, multiple pregnancy, and BMI extremes
- **Mental health**: EPDS screening (Edinburgh Postnatal Depression Scale) with self-harm detection and crisis routing
- **Nutrition**: IFA and calcium supplement adherence tracking, weight monitoring with personalized targets
- **Delivery preparation**: Birth plan builder, labor signs education, hospital bag checklist

### Postpartum (0-12 Months)

- **Recovery tracking**: Lochia monitoring, wound healing (C-section/perineal), pelvic floor exercises, uterus involution
- **Mental health**: EPDS postpartum screening with bonding assessment, postpartum depression/anxiety detection, crisis escalation
- **Breastfeeding support**: Latch quality assessment, engorgement/mastitis recognition, milk production tracking, pumping schedule management
- **Family planning**: Lactational amenorrhea (LAM) tracking, contraception timing, return-of-menstruation prediction

### Child Care (Ages 0-5)

- **Symptom triage (IMCI protocol)**: WHO/IMCI-based pediatric assessment with color-coded urgency, breathing rate evaluation, dehydration classification, pneumonia detection, and age-specific handling (young infants vs. older children)
- **Vaccination tracking**: Complete Indian Universal Immunization Schedule with 36 vaccine types (BCG, OPV, Hepatitis B, Pentavalent, DPT, Rotavirus, PCV, Measles, MR, JE, Vitamin A), due date reminders, side effect monitoring, batch/facility tracking
- **Growth monitoring**: Weight, height, and head circumference measurements with WHO growth chart percentiles (weight-for-age, height-for-age, weight-for-height) and malnutrition risk scoring
- **Developmental milestones**: 24 milestone types across gross motor, fine motor, language, and social domains with expected age tracking and achievement recording
- **Child profile management**: Name, DOB, gender, birth/current weight and height with automatic age calculation in months and days

### Midlife (Ages 40-55)

- **Menopause transition**: Hot flash tracking with triggers, irregular bleeding patterns, symptom cluster analysis, menopause stage prediction
- **Cardiovascular health**: Blood pressure tracking, heart rate monitoring, cardiovascular risk assessment
- **Bone health**: Osteoporosis risk scoring, calcium/vitamin D intake, weight-bearing exercise logging
- **Cognitive health**: Memory concerns, sleep quality monitoring, cognitive screening

### Elder Years (55+)

- **Chronic condition management**: Hypertension, diabetes, arthritis tracking with medication adherence and drug interaction checking
- **Preventive care**: Mammogram scheduling, cervical cancer screening, blood pressure and cholesterol management
- **Medication management**: Dosage tracking, frequency scheduling, reminder times, skip-reason logging, active/inactive medication status
- **Cognitive and sensory health**: Hearing concerns, vision changes, memory tracking, fall risk assessment

---

## Language and Localization

- **22 Indian languages** supported (all scheduled languages)
- **11 languages with full voice** (STT + TTS): Hindi, Bengali, Kannada, Malayalam, Marathi, Odia, Punjabi, Tamil, Telugu, Gujarati, English
- **11 languages with translation only**: Assamese, Bodo, Dogri, Kashmiri, Konkani, Maithili, Manipuri, Nepali, Sanskrit, Santali, Sindhi, Urdu
- **Literacy-level adaptation**: Content adjusts across basic (simple language, more visuals), standard, and advanced (medical terminology acceptable) levels
- **African region support**: 44+ African countries supported via Google Translate API fallback
- **Code-mixing support**: Handles mixed-language input (e.g., Hindi-English) through Sarvam AI

---

## Authentication and Security

- **Biometric authentication**: Face ID and fingerprint via AndroidX Biometric as the primary login method
- **Phone OTP fallback**: Firebase Auth phone-based verification, no passwords required
- **30-day session persistence**
- **Security hardening**: App backup disabled, cleartext traffic prohibited, HTTP logging secured (BASIC level, headers redacted), SMS permissions removed, API key headers redacted (x-api-key, anthropic-api-key, Authorization)

---

## Architecture and Infrastructure

- **Tech stack**: Kotlin, Jetpack Compose, Material 3, MVVM + Clean Architecture, Hilt dependency injection
- **AI stack**: Claude Opus 4.6 (primary), Gemini 3 Flash (backup), on-device rule-based triage (offline)
- **Local storage**: Room (SQLite) database with DataStore preferences for offline-first operation
- **Cloud backend**: Firebase (Auth, Firestore, Storage, Functions, Analytics) with offline-first sync
- **Cloud sync**: Bidirectional Firestore sync with offline queuing via `CloudSyncService.kt`
- **Data export**: PDF and JSON export of health records via `DataExportService.kt`
- **Push notifications**: Firebase Cloud Messaging with categorized notification channels (danger alerts, health insights, reminders)
- **Background processing**: WorkManager for periodic health monitoring every 6 hours
- **Testing**: JUnit 4 unit tests, MockK mocking, Compose UI tests, Espresso E2E tests covering onboarding, navigation, and health tracking flows
