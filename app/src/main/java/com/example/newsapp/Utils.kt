package com.example.newsapp

import java.text.SimpleDateFormat
import java.util.Locale

object Utils {

    fun formatDateTime(isoFormatted: String): String {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH)
        val formatter = SimpleDateFormat("yyyy-MMMM-dd HH:mm", Locale.ENGLISH)
        return try {
            val parsed = parser.parse(isoFormatted)!!
            formatter.format(parsed)
        } catch (e: Exception) {
            isoFormatted
        }
    }
}
