package studio.codescape.metronome.player.domain.usecase

import kotlinx.coroutines.flow.Flow

interface GetSoundLoaded {
    operator fun invoke(): Flow<Unit>
}