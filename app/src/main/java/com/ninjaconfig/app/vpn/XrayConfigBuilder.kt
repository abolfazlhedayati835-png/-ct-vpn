package com.ninjaconfig.app.vpn

import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.net.URLDecoder

/**
 * Builds a minimal Xray-core JSON configuration from a share link
 * (vmess://, vless://, trojan://, ss://). No routing rules by geoip/geosite
 * are used on purpose, since those require extra asset files (geoip.dat /
 * geosite.dat) we are not bundling - all traffic is simply sent through the
 * single outbound proxy.
 */
object XrayConfigBuilder {

    fun build(link: String): String {
        val outbound = when {
            link.startsWith("vmess://") -> parseVmess(link)
            link.startsWith("vless://") -> parseVless(link)
            link.startsWith("trojan://") -> parseTrojan(link)
            link.startsWith("ss://") -> parseShadowsocks(link)
            else -> throw IllegalArgumentException("Unsupported config link format")
        }

        val root = JSONObject()
        root.put("log", JSONObject().put("loglevel", "warning"))
        root.put("inbounds", JSONArray())

        val outbounds = JSONArray()
        outbounds.put(outbound)
        outbounds.put(JSONObject().put("protocol", "freedom").put("tag", "direct"))
        root.put("outbounds", outbounds)

        return root.toString()
    }

    private fun parseVmess(link: String): JSONObject {
        val b64 = link.removePrefix("vmess://")
        val decoded = String(Base64.decode(b64, Base64.DEFAULT))
        val json = JSONObject(decoded)

        val user = JSONObject().apply {
            put("id", json.optString("id"))
            put("alterId", json.optInt("aid", 0))
            put("security", json.optString("scy", "auto"))
        }
        val vnext = JSONObject().apply {
            put("address", json.optString("add"))
            put("port", json.optString("port").toIntOrNull() ?: 443)
            put("users", JSONArray().put(user))
        }
        val settings = JSONObject().put("vnext", JSONArray().put(vnext))

        val net = json.optString("net", "tcp")
        val streamSettings = JSONObject().apply {
            put("network", net)
            put("security", json.optString("tls", ""))
            if (net == "ws") {
                put(
                    "wsSettings",
                    JSONObject()
                        .put("path", json.optString("path", "/"))
                        .put("headers", JSONObject().put("Host", json.optString("host", "")))
                )
            }
        }

        return JSONObject().apply {
            put("protocol", "vmess")
            put("tag", "proxy")
            put("settings", settings)
            put("streamSettings", streamSettings)
        }
    }

    private fun parseVless(link: String): JSONObject {
        val uri = URI(link)
        val id = uri.userInfo
        val address = uri.host
        val port = if (uri.port > 0) uri.port else 443
        val params = parseQuery(uri.rawQuery)

        val user = JSONObject().apply {
            put("id", id)
            put("encryption", params["encryption"] ?: "none")
        }
        val vnext = JSONObject().apply {
            put("address", address)
            put("port", port)
            put("users", JSONArray().put(user))
        }
        val settings = JSONObject().put("vnext", JSONArray().put(vnext))

        val net = params["type"] ?: "tcp"
        val streamSettings = JSONObject().apply {
            put("network", net)
            put("security", params["security"] ?: "")
            if (net == "ws") {
                put(
                    "wsSettings",
                    JSONObject()
                        .put("path", params["path"] ?: "/")
                        .put("headers", JSONObject().put("Host", params["host"] ?: ""))
                )
            }
            if ((params["security"] ?: "") == "tls") {
                put("tlsSettings", JSONObject().put("serverName", params["sni"] ?: address))
            }
        }

        return JSONObject().apply {
            put("protocol", "vless")
            put("tag", "proxy")
            put("settings", settings)
            put("streamSettings", streamSettings)
        }
    }

    private fun parseTrojan(link: String): JSONObject {
        val uri = URI(link)
        val password = uri.userInfo
        val address = uri.host
        val port = if (uri.port > 0) uri.port else 443
        val params = parseQuery(uri.rawQuery)

        val server = JSONObject().apply {
            put("address", address)
            put("port", port)
            put("password", password)
        }
        val settings = JSONObject().put("servers", JSONArray().put(server))

        val streamSettings = JSONObject().apply {
            put("network", params["type"] ?: "tcp")
            put("security", params["security"] ?: "tls")
            put("tlsSettings", JSONObject().put("serverName", params["sni"] ?: address))
        }

        return JSONObject().apply {
            put("protocol", "trojan")
            put("tag", "proxy")
            put("settings", settings)
            put("streamSettings", streamSettings)
        }
    }

    private fun parseShadowsocks(link: String): JSONObject {
        // ss://BASE64(method:password)@host:port#name  OR  ss://BASE64(method:password@host:port)
        val body = link.removePrefix("ss://").substringBefore("#")
        val (methodPass, hostPort) = if (body.contains("@")) {
            val at = body.indexOf("@")
            val left = body.substring(0, at)
            val right = body.substring(at + 1)
            val decodedLeft = runCatching { String(Base64.decode(left, Base64.URL_SAFE or Base64.NO_PADDING)) }
                .getOrElse { left }
            decodedLeft to right
        } else {
            val decoded = String(Base64.decode(body, Base64.URL_SAFE or Base64.NO_PADDING))
            val at = decoded.indexOf("@")
            decoded.substring(0, at) to decoded.substring(at + 1)
        }

        val method = methodPass.substringBefore(":")
        val password = methodPass.substringAfter(":")
        val host = hostPort.substringBefore(":")
        val port = hostPort.substringAfter(":").substringBefore("/").toIntOrNull() ?: 8388

        val server = JSONObject().apply {
            put("address", host)
            put("port", port)
            put("method", method)
            put("password", password)
        }
        val settings = JSONObject().put("servers", JSONArray().put(server))

        return JSONObject().apply {
            put("protocol", "shadowsocks")
            put("tag", "proxy")
            put("settings", settings)
        }
    }

    private fun parseQuery(rawQuery: String?): Map<String, String> {
        if (rawQuery.isNullOrBlank()) return emptyMap()
        return rawQuery.split("&").mapNotNull {
            val idx = it.indexOf("=")
            if (idx == -1) return@mapNotNull null
            val key = it.substring(0, idx)
            val value = URLDecoder.decode(it.substring(idx + 1), "UTF-8")
            key to value
        }.toMap()
    }
}
