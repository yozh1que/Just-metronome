package studio.codescape.metronome.player.application.usecase

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.suspendCancellableCoroutine
import studio.codescape.metronome.player.domain.usecase.GetSoundLoaded
import studio.codescape.metronome.player.domain.usecase.settings.GetPlayerSettings
import kotlin.coroutines.resume

class GetSoundLoadedImpl(
    private val player: Player,
    private val getPlayerSettings: GetPlayerSettings
) : GetSoundLoaded {
    override fun invoke(): Flow<Unit> = getPlayerSettings()
        .flatMapLatest { settings ->
            flow {
                emit(
                    suspendCancellableCoroutine { continuation ->
                        val readyListener = object : Player.Listener {

                            init {
                                player.setMediaItem(MediaItem.fromUri(Uri.parse(settings.soundUri)))
                                player.prepare()
                            }

                            override fun onPlaybackStateChanged(playbackState: Int) {
                                super.onPlaybackStateChanged(playbackState)
                                if (playbackState == Player.STATE_READY) {
                                    continuation.resume(Unit)
                                    player.removeListener(this)
                                }
                            }
                        }
                        player.addListener(readyListener)
                        continuation.invokeOnCancellation {
                            player.removeListener(readyListener)
                        }
                    }
                )
            }
        }
}