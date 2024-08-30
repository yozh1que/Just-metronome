package studio.codescape.metronome.player.domain.usecase.settings

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.player.domain.model.settings.Settings

class SettingsInteractor(
    private val getPlayerSettings: GetPlayerSettings,
    private val setSoundUriSettings: SetSoundUriSettings
) {
    val settings: Flow<Settings>
        get() = getPlayerSettings()

    suspend fun setSoundUri(uri: String) = setSoundUriSettings(uri)
}