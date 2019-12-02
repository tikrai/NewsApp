package com.example.newsapp

import com.example.newsapp.Utils.Companion.formatDateTime
import org.junit.Assert.assertEquals
import org.junit.Test

class UtilsTest {
    @Test
    fun shouldReFormatDateTimeCorrectly() {
        assertEquals("2019-November-18 14:50", formatDateTime("2019-11-18T14:50:00Z"))
    }

    @Test
    fun shouldReturnOriginalStringIfInputIsNotIso8601formatted() {
        assertEquals("2019-11-18T14:50:00", formatDateTime("2019-11-18T14:50:00"))
    }

    @Test
    fun shouldReturnOriginalStringIfInputIsText() {
        assertEquals("today", formatDateTime("today"))
    }

    @Test
    fun shouldReturnOriginalStringIfInputIsNumber() {
        assertEquals("1", formatDateTime("1"))
    }

    @Test
    fun shouldReturnOriginalStringIfInputIsSpace() {
        assertEquals(" ", formatDateTime(" "))
    }

    @Test
    fun shouldReturnOriginalStringIfInputIsEmptyString() {
        assertEquals("", formatDateTime(""))
    }
}
