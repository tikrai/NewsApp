package com.example.newsapp

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.anyInt
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
import org.mockito.Mockito.`when` as _when

class SearchStringTest {

    private val key = "Key"
    private val defaultSearchString = "Default"
    private val storedSearchString = "Stored"

    private val context = mock(Context::class.java)
    private val sharedPreferences = mock(SharedPreferences::class.java)
    private val resources = mock(Resources::class.java)
    private val editor = mock(SharedPreferences.Editor::class.java)

    @Before
    fun setup() {
        _when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
        _when(context.resources).thenReturn(resources)
        _when(sharedPreferences.getString(key, defaultSearchString)).thenReturn(storedSearchString)
        _when(sharedPreferences.edit()).thenReturn(editor)
        _when(resources.getString(R.string.search_string_key)).thenReturn(key)
        _when(resources.getString(R.string.search_string_default)).thenReturn(defaultSearchString)
        _when(editor.putString(anyString(), anyString())).thenReturn(editor)
    }

    @After
    fun verifyMocks() {
        verifyNoMoreInteractions(context, sharedPreferences, resources, editor)
    }

    @Test
    fun shouldLazilyWaitUntilRequestIsMade() {
        SearchString(context)
    }


    @Test
    fun shouldLoadSavedSearchString() {
        val searchString = SearchString(context)

        val actual = searchString.load()

        assertEquals(storedSearchString, actual)
        verify(context).getSharedPreferences(anyString(), anyInt())
        verify(context, times(2)).resources
        verify(sharedPreferences).getString(key, defaultSearchString)
        verify(resources, times(2)).getString(anyInt())
    }

    @Test
    fun shouldLoadSavedSearchStringTwiceButReadPreferencesOnce() {
        val searchString = SearchString(context)

        searchString.load()
        val actual = searchString.load()

        assertEquals(storedSearchString, actual)
        verify(context).getSharedPreferences(anyString(), anyInt())
        verify(context, times(2)).resources
        verify(sharedPreferences).getString(key, defaultSearchString)
        verify(resources, times(2)).getString(anyInt())
    }

    @Test
    fun shouldSaveSearchString() {
        val searchString = SearchString(context)
        val newString = "new string"

        searchString.save(newString)

        verify(context).getSharedPreferences(anyString(), anyInt())
        verify(context).resources
        verify(sharedPreferences).edit()
        verify(resources).getString(anyInt())
        verify(editor).putString(key, newString)
        verify(editor).apply()
    }
}
