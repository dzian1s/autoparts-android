package com.dzian1s.autopartsapp.data

import android.content.Context
import java.util.UUID

object UserPrefs {
    private const val PREFS = "autoparts_prefs"
    private const val KEY_CLIENT_ID = "client_id"

    fun getOrCreateClientId(context: Context): String {
        val sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val existing = sp.getString(KEY_CLIENT_ID, null)
        if (!existing.isNullOrBlank()) return existing

        val newId = UUID.randomUUID().toString()
        sp.edit().putString(KEY_CLIENT_ID, newId).apply()
        return newId
    }
}
