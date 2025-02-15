package studio.codescape.metronome.common.application.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import timber.log.Timber
import kotlin.reflect.KType

class PreferenceItemRepository<T>(
    private val dataStore: DataStore<Preferences>,
    key: String,
    type: KType
) {

    private val serializer = serializer(type)
    private val preferenceKey = stringPreferencesKey(key)

    val values: Flow<T?> = dataStore
        .data
        .map { preferences ->
            preferences[preferenceKey]
                ?.let { serializedValue ->
                    runCatchingSerializationExceptions {
                        Json.decodeFromString(serializer, serializedValue) as T
                    }
                }
        }

    suspend fun set(value: T) {
        dataStore.edit { preferences ->
            preferences[preferenceKey] =
                runCatchingSerializationExceptions { Json.encodeToString(serializer, value) }.orEmpty()
        }
    }

    private fun <T> runCatchingSerializationExceptions(block: () -> T): T? = try {
        block()
    } catch (e: Throwable) {
        when (e) {
            is SerializationException, is IllegalArgumentException -> {
                Timber.e(
                    e,
                    "Failed to serialize/deserialize structure."
                )
                null
            }

            else -> throw e
        }
    }
}