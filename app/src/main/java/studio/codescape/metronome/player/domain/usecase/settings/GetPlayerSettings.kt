package studio.codescape.metronome.player.domain.usecase.settings

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.player.domain.model.settings.Settings

interface GetPlayerSettings {
    operator fun invoke(): Flow<Settings>
}