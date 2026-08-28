package com.ninjaconfig.app.data

/**
 * Fixed baseline configs shipped inside the app itself - no admin panel, no
 * Firestore, no GitHub fetch needed. These always show up immediately, on
 * every device, regardless of network conditions. Firestore/GitHub configs
 * (if reachable) are merged on top of this list, not instead of it.
 *
 * To change these later, just edit this file and rebuild - there is no
 * remote source for this list.
 */
object BundledConfigs {

    private data class Entry(val code: String, val name: String, val link: String)

    private val entries = listOf(
        Entry("DE", "Germany", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@104.16.106.176:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%8C%90%20%7C%20ZEUS-O27CRPJ3"),
        Entry("US", "United States", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@payment.emway.ir:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%8C%90%20%7C%20ZEUS-O27CRPJ3"),
        Entry("GB", "United Kingdom", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@172.66.150.200:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%8C%90%20%7C%20ZEUS-O27CRPJ3"),
        Entry("FR", "France", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@104.27.8.119:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%8C%90%20%7C%20ZEUS-O27CRPJ3"),
        Entry("NL", "Netherlands", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@104.27.8.119:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f%2Floc-0&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%87%AB%F0%9F%87%B7%20%7C%20ZEUS-O27CRPJ3"),
        Entry("TR", "Türkiye", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@172.67.167.241:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%8C%90%20%7C%20ZEUS-O27CRPJ3"),
        Entry("CA", "Canada", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@104.25.165.136:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%8C%90%20%7C%20ZEUS-O27CRPJ3"),
        Entry("JP", "Japan", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@104.21.219.158:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%8C%90%20%7C%20ZEUS-O27CRPJ3"),
        Entry("SG", "Singapore", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@104.25.48.149:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%8C%90%20%7C%20ZEUS-O27CRPJ3"),
        Entry("AE", "United Arab Emirates", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@104.16.4.103:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%8C%90%20%7C%20ZEUS-O27CRPJ3"),
        Entry("FI", "Finland", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@104.18.234.94:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%8C%90%20%7C%20ZEUS-O27CRPJ3"),
        Entry("SE", "Sweden", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@190.93.246.107:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%8C%90%20%7C%20ZEUS-O27CRPJ3"),
        Entry("CH", "Switzerland", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@172.67.152.85:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%8C%90%20%7C%20ZEUS-O27CRPJ3"),
        Entry("IR", "Iran", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@104.21.7.233:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%8C%90%20%7C%20ZEUS-O27CRPJ3"),
        Entry("RU", "Russia", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@172.67.152.85:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f%2Floc-0&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%87%AB%F0%9F%87%B7%20%7C%20ZEUS-O27CRPJ3"),
        Entry("IN", "India", "vless://50414e45-4c5f-5a45-5553-7ac458bcad4f@104.21.219.158:443?path=%2Fstream%2FPANEL_ZEUS%2F7ac458bcad4f%2Floc-1&security=tls&fragment=200-3000%2C1-2&encryption=none&insecure=0&host=ez-a0a529.abolfazlhedayati355.workers.dev&fp=ios&type=ws&allowInsecure=0&sni=ez-a0a529.abolfazlhedayati355.workers.dev#ZEUS%20%7C%20%F0%9F%87%A6%F0%9F%87%B2%20%7C%20ZEUS-O27CRPJ3")
    )

    val list: List<VpnConfig> = entries.mapIndexed { index, e ->
        VpnConfig(
            id = "bundled-$index",
            countryCode = e.code,
            countryName = e.name,
            protocol = "vless",
            configLink = e.link,
            label = "ZEUS",
            isPremium = false,
            supportsHD = false,
            supportsGaming = false,
            speedMbps = 0,
            sortOrder = index.toLong()
        )
    }
}
