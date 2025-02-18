package studio.codescape.metronome.domain.model

import android.os.Build
import androidx.media3.common.Player
import androidx.media3.test.utils.TestExoPlayerBuilder
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import studio.codescape.metronome.di.AppComponent
import studio.codescape.metronome.di.SessionComponent
import studio.codescape.metronome.di.create
import studio.codescape.metronome.domain.model.settings.Settings
import studio.codescape.metronome.player.di.PlayerComponent
import studio.codescape.metronome.player.di.create
import studio.codescape.metronome.player.domain.usecase.settings.GetPlayerSettings
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.coroutineDispatchers
import studio.codescape.metronome.test.observer.observe
import studio.codescape.metronome.test.observer.observer
import kotlin.coroutines.CoroutineContext

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class MetronomeIntegrationTest : StateHolderTest<Metronome>() {

    private var player: Player? = null

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): Metronome {
        val appComponent = AppComponent::class.create(
            applicationContext = RuntimeEnvironment.getApplication(),
            coroutineDispatchers = parentCoroutineContext.coroutineDispatchers()
        )
        val sessionComponent = SessionComponent::class.create(
            appComponent = appComponent,
            parentCoroutineContext = parentCoroutineContext,
            getPlayerComponent = { context, coroutineDispatchers, sessionCoroutineScope ->
                PlayerComponent::class.create(
                    applicationContext = context,
                    parentCoroutineContext = sessionCoroutineScope.coroutineContext,
                    coroutineDispatchers = coroutineDispatchers,
                    storageFileName = PlayerComponent.DEFAULT_DATA_STORE_FILE_NAME,
                    getMediaPlayer = { context -> TestExoPlayerBuilder(context).build().also { exoPlayer ->
                        player = exoPlayer
                    } }
                )
            }
        )
        return sessionComponent.metronome
    }

    @After
    fun after() {
        player = null
    }

    @Test
    fun `launching metronome produces 1 beat immediately followed by 4 beats per each second`() =
        runStateHolderTest { metronome ->
            val beatEffects = metronome.beats.observer()
            metronome.state.observe {
                advanceUntilIdle()
                launch { TestPlayerRunHelper.runUntilPlaybackState(player!!, Player.STATE_READY) }
                metronome.handleCommand(Metronome.Command.TogglePlayback)
                advanceTimeBy(4200L)

                metronome.handleCommand(Metronome.Command.TogglePlayback)
                advanceUntilIdle()
                expectValues(
                    Metronome.State.Loading,
                    Metronome.State.Paused(DEFAULT_SETTINGS),
                    Metronome.State.Resumed(DEFAULT_SETTINGS),
                    Metronome.State.Paused(DEFAULT_SETTINGS)
                )

                beatEffects.expectValues(*(0..4).map { }.toTypedArray())
                beatEffects.clear()
            }
        }

    private companion object {
        private val DEFAULT_SETTINGS = Settings(
            GetConductorSettings.DEFAULT_SETTINGS,
            GetPlayerSettings.DEFAULT_SETTINGS
        )
    }
}