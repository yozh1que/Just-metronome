package studio.codescape.metronome.player.application.usecase

import studio.codescape.metronome.player.domain.model.Player
import studio.codescape.metronome.player.domain.usecase.CreatePlayer

class CreatePlayerImpl : CreatePlayer {
    override suspend fun invoke(): Player? = TODO()

}