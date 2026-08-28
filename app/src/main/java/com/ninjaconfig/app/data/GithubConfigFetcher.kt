package com.ninjaconfig.app.data

import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

/**
 * Fallback config source. Firestore requires reaching Google's servers, which
 * may be unreachable without an already-working VPN connection (a classic
 * chicken-and-egg problem). GitHub's raw content endpoint is often reachable
 * without a VPN, so this file is used as a bootstrap/fallback list.
 *
 * The JSON file lives at /public/configs.json in this repository and can be
 * edited directly from the GitHub web UI - same format as the fields below.
 */
object GithubConfigFetcher {

    private const val RAW_URL =
        "https://raw.githubusercontent.com/abolfazlhedayati835-png/-ct-vpn/main/public/configs.json"

    fun fetchConfigs(): List<VpnConfig> {
        return try {
            val connection = URL(RAW_URL).openConnection() as HttpURLConnection
            connection.connectTimeout = 6000
            connection.readTimeout = 6000
            connection.requestMethod = "GET"

            val text = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val array = JSONArray(text)
            (0 until array.length()).mapNotNull { i ->
                val obj = array.optJSONObject(i) ?: return@mapNotNull null
                VpnConfig(
                    id = "github-$i",
                    countryCode = obj.optString("countryCode"),
                    countryName = obj.optString("countryName"),
                    protocol = obj.optString("protocol"),
                    configLink = obj.optString("configLink"),
                    label = obj.optString("label"),
                    isPremium = obj.optBoolean("isPremium", false)
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
