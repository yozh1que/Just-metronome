package studio.codescape.metronome.conductor.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import studio.codescape.metronome.common.di.IoDispatcher
import studio.codescape.metronome.conductor.application.repository.SettingsRepository
import studio.codescape.metronome.conductor.application.repository.SettingsRepositoryDataStoreImpl
import studio.codescape.metronome.conductor.domain.model.Conductor
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import kotlin.coroutines.CoroutineContext

typealias ConductorStorageFileName = String

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class ConductorScope

@Component
@ConductorScope
abstract class ConductorComponent(
    @get:Provides val context: Context,
    @get:Provides val ioDispatcher: IoDispatcher = Dispatchers.IO,
    @get:Provides val parentCoroutineContext: CoroutineContext,
    @get:Provides val storageFileName: ConductorStorageFileName = DEFAULT_DATA_STORE_FILE_NAME,
) : ConductorSettings.RepositoryComponent {

    abstract val conductor: Conductor
    abstract val getConductorSettings: GetConductorSettings

    private companion object {
        private const val DEFAULT_DATA_STORE_FILE_NAME = "conductor settings"
    }
}

class ConductorSettings {

    interface RepositoryComponent {

        val SettingsRepositoryDataStoreImpl.bind: SettingsRepository
            @Provides get() = this

        @Provides
        @ConductorScope
        fun dataStore(
            context: Context,
            ioDispatcher: IoDispatcher,
            parentCoroutineContext: CoroutineContext,
            storageFileName: ConductorStorageFileName
        ): DataStore<androidx.datastore.preferences.core.Preferences> =
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(parentCoroutineContext + Job() + ioDispatcher)
            ) {
                context.preferencesDataStoreFile(storageFileName)
            }

    }
}


