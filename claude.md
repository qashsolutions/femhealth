# Maa Health App - Development Notes

## Project Overview
Maa Health is a women's health app targeting developing nations (South Asia and Africa).

## Authentication Flow
- **Primary**: Face ID / Biometric authentication
- **Secondary**: OTP sent to mobile number
- No passwords required - frictionless signup for free app

## Supported Regions
The app is available in:
- **South Asia**: India, Pakistan, Bangladesh, Nepal, Sri Lanka, Afghanistan, Bhutan, Maldives
- **Africa**: Nigeria, Kenya, Ethiopia, Ghana, Tanzania, Uganda, South Africa, Egypt, Morocco, Algeria, Sudan, DR Congo, Angola, Mozambique, Madagascar, Cameroon, Ivory Coast, Niger, Burkina Faso, Mali, Malawi, Zambia, Senegal, Chad, Somalia, Zimbabwe, Guinea, Rwanda, Benin, Burundi, Tunisia, South Sudan, Togo, Sierra Leone, Libya, Liberia, Mauritania, Eritrea, Gambia, Botswana, Namibia, Gabon, Lesotho, Guinea-Bissau, Equatorial Guinea, Mauritius, Eswatini, Djibouti, Comoros, Cabo Verde, Sao Tome and Principe, Seychelles

## Language Support
- **India**: 22 scheduled languages via Sarvam AI (with STT/TTS for 11 languages)
- **Other countries**: Google Translate API based on phone country code

---

## AI Stack

### Primary: Claude Opus 4.6 (Anthropic)
- Model ID: `claude-opus-4-6`
- Used for: Symptom triage, screening interpretation, health education, pattern analysis
- API: Anthropic Messages API (`api.anthropic.com/v1/messages`)
- Why: Best reasoning, 1M context window, safety-conscious, culturally sensitive responses

### Backup: Gemini 3 Flash (Google)
- Model ID: `gemini-3-flash`
- Used for: Automatic failover when Claude is unavailable
- API: Gemini API (`generativelanguage.googleapis.com/v1/models`)
- Why: Near-Pro reasoning, low latency, cost-effective fallback

### On-Device: Rule-Based Triage Engine
- No model download required
- WHO pregnancy danger signs + IMCI child health protocols
- <50ms inference, works fully offline

---

## Future Development - Reserved Features

### Aadhaar Integration (India Only)
**Status**: Reserved for future development
**Date**: 2026-01-02
**Notes**: Aadhaar (UIDAI) authentication is planned for Indian users only. This would enable:
- Aadhaar-based eKYC verification
- Aadhaar OTP authentication as alternative to SMS OTP
- Integration with DigiLocker for document verification

**Implementation Notes**:
- Requires UIDAI API integration
- Must comply with Aadhaar Act regulations
- Only applicable for Indian phone numbers (+91)

### ABDM Integration (India Only)
**Status**: Reserved for future development
**Date**: 2026-01-02
**Notes**: Ayushman Bharat Digital Mission (ABDM) integration is planned for Indian users. This would enable:
- ABHA (Ayushman Bharat Health Account) creation
- Health records linking via PHR (Personal Health Records)
- Integration with Health Information Exchange
- Consent-based health data sharing with healthcare providers

**Implementation Notes**:
- Requires ABDM sandbox and production API access
- Must implement M1-M3 milestones for certification
- Health ID creation and linking workflows
- Consent manager integration

---

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose
- **Architecture**: MVVM + Clean Architecture
- **DI**: Hilt
- **Database**: Room
- **Preferences**: DataStore
- **AI (Primary)**: Claude Opus 4.6 (Anthropic)
- **AI (Backup)**: Gemini 3 Flash (Google)
- **Speech/Translation (India)**: Sarvam AI
- **Translation (Other regions)**: Google Translate API
