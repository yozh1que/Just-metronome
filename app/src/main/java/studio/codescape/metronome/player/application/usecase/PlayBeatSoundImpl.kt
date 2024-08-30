package studio.codescape.metronome.player.application.usecase

import androidx.media3.common.Player
import studio.codescape.metronome.player.domain.usecase.PlayBeatSound

class PlayBeatSoundImpl(
    private val player: Player
) : PlayBeatSound {
    override fun invoke() {
        player.seekToDefaultPosition()
        player.play()
    }
}