package com.example.newsapp

import java.text.SimpleDateFormat
import java.util.*

class Utils {

    companion object {
        fun formatDateTime(isoFormatted: String): String {
            val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
            val formatter = SimpleDateFormat("yyyy-MMMM-dd HH:mm", Locale.ENGLISH)
            return try {
                val parsed = parser.parse(isoFormatted) ?: return isoFormatted
                formatter.format(parsed)
            } catch (e: Exception) {
                isoFormatted
            }
        }
    }
}