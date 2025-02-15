package studio.codescape.metronome.player.application.usecase

import androidx.media3.common.Player
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.player.domain.usecase.PlaySound

@Inject
class PlaySoundImpl(
    private val player: Player
) : PlaySound {
    override fun invoke() {
        player.seekToDefaultPosition()
        player.play()
    }
}