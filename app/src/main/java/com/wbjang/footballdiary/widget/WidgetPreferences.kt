package com.wbjang.footballdiary.widget

import android.content.Context
import com.google.gson.Gson

object WidgetPreferences {

    private const val PREFS_NAME = "match_widget_prefs"
    private const val KEY_MATCH = "widget_match"
    private const val KEY_TEAM_NAME = "widget_team_name"

    fun saveData(context: Context, teamName: String, match: WidgetMatch?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TEAM_NAME, teamName)
            .putString(KEY_MATCH, match?.let { Gson().toJson(it) })
            .apply()
    }

    fun getTeamName(context: Context): String =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_TEAM_NAME, "") ?: ""

    fun getMatch(context: Context): WidgetMatch? {
        val json = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_MATCH, null) ?: return null
        return try {
            Gson().fromJson(json, WidgetMatch::class.java)
        } catch (e: Exception) {
            null
        }
    }
}
