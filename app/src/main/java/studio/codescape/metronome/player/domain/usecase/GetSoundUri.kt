package studio.codescape.metronome.player.domain.usecase

import kotlinx.coroutines.flow.Flow

interface GetSoundUri {
    operator fun invoke(): Flow<String?>
}