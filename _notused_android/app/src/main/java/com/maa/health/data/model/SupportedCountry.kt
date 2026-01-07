package com.maa.health.data.model

/**
 * Supported countries for Maa Health app
 *
 * The app is available in developing nations across South Asia and Africa.
 * Each country has a dial code, flag emoji, and default language code for Google Translate.
 */
enum class SupportedCountry(
    val countryCode: String,      // ISO 3166-1 alpha-2
    val dialCode: String,         // International dialing code
    val name: String,             // English name
    val flag: String,             // Flag emoji
    val defaultLanguage: String,  // ISO 639-1 language code for Google Translate
    val region: Region
) {
    // ═══════════════════════════════════════════════════════════════════════════
    // SOUTH ASIA
    // ═══════════════════════════════════════════════════════════════════════════

    INDIA("IN", "+91", "India", "🇮🇳", "hi", Region.SOUTH_ASIA),
    PAKISTAN("PK", "+92", "Pakistan", "🇵🇰", "ur", Region.SOUTH_ASIA),
    BANGLADESH("BD", "+880", "Bangladesh", "🇧🇩", "bn", Region.SOUTH_ASIA),
    NEPAL("NP", "+977", "Nepal", "🇳🇵", "ne", Region.SOUTH_ASIA),
    SRI_LANKA("LK", "+94", "Sri Lanka", "🇱🇰", "si", Region.SOUTH_ASIA),
    AFGHANISTAN("AF", "+93", "Afghanistan", "🇦🇫", "ps", Region.SOUTH_ASIA),
    BHUTAN("BT", "+975", "Bhutan", "🇧🇹", "dz", Region.SOUTH_ASIA),
    MALDIVES("MV", "+960", "Maldives", "🇲🇻", "dv", Region.SOUTH_ASIA),

    // ═══════════════════════════════════════════════════════════════════════════
    // AFRICA
    // ═══════════════════════════════════════════════════════════════════════════

    // East Africa
    KENYA("KE", "+254", "Kenya", "🇰🇪", "sw", Region.AFRICA),
    ETHIOPIA("ET", "+251", "Ethiopia", "🇪🇹", "am", Region.AFRICA),
    TANZANIA("TZ", "+255", "Tanzania", "🇹🇿", "sw", Region.AFRICA),
    UGANDA("UG", "+256", "Uganda", "🇺🇬", "sw", Region.AFRICA),
    RWANDA("RW", "+250", "Rwanda", "🇷🇼", "rw", Region.AFRICA),
    BURUNDI("BI", "+257", "Burundi", "🇧🇮", "rn", Region.AFRICA),
    SOUTH_SUDAN("SS", "+211", "South Sudan", "🇸🇸", "en", Region.AFRICA),
    ERITREA("ER", "+291", "Eritrea", "🇪🇷", "ti", Region.AFRICA),
    DJIBOUTI("DJ", "+253", "Djibouti", "🇩🇯", "fr", Region.AFRICA),
    SOMALIA("SO", "+252", "Somalia", "🇸🇴", "so", Region.AFRICA),

    // West Africa
    NIGERIA("NG", "+234", "Nigeria", "🇳🇬", "en", Region.AFRICA),
    GHANA("GH", "+233", "Ghana", "🇬🇭", "en", Region.AFRICA),
    IVORY_COAST("CI", "+225", "Ivory Coast", "🇨🇮", "fr", Region.AFRICA),
    SENEGAL("SN", "+221", "Senegal", "🇸🇳", "fr", Region.AFRICA),
    MALI("ML", "+223", "Mali", "🇲🇱", "fr", Region.AFRICA),
    BURKINA_FASO("BF", "+226", "Burkina Faso", "🇧🇫", "fr", Region.AFRICA),
    NIGER("NE", "+227", "Niger", "🇳🇪", "fr", Region.AFRICA),
    GUINEA("GN", "+224", "Guinea", "🇬🇳", "fr", Region.AFRICA),
    BENIN("BJ", "+229", "Benin", "🇧🇯", "fr", Region.AFRICA),
    TOGO("TG", "+228", "Togo", "🇹🇬", "fr", Region.AFRICA),
    SIERRA_LEONE("SL", "+232", "Sierra Leone", "🇸🇱", "en", Region.AFRICA),
    LIBERIA("LR", "+231", "Liberia", "🇱🇷", "en", Region.AFRICA),
    MAURITANIA("MR", "+222", "Mauritania", "🇲🇷", "ar", Region.AFRICA),
    GAMBIA("GM", "+220", "Gambia", "🇬🇲", "en", Region.AFRICA),
    GUINEA_BISSAU("GW", "+245", "Guinea-Bissau", "🇬🇼", "pt", Region.AFRICA),
    CABO_VERDE("CV", "+238", "Cabo Verde", "🇨🇻", "pt", Region.AFRICA),

    // Central Africa
    DR_CONGO("CD", "+243", "DR Congo", "🇨🇩", "fr", Region.AFRICA),
    CAMEROON("CM", "+237", "Cameroon", "🇨🇲", "fr", Region.AFRICA),
    CHAD("TD", "+235", "Chad", "🇹🇩", "fr", Region.AFRICA),
    CENTRAL_AFRICAN_REPUBLIC("CF", "+236", "Central African Republic", "🇨🇫", "fr", Region.AFRICA),
    GABON("GA", "+241", "Gabon", "🇬🇦", "fr", Region.AFRICA),
    EQUATORIAL_GUINEA("GQ", "+240", "Equatorial Guinea", "🇬🇶", "es", Region.AFRICA),
    CONGO("CG", "+242", "Congo", "🇨🇬", "fr", Region.AFRICA),
    SAO_TOME_AND_PRINCIPE("ST", "+239", "São Tomé and Príncipe", "🇸🇹", "pt", Region.AFRICA),

    // Southern Africa
    SOUTH_AFRICA("ZA", "+27", "South Africa", "🇿🇦", "en", Region.AFRICA),
    ANGOLA("AO", "+244", "Angola", "🇦🇴", "pt", Region.AFRICA),
    MOZAMBIQUE("MZ", "+258", "Mozambique", "🇲🇿", "pt", Region.AFRICA),
    MADAGASCAR("MG", "+261", "Madagascar", "🇲🇬", "mg", Region.AFRICA),
    MALAWI("MW", "+265", "Malawi", "🇲🇼", "en", Region.AFRICA),
    ZAMBIA("ZM", "+260", "Zambia", "🇿🇲", "en", Region.AFRICA),
    ZIMBABWE("ZW", "+263", "Zimbabwe", "🇿🇼", "en", Region.AFRICA),
    BOTSWANA("BW", "+267", "Botswana", "🇧🇼", "en", Region.AFRICA),
    NAMIBIA("NA", "+264", "Namibia", "🇳🇦", "en", Region.AFRICA),
    LESOTHO("LS", "+266", "Lesotho", "🇱🇸", "en", Region.AFRICA),
    ESWATINI("SZ", "+268", "Eswatini", "🇸🇿", "en", Region.AFRICA),
    MAURITIUS("MU", "+230", "Mauritius", "🇲🇺", "en", Region.AFRICA),
    COMOROS("KM", "+269", "Comoros", "🇰🇲", "ar", Region.AFRICA),
    SEYCHELLES("SC", "+248", "Seychelles", "🇸🇨", "en", Region.AFRICA),

    // North Africa
    EGYPT("EG", "+20", "Egypt", "🇪🇬", "ar", Region.AFRICA),
    MOROCCO("MA", "+212", "Morocco", "🇲🇦", "ar", Region.AFRICA),
    ALGERIA("DZ", "+213", "Algeria", "🇩🇿", "ar", Region.AFRICA),
    SUDAN("SD", "+249", "Sudan", "🇸🇩", "ar", Region.AFRICA),
    TUNISIA("TN", "+216", "Tunisia", "🇹🇳", "ar", Region.AFRICA),
    LIBYA("LY", "+218", "Libya", "🇱🇾", "ar", Region.AFRICA);

    companion object {
        /**
         * Get all supported countries sorted by name
         */
        fun getAll(): List<SupportedCountry> = entries.sortedBy { it.name }

        /**
         * Get countries by region
         */
        fun getByRegion(region: Region): List<SupportedCountry> =
            entries.filter { it.region == region }.sortedBy { it.name }

        /**
         * Find country by dial code (e.g., "+91")
         */
        fun fromDialCode(dialCode: String): SupportedCountry? =
            entries.find { it.dialCode == dialCode }

        /**
         * Find country by ISO country code (e.g., "IN")
         */
        fun fromCountryCode(countryCode: String): SupportedCountry? =
            entries.find { it.countryCode.equals(countryCode, ignoreCase = true) }

        /**
         * Get default country (India)
         */
        fun getDefault(): SupportedCountry = INDIA

        /**
         * Check if a country uses Sarvam AI (India only) or Google Translate
         */
        fun usesSarvamAI(country: SupportedCountry): Boolean = country == INDIA

        /**
         * Get popular countries (shown first in picker)
         */
        fun getPopular(): List<SupportedCountry> = listOf(
            INDIA, NIGERIA, KENYA, BANGLADESH, PAKISTAN,
            ETHIOPIA, SOUTH_AFRICA, GHANA, TANZANIA, EGYPT
        )
    }
}

/**
 * Geographic regions for country grouping
 */
enum class Region(val displayName: String) {
    SOUTH_ASIA("South Asia"),
    AFRICA("Africa")
}
