package studio.codescape.metronome.conductor.domain.model

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import studio.codescape.metronome.conductor.domain.model.settings.Settings
import studio.codescape.metronome.conductor.domain.usecase.settings.SettingsInteractor
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext

class ConductorTest : StateHolderTest<Conductor>() {


    @Mock
    private lateinit var mockSettingsInteractor: SettingsInteractor

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): Conductor = Conductor(
        settingsInteractor = mockSettingsInteractor,
        parentCoroutineContext = parentCoroutineContext
    )

    @Before
    override fun before() {
        super.before()
        whenever(mockSettingsInteractor.settings).thenReturn(flowOf(stubSettings))
    }

    @Test
    fun `initially idle`() = runStateHolderTest { metronome ->
        metronome.state.observe {
            advanceUntilIdle()
            expectValues(Conductor.State.Paused)
        }
    }

    @Test
    fun `toggles metronome state between idle and running states`() =
        runStateHolderTest { metronome ->
            metronome.state.observe {
                advanceUntilIdle()
                metronome.handleCommand(Conductor.Command.Toggle)
                advanceUntilIdle()
                expectValues(
                    Conductor.State.Paused,
                    Conductor.State.Resumed
                )
            }
        }

    @Test
    fun `publishes metronome beats when running state is active`() =
        runStateHolderTest { metronome ->
            val oneMinuteMillis = 1000L * 60
            fun advanceTimeFor1Beat() = advanceTimeBy(oneMinuteMillis / stubSettings.beatsPerMinute)
            metronome.effects.observe {
                advanceUntilIdle()

                metronome.handleCommand(Conductor.Command.Toggle)

                repeat(5) { iter ->
                    advanceTimeFor1Beat()
                    expectValues(*(0..iter).map { Conductor.Effect.Beat }.toTypedArray())
                }
                verify(mockSettingsInteractor).settings
            }
        }

    private companion object {
        private val stubSettings = Settings(
            beatsPerMinute = 60
        )
    }

}