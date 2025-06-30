package se.umu.cs.dv20fnh.gymlogger

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import java.io.IOException

val Context.dataStore by preferencesDataStore("settings")


/**
 * Class used to manage the tutorial datastore preference, in order to only show it once
 * */
class TutorialManager(private val context: Context) {

    private val tutorialComplete = booleanPreferencesKey("tutorial_complete")

    val isTutorialCompleted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->

            preferences[tutorialComplete] ?: false
        }


    suspend fun setTutorialCompleted(completed: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[tutorialComplete] = completed
        }
    }

    suspend fun clearTutorialPreferences() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }

}