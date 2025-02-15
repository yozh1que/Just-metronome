package studio.codescape.metronome.conductor.domain.usecase.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.conductor.domain.repository.SettingsRepository
import studio.codescape.metronome.conductor.domain.model.settings.Settings

@Inject
class GetConductorSettings(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<Settings> = settingsRepository
        .settings
        .map { settings -> settings ?: DEFAULT_SETTINGS }

    internal companion object {
        internal val DEFAULT_SETTINGS = Settings(
            beatsPerMinute = 60
        )
    }
}
