# Maa Health App - Automated Test Suite

## Overview

This document describes the automated test suite for the Maa Women's Health Companion app. The test suite includes unit tests, integration tests, and end-to-end (E2E) regression tests.

## Test Framework

- **Unit Tests**: JUnit 4.13.2 + MockK 1.13.8
- **Coroutines Testing**: kotlinx-coroutines-test 1.7.3
- **UI Tests**: Espresso 3.5.1 + Compose UI Test JUnit4
- **Android Test Runner**: AndroidJUnitRunner

## Running Tests

### Unit Tests (No device required)
```bash
./gradlew test
```

### Instrumented Tests (Requires device/emulator)
```bash
./gradlew connectedAndroidTest
```

### All Tests
```bash
./gradlew test connectedAndroidTest
```

---

## Unit Tests

### 1. UserRepositoryTest
**Location**: `app/src/test/java/com/maa/health/data/repository/UserRepositoryTest.kt`

| Test | Description | Status |
|------|-------------|--------|
| `getCurrentUser returns null when no user id in preferences` | Verifies null is returned when no user exists | Ready |
| `getCurrentUser returns user when user id exists` | Verifies user is fetched correctly from database | Ready |
| `createUser inserts user and sets user id in preferences` | Tests user creation flow | Ready |
| `setLifecycleStages updates preferences` | Tests lifecycle stage persistence | Ready |
| `setLanguage updates preferences` | Tests language preference persistence | Ready |
| `isOnboardingComplete returns correct value` | Tests onboarding completion status | Ready |
| `isPremiumUser always returns true for free app` | **Regression**: Verifies all features are free | Ready |
| `deleteAllUserData clears user and preferences` | Tests data deletion | Ready |
| `logout clears all preferences` | Tests logout functionality | Ready |

### 2. CycleRepositoryTest
**Location**: `app/src/test/java/com/maa/health/data/repository/CycleRepositoryTest.kt`

| Test | Description | Status |
|------|-------------|--------|
| `getCycleLogs returns logs for user` | Verifies cycle log retrieval | Ready |
| `getCurrentCycle returns latest cycle` | Tests fetching current cycle | Ready |
| `startNewCycle creates and inserts cycle log` | Tests cycle creation | Ready |
| `endCurrentCycle updates cycle with end date` | Tests cycle completion | Ready |
| `getCyclePattern returns pattern for user` | Tests pattern retrieval | Ready |
| `predictNextPeriod calculates based on pattern` | Tests prediction algorithm | Ready |
| `deleteCycleLog removes log from database` | Tests cycle deletion | Ready |
| `getCycleCount returns correct count` | Tests cycle counting | Ready |

### 3. MoodRepositoryTest
**Location**: `app/src/test/java/com/maa/health/data/repository/MoodRepositoryTest.kt`

| Test | Description | Status |
|------|-------------|--------|
| `getMoodLogs returns logs for user` | Verifies mood log retrieval | Ready |
| `logMood creates and inserts mood log` | Tests mood logging | Ready |
| `logMood validates mood score range` | Tests input validation (1-5) | Ready |
| `getMoodPattern returns pattern for user` | Tests pattern retrieval | Ready |
| `getAverageMoodScore calculates correctly` | Tests average calculation | Ready |
| `getMoodTrend detects declining trend` | Tests trend detection algorithm | Ready |
| `deleteMoodLog removes log from database` | Tests mood log deletion | Ready |
| `getRecentMoodLogs returns logs within time range` | Tests time-based filtering | Ready |

### 4. HealthMonitoringServiceTest
**Location**: `app/src/test/java/com/maa/health/service/HealthMonitoringServiceTest.kt`

| Test | Description | Status |
|------|-------------|--------|
| `analyzeHealthData returns empty list when no issues` | Tests baseline behavior | Ready |
| `analyzeHealthData detects danger signs` | Tests critical symptom detection | Ready |
| `analyzeHealthData detects recurring symptoms` | Tests pattern detection (3+ occurrences) | Ready |
| `analyzeHealthData detects mood decline` | Tests mental health trend detection | Ready |
| `analyzeHealthData detects PMDD indicator` | Tests cycle-mood correlation | Ready |
| `analyzeHealthData detects cycle irregularity` | Tests cycle pattern analysis | Ready |
| `analyzeHealthData detects PCOS risk` | Tests PCOS risk indicator | Ready |

### 5. CloudSyncServiceTest
**Location**: `app/src/test/java/com/maa/health/service/CloudSyncServiceTest.kt`

| Test | Description | Status |
|------|-------------|--------|
| `isPremiumUser always returns true for free app` | **Regression**: Cloud sync is free | Ready |
| `syncToCloud fails when user not authenticated` | Tests auth requirement | Ready |
| `restoreFromCloud fails when user not authenticated` | Tests auth requirement | Ready |
| `getLastSyncTime returns null when never synced` | Tests initial state | Ready |

### 6. UserPreferencesTest
**Location**: `app/src/test/java/com/maa/health/data/UserPreferencesTest.kt`

| Test | Description | Status |
|------|-------------|--------|
| `isPremium always returns true for free app` | **Regression**: Premium always true | Ready |
| `UserPreferencesData canUseVoice always returns true` | **Regression**: Voice is free | Ready |
| `UserPreferencesData canUseScreening always returns true` | **Regression**: Screening is free | Ready |
| `UserPreferencesData canUseTriage always returns true` | **Regression**: Triage is free | Ready |
| `default language is Hindi` | Tests default setting | Ready |
| `default notifications are enabled` | Tests default setting | Ready |
| `default biometric is disabled` | Tests default setting | Ready |
| `default onboarding is not complete` | Tests default setting | Ready |
| `lifecycle stages are empty by default` | Tests default setting | Ready |
| `setLanguage updates datastore` | Tests preference update | Ready |
| `setLifecycleStages updates datastore` | Tests preference update | Ready |
| `clearAllPreferences clears datastore` | Tests preference clearing | Ready |

---

## E2E Regression Tests

### 1. OnboardingFlowTest
**Location**: `app/src/androidTest/java/com/maa/health/ui/OnboardingFlowTest.kt`

| Test | Description | Priority |
|------|-------------|----------|
| `splashScreen_displaysAppName` | Verifies app loads correctly | High |
| `languageSelection_showsAllLanguages` | Tests 22 language availability | High |
| `languageSelection_hindiIsDefault` | Verifies Hindi as default | Medium |
| `languageSelection_selectingLanguageNavigatesToPhoneAuth` | Tests navigation flow | High |
| `phoneAuth_validatesPhoneNumberFormat` | Tests input validation | High |
| `phoneAuth_acceptsValidIndianNumber` | Tests valid phone acceptance | High |
| `phoneAuth_skipButtonAvailable` | Tests skip option exists | Medium |
| `profileSetup_displaysRequiredFields` | Tests profile fields | High |
| `lifecycleStageSelection_showsAllStages` | Tests all 7 lifecycle stages | High |
| `lifecycleStageSelection_allowsMultipleSelection` | Tests multi-select | Medium |
| `completeOnboarding_navigatesToHome` | E2E onboarding completion | Critical |

### 2. NavigationTest
**Location**: `app/src/androidTest/java/com/maa/health/ui/NavigationTest.kt`

| Test | Description | Priority |
|------|-------------|----------|
| `bottomNavigation_allTabsAccessible` | Tests all 5 bottom tabs | Critical |
| `homeScreen_navigatesToBodyMap` | Tests symptom checker access | High |
| `homeScreen_navigatesToCycleTracking` | Tests cycle tracking access | High |
| `homeScreen_navigatesToMentalHealth` | Tests mental health access | High |
| `backNavigation_returnsToPreviewScreen` | Tests back button | High |
| `emergencyButton_navigatesToEmergencyScreen` | Tests emergency access | Critical |
| `youHub_showsAllSettings` | Tests settings visibility | High |
| `youHub_noUpgradeToPremiumButton` | **Regression**: No premium upsell | Critical |
| `youHub_cloudSyncAccessibleWithoutPremium` | **Regression**: Cloud sync free | Critical |

### 3. HealthTrackingFlowTest
**Location**: `app/src/androidTest/java/com/maa/health/ui/HealthTrackingFlowTest.kt`

| Test | Description | Priority |
|------|-------------|----------|
| `cycleTracking_displaysCalendar` | Tests cycle UI | High |
| `cycleTracking_canLogPeriod` | Tests period logging | Critical |
| `cycleTracking_showsPrediction` | Tests prediction display | High |
| `mentalHealth_displaysMoodOptions` | Tests mood UI | High |
| `mentalHealth_canLogMood` | Tests mood logging | Critical |
| `mentalHealth_showsScreeningOptions` | Tests screening access | High |
| `mentalHealth_screeningsFreeForAll` | **Regression**: Free screenings | Critical |
| `mentalHealth_crisisSupportAccessible` | Tests crisis support | Critical |
| `symptomTriage_displaysBodyMap` | Tests body map UI | High |
| `symptomTriage_canSelectBodyRegion` | Tests region selection | High |
| `symptomTriage_hasEmergencyOption` | Tests emergency access | Critical |
| `symptomTriage_triageFreeForAll` | **Regression**: Free triage | Critical |

---

## Regression Tests Summary

### Payment Removal Verification
The following tests verify that the app is **FREE** for all users:

1. `UserRepositoryTest.isPremiumUser always returns true for free app`
2. `CloudSyncServiceTest.isPremiumUser always returns true for free app`
3. `UserPreferencesTest.isPremium always returns true for free app`
4. `UserPreferencesTest.canUseVoice always returns true`
5. `UserPreferencesTest.canUseScreening always returns true`
6. `UserPreferencesTest.canUseTriage always returns true`
7. `NavigationTest.youHub_noUpgradeToPremiumButton`
8. `NavigationTest.youHub_cloudSyncAccessibleWithoutPremium`
9. `HealthTrackingFlowTest.mentalHealth_screeningsFreeForAll`
10. `HealthTrackingFlowTest.symptomTriage_triageFreeForAll`

---

## APIs Required (Bypassed/Mocked)

The following APIs need to be configured for full functionality:

| API | Purpose | Current Status |
|-----|---------|----------------|
| **Firebase Auth** | Phone authentication & OTP | Mocked in tests |
| **Firebase Firestore** | Cloud data sync | Mocked in tests |
| **Sarvam AI** | Speech-to-text, translation | Requires API key |
| **Claude/Gemini AI** | AI symptom triage | Requires API keys (CLAUDE_API_KEY, GEMINI_API_KEY) |

### To Add API Keys:
1. Edit `gradle.properties`:
```properties
SARVAM_API_KEY=your_sarvam_api_key_here
```

2. Add `google-services.json` for Firebase configuration

---

## Test Coverage Goals

| Category | Target | Current |
|----------|--------|---------|
| Unit Tests | 80% | ~70% |
| Integration Tests | 60% | ~50% |
| E2E Tests | Key flows | Implemented |

---

## Continuous Integration

Add to CI/CD pipeline:
```yaml
- name: Run Unit Tests
  run: ./gradlew test

- name: Run Instrumented Tests
  run: ./gradlew connectedAndroidTest
```

---

## Known Limitations

1. **Firebase Tests**: Require Firebase emulator or actual device
2. **Sarvam AI Tests**: Require API key for voice features
3. **Claude/Gemini AI Tests**: Require API keys for AI features (CLAUDE_API_KEY, GEMINI_API_KEY)
4. **Biometric Tests**: Require device with fingerprint sensor

---

## Security Tests Covered

1. No hardcoded secrets in tests
2. Sensitive headers are redacted in logging
3. All backup data is excluded
4. No SMS permissions (removed)
5. Cleartext traffic disabled globally

---

*Last Updated: 2026-01-02*
*Test Suite Version: 1.0.0*
