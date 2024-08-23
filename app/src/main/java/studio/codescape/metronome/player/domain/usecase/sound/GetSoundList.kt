package studio.codescape.metronome.player.domain.usecase.sound

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.player.domain.model.sound.Sound

interface GetSoundList {
    operator fun invoke(): Flow<List<Sound>>
}