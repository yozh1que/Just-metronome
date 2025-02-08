package studio.codescape.metronome.conductor.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import studio.codescape.metronome.conductor.application.repository.SettingsRepository
import studio.codescape.metronome.conductor.application.repository.SettingsRepositoryDataStoreImpl
import studio.codescape.metronome.conductor.domain.model.Conductor
import kotlin.coroutines.CoroutineContext

typealias StorageFileName = String
typealias IoDispatcher = CoroutineDispatcher

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ConductorScope

@ConductorScope
@Component
abstract class ConductorComponent(
    @get:Provides val context: Context,
    @get:Provides val ioDispatcher: IoDispatcher = Dispatchers.IO,
    @get:Provides val parentCoroutineContext: CoroutineContext,
    @get:Provides val storageFileName: StorageFileName = DEFAULT_DATA_STORE_FILE_NAME
) : ConductorSettings.RepositoryComponent {

    abstract val conductor: Conductor

    private companion object {
        private const val DEFAULT_DATA_STORE_FILE_NAME = "conductor settings"
    }
}

private class ConductorSettings {
    
    interface RepositoryComponent {

        val SettingsRepositoryDataStoreImpl.bind: SettingsRepository
            @Provides get() = this

        @ConductorScope
        @Provides
        fun dataStore(
            context: Context,
            ioDispatcher: IoDispatcher,
            parentCoroutineContext: CoroutineContext,
            storageFileName: StorageFileName
        ): DataStore<androidx.datastore.preferences.core.Preferences> =
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(parentCoroutineContext + Job() + ioDispatcher)
            ) {
                context.preferencesDataStoreFile(storageFileName)
            }

    }
}


