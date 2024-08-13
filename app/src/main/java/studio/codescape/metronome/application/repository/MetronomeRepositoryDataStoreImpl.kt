package studio.codescape.metronome.application.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import studio.codescape.metronome.domain.model.Metronome
import timber.log.Timber

class MetronomeRepositoryDataStoreImpl(
    private val dataStore: DataStore<Preferences>
) : MetronomeRepository {

    private val metronomePreferenceKey: Preferences.Key<String>
        get() = stringPreferencesKey(metronomeKey)

    override val metronome: Flow<Metronome?> = dataStore
        .data
        .map { preferences -> preferences[metronomePreferenceKey] }
        .map { serializedMetronome ->
            serializedMetronome
                ?.let { metronomeString ->
                    runCatchingSerialization {
                        Json.decodeFromString<Metronome>(metronomeString)
                    }
                }
        }

    override suspend fun set(metronome: Metronome) {
        dataStore.edit { preferences ->
            preferences[metronomePreferenceKey] =
                runCatchingSerialization { Json.encodeToString(metronome) }.orEmpty()
        }
    }

    private fun <T> runCatchingSerialization(block: () -> T): T? = try {
        block()
    } catch (e: RuntimeException) {
        Timber.e(
            e,
            "Failed to serialize/deserialize Metronome structure.".takeIf {
                e is SerializationException || e is IllegalArgumentException
            }
        )
        null
    }

    private companion object {
        private const val metronomeKey = "metronome"
    }
}