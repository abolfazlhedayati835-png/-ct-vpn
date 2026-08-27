package com.ninjaconfig.app.data

/**
 * A single VPN/proxy config entry added by the admin.
 * Stored as a document inside the "configs" collection in Firestore.
 */
data class VpnConfig(
    val id: String = "",
    val countryCode: String = "",   // ISO 3166-1 alpha-2, e.g. "DE", "US" — used to render the flag emoji
    val countryName: String = "",   // Display name, e.g. "Germany"
    val protocol: String = "",      // "vmess" | "vless" | "shadowsocks" | "trojan"
    val configLink: String = "",    // Full share link, e.g. vmess://..., ss://..., vless://...
    val label: String = "",         // Optional short label, e.g. "Server 1"
    val isPremium: Boolean = false, // Show crown badge
    val supportsHD: Boolean = false,
    val supportsGaming: Boolean = false,
    val speedMbps: Int = 0,         // 0 = unknown, else shown as a badge
    val sortOrder: Long = 0,        // For manual ordering within a country
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Configs grouped by country for the home list — mirrors the section
 * grouping seen in the Ninja VPN screenshot (featured country on top,
 * then an alphabetical/grouped list below).
 */
data class CountryGroup(
    val countryCode: String,
    val countryName: String,
    val configs: List<VpnConfig>
)

/** Converts an ISO country code like "DE" into its flag emoji, e.g. 🇩🇪 */
fun countryCodeToFlagEmoji(countryCode: String): String {
    if (countryCode.length != 2) return "🏳️"
    val base = 0x1F1E6
    val first = Character.codePointAt(countryCode.uppercase(), 0) - 'A'.code + base
    val second = Character.codePointAt(countryCode.uppercase(), 1) - 'A'.code + base
    return String(Character.toChars(first)) + String(Character.toChars(second))
}
