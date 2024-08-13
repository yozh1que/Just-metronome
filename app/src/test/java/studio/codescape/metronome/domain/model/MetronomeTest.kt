@file:OptIn(ExperimentalCoroutinesApi::class)

package studio.codescape.metronome.domain.model

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import studio.codescape.metronome.domain.model.settings.Settings
import studio.codescape.metronome.domain.usecase.GetMetronomeSettings
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext

class MetronomeTest : StateHolderTest<Metronome>() {

    @Mock
    private lateinit var mockGetMetronomeSettings: GetMetronomeSettings

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): Metronome = Metronome(
        getMetronomeSettings = mockGetMetronomeSettings,
        parentCoroutineContext = parentCoroutineContext
    )

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        whenever(mockGetMetronomeSettings.invoke()).thenReturn(flowOf(stubSettings))
    }

    @Test
    fun `initially idle`() = runStateHolderTest { metronome ->
        metronome.state.observe {
            advanceUntilIdle()
            expectValues(State.Idle)
        }
    }

    @Test
    fun `toggles metronome state between idle and running states`() =
        runStateHolderTest { metronome ->
            metronome.state.observe {
                advanceUntilIdle()
                metronome.handleCommand(Command.Toggle)
                advanceUntilIdle()
                expectValues(
                    State.Idle,
                    State.Running
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

                metronome.handleCommand(Command.Toggle)

                repeat(5) { iter ->
                    advanceTimeFor1Beat()
                    expectValues(*(0..iter).map { Effect.Beat }.toTypedArray())
                }
                verify(mockGetMetronomeSettings).invoke()
            }
        }

    private companion object {
        private val stubSettings = Settings(
            beatsPerMinute = 60
        )
    }

}