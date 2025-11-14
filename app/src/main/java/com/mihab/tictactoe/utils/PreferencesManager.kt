package com.mihab.tictactoe.utils

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "tictactoe_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val KEY_SOUND = booleanPreferencesKey("sound_on")
        private val KEY_X_SCORE = intPreferencesKey("x_score")
        private val KEY_O_SCORE = intPreferencesKey("o_score")
    }

    // --- SOUND ---
    val soundEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[KEY_SOUND] ?: true
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_SOUND] = enabled
        }
    }

    // --- SCORES ---
    val xScore: Flow<Int> = context.dataStore.data.map { it[KEY_X_SCORE] ?: 0 }
    val oScore: Flow<Int> = context.dataStore.data.map { it[KEY_O_SCORE] ?: 0 }

    suspend fun updateScore(x: Int, o: Int) {
        context.dataStore.edit { prefs ->
            prefs[KEY_X_SCORE] = x
            prefs[KEY_O_SCORE] = o
        }
    }

    suspend fun resetScores() {
        context.dataStore.edit { prefs ->
            prefs[KEY_X_SCORE] = 0
            prefs[KEY_O_SCORE] = 0
        }
    }
}
