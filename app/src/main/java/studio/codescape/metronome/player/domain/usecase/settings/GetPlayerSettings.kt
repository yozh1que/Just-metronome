package studio.codescape.metronome.player.domain.usecase.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.player.domain.model.settings.Settings
import studio.codescape.metronome.player.domain.repository.SettingsRepository

@Inject
class GetPlayerSettings(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<Settings> = settingsRepository
        .settings
        .map { settings -> settings ?: DEFAULT_SETTINGS }

    internal companion object {
        internal val DEFAULT_SETTINGS = Settings(
            soundUri = "file:///android_asset/beats/rimshot.mp3"
        )
    }
}