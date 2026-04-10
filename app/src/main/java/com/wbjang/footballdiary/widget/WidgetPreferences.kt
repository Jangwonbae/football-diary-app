package com.wbjang.footballdiary.widget

import android.content.Context
import com.google.gson.Gson

object WidgetPreferences {

    private const val PREFS_NAME = "match_widget_prefs"
    private const val KEY_MATCHES = "widget_matches"
    private const val KEY_TEAM_NAME = "widget_team_name"

    fun saveData(context: Context, teamName: String, matches: List<WidgetMatch>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TEAM_NAME, teamName)
            .putString(KEY_MATCHES, Gson().toJson(matches))
            .apply()
    }

    fun getTeamName(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TEAM_NAME, "") ?: ""

    fun getMatches(context: Context): List<WidgetMatch> {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MATCHES, null) ?: return emptyList()
        return try {
            Gson().fromJson(json, Array<WidgetMatch>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
    }
}
