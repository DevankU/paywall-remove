package com.freedium.reader.utils

object UrlRouter {

    private const val FREEDIUM_BASE = "https://freedium-mirror.cfd/"
    private const val REMOVEPAYWALL_BASE = "https://www.removepaywall.com/search?url="

    fun routeUrl(originalUrl: String): String {
        return if (isMediumUrl(originalUrl)) {
            "$FREEDIUM_BASE$originalUrl"
        } else {
            "$REMOVEPAYWALL_BASE$originalUrl"
        }
    }

    private fun isMediumUrl(url: String): Boolean {
        return url.contains("medium.com", ignoreCase = true)
    }
}
