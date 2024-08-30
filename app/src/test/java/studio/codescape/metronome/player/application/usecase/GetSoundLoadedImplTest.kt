package studio.codescape.metronome.player.application.usecase

import android.net.Uri
import android.os.Build
import androidx.media3.common.Player
import androidx.media3.test.utils.TestExoPlayerBuilder
import androidx.media3.test.utils.robolectric.ShadowMediaCodecConfig
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import studio.codescape.metronome.player.domain.model.settings.Settings
import studio.codescape.metronome.player.domain.usecase.settings.GetPlayerSettings

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class GetSoundLoadedImplTest {

    @Rule
    @JvmField
    val shadowMediaCodecConfig: ShadowMediaCodecConfig =
        ShadowMediaCodecConfig.forAllSupportedMimeTypes()

    @Test
    fun `loads player with provided sound`() = runTest {
        val player = TestExoPlayerBuilder(RuntimeEnvironment.getApplication()).build()
        val getPlayerSettings = mock<GetPlayerSettings>()
        whenever(getPlayerSettings.invoke()).thenReturn(flowOf(Settings(stubSoundUri)))

        coroutineScope {
            launch {
                GetSoundLoadedImpl(
                    player = player,
                    getPlayerSettings = getPlayerSettings
                ).invoke().filterNotNull().first()
            }
            advanceUntilIdle()
            launch {
                TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
            }
        }

        assertEquals(
            Player.STATE_READY,
            player.playbackState
        )

        assertEquals(
            Uri.parse(stubSoundUri),
            player.getMediaItemAt(0).localConfiguration?.uri
        )

    }

    private companion object {
        private const val stubSoundUri = "file:///android_asset/beats/rimshot.mp3"
    }

}