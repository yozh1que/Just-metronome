package studio.codescape.metronome.conductor.application.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.common.qualifiers.Singleton
import studio.codescape.metronome.conductor.domain.model.settings.Settings
import timber.log.Timber

@Inject
@Singleton
class SettingsRepositoryDataStoreImpl(
    private val dataStore: DataStore<Preferences>
) : SettingsRepository {

    override val settings: Flow<Settings?> = dataStore
        .data
        .map { preferences ->
            preferences[CONDUCTOR_SETTINGS_PREFERENCE_KEY]
                ?.let { serializedSettings ->
                    runCatchingSerializationExceptions {
                        Json.decodeFromString<Settings>(serializedSettings)
                    }
                }
        }

    override suspend fun set(settings: Settings) {
        dataStore.edit { preferences ->
            preferences[CONDUCTOR_SETTINGS_PREFERENCE_KEY] =
                runCatchingSerializationExceptions { Json.encodeToString(settings) }.orEmpty()
        }
    }

    private fun <T> runCatchingSerializationExceptions(block: () -> T): T? = try {
        block()
    } catch (e: Throwable) {
        when (e) {
            is SerializationException, is IllegalArgumentException -> {
                Timber.e(
                    e,
                    "Failed to serialize/deserialize Settings structure."
                )
                null
            }

            else -> throw e
        }
    }

    private companion object {
        private val CONDUCTOR_SETTINGS_PREFERENCE_KEY: Preferences.Key<String> =
             stringPreferencesKey("conductor settings")
    }
}