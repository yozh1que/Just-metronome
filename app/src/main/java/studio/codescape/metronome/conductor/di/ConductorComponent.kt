package studio.codescape.metronome.conductor.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import studio.codescape.metronome.common.qualifiers.Singleton
import studio.codescape.metronome.conductor.application.repository.SettingsRepository
import studio.codescape.metronome.conductor.application.repository.SettingsRepositoryDataStoreImpl
import studio.codescape.metronome.conductor.application.usecase.settings.SetBeatsPerMinuteSettingsImpl
import studio.codescape.metronome.conductor.domain.model.Conductor
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import studio.codescape.metronome.conductor.domain.usecase.settings.SetBeatsPerMinuteSettings
import kotlin.coroutines.CoroutineContext

@Singleton
@Component
abstract class ConductorComponent(
    @get:Provides val context: Context,
    @get:Provides val parentCoroutineContext: CoroutineContext,
) : ConductorSettings.UseCaseComponent, ConductorSettings.RepositoryComponent {
    abstract val conductor: Conductor
}

private class ConductorSettings {

    interface UseCaseComponent {

        val SetBeatsPerMinuteSettingsImpl.bind: SetBeatsPerMinuteSettings
            @Provides get() = this
    }

    interface RepositoryComponent {

        val SettingsRepositoryDataStoreImpl.bind: SettingsRepository
            @Provides get() = this

        @Singleton
        @Provides
        fun dataStore(
            context: Context,
            parentCoroutineContext: CoroutineContext,
        ): DataStore<androidx.datastore.preferences.core.Preferences> =
            PreferenceDataStoreFactory.create(
                scope = CoroutineScope(parentCoroutineContext + Dispatchers.IO + SupervisorJob())
            ) {
                context.preferencesDataStoreFile(DATA_STORE_FILE_NAME)
            }

        companion object {
            private const val DATA_STORE_FILE_NAME = "conductor_settings"
        }
    }
}


