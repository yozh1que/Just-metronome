package studio.codescape.metronome.player.domain.usecase

import studio.codescape.metronome.player.domain.model.Player

interface CreatePlayer {
    suspend fun invoke(): Player?
}