package com.wbjang.footballdiary.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.wbjang.footballdiary.ui.theme.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val followingTeamIdKey       = intPreferencesKey("following_team_id")
    private val followingTeamNameKey     = stringPreferencesKey("following_team_name")
    private val followingTeamCrestUrlKey = stringPreferencesKey("following_team_crest_url")
    private val themeModeKey             = stringPreferencesKey("theme_mode")

    val followingTeamId: Flow<Int?> = context.dataStore.data.map { prefs ->
        prefs[followingTeamIdKey]
    }

    val followingTeamName: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[followingTeamNameKey]
    }

    val followingTeamCrestUrl: Flow<String?> = context.dataStore.data.map { prefs ->
        prefs[followingTeamCrestUrlKey]
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.valueOf(prefs[themeModeKey] ?: ThemeMode.SYSTEM.name)
    }

    suspend fun saveFollowingTeam(teamId: Int, teamName: String, teamCrestUrl: String) {
        context.dataStore.edit { prefs ->
            prefs[followingTeamIdKey]       = teamId
            prefs[followingTeamNameKey]     = teamName
            prefs[followingTeamCrestUrlKey] = teamCrestUrl
        }
    }

    suspend fun saveThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }
}
