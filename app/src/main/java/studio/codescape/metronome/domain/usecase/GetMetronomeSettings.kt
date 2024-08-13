package studio.codescape.metronome.domain.usecase

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.domain.model.settings.Settings

interface GetMetronomeSettings {
    operator fun invoke(): Flow<Settings>
}