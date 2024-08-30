package studio.codescape.metronome.player.application.repository
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import studio.codescape.metronome.player.domain.model.settings.Settings
import timber.log.Timber

class PlayerSettingsRepositoryDataStoreImpl(
    private val dataStore: DataStore<Preferences>
) : PlayerSettingsRepository {

    private val settingsPreferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(settingsKey)

    override val settings: Flow<Settings?> = dataStore
        .data
        .map { preferences ->
            preferences[settingsPreferenceKey]
                ?.let { serializedSettings ->
                    runCatchingSerialization {
                        Json.decodeFromString<Settings>(serializedSettings)
                    }
                }
        }

    override suspend fun set(settings: Settings) {
        dataStore.edit { preferences ->
            preferences[settingsPreferenceKey] =
                runCatchingSerialization { Json.encodeToString(settings) }.orEmpty()
        }
    }

    private fun <T> runCatchingSerialization(block: () -> T): T? = try {
        block()
    } catch (e: RuntimeException) {
        Timber.e(
            e,
            "Failed to serialize/deserialize Settings structure.".takeIf {
                e is SerializationException || e is IllegalArgumentException
            }
        )
        null
    }

    private companion object {
        private const val settingsKey = "player settings"
    }
}