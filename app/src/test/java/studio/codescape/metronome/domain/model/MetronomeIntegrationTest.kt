package studio.codescape.metronome.domain.model

import android.os.Build
import androidx.media3.test.utils.TestExoPlayerBuilder
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import studio.codescape.metronome.di.AppComponent
import studio.codescape.metronome.di.SessionComponent
import studio.codescape.metronome.di.create
import studio.codescape.metronome.player.di.PlayerComponent
import studio.codescape.metronome.player.di.create
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.coroutineDispatchers
import studio.codescape.metronome.test.observer.observe
import studio.codescape.metronome.test.observer.observer
import kotlin.coroutines.CoroutineContext

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.Q])
class MetronomeIntegrationTest : StateHolderTest<Metronome>() {

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): Metronome {
        val appComponent = AppComponent::class.create(
            applicationContext = RuntimeEnvironment.getApplication(),
            coroutineDispatchers = parentCoroutineContext.coroutineDispatchers()
        )
        val sessionComponent = SessionComponent::class.create(
            appComponent = appComponent,
            parentCoroutineContext = parentCoroutineContext,
            getPlayerComponent = { context, sessionCoroutineScope ->
                PlayerComponent::class.create(
                    context = context,
                    parentCoroutineContext = sessionCoroutineScope.coroutineContext,
                    ioDispatcher = appComponent.coroutineDispatchers.io,
                    storageFileName = PlayerComponent.DEFAULT_DATA_STORE_FILE_NAME,
                    getMediaPlayer = { context -> TestExoPlayerBuilder(context).build() }
                )
            }
        )
        return sessionComponent.metronome
    }

    @Test
    fun `launching metronome produces 1 beat immediately followed by 4 beats per each second`() = runStateHolderTest { metronome ->
        val beatEffects = metronome.beats.observer()
        metronome.state.observe {
            advanceUntilIdle()
            metronome.handleCommand(Metronome.Command.TogglePlayback)
            advanceTimeBy(4000L)
            metronome.handleCommand(Metronome.Command.TogglePlayback)
            advanceUntilIdle()
            expectValues(
                Metronome.State.Loading,
                Metronome.State.Ready.Paused(
                    settings = Settings(
                        conductorSettings = GetConductorSettings.DEFAULT_SETTINGS
                    )
                ),
                Metronome.State.Ready.Resumed(
                    settings = Settings(
                        conductorSettings = GetConductorSettings.DEFAULT_SETTINGS
                    )
                ),
                Metronome.State.Ready.Paused(
                    settings = Settings(
                        conductorSettings = GetConductorSettings.DEFAULT_SETTINGS
                    )
                )
            )

            beatEffects.expectValues(*(0..4).map { }.toTypedArray())
            beatEffects.clear()
        }
    }
}