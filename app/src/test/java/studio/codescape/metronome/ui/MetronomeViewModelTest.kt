@file:OptIn(ExperimentalCoroutinesApi::class)

package studio.codescape.metronome.ui

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import studio.codescape.metronome.R
import studio.codescape.metronome.domain.model.State
import studio.codescape.metronome.domain.model.settings.Settings
import studio.codescape.metronome.domain.usecase.GetMetronomeBeat
import studio.codescape.metronome.domain.usecase.GetMetronomeSettings
import studio.codescape.metronome.domain.usecase.GetMetronomeState
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext

class MetronomeViewModelTest : StateHolderTest<MetronomeViewModel>() {

    @Mock
    private lateinit var mockGetMetronomeState: GetMetronomeState

    @Mock
    private lateinit var mockGetMetronomeSettings: GetMetronomeSettings

    @Mock
    private lateinit var mockGetMetronomeBeat: GetMetronomeBeat

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): MetronomeViewModel =
        MetronomeViewModel(
            getMetronomeState = mockGetMetronomeState,
            getMetronomeSettings = mockGetMetronomeSettings,
            getMetronomeBeat = mockGetMetronomeBeat
        )

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        whenever(mockGetMetronomeState.invoke()).thenReturn(emptyFlow())
        whenever(mockGetMetronomeSettings.invoke()).thenReturn(emptyFlow())
        whenever(mockGetMetronomeBeat.invoke()).thenReturn(emptyFlow())
    }

    @Test
    fun `collects metronome state after being created`() = runStateHolderTest { viewModel ->
        whenever(mockGetMetronomeState.invoke()).thenReturn(flowOf(State.Idle))
        whenever(mockGetMetronomeSettings.invoke()).thenReturn(flowOf(stubMetronomeSettings))

        viewModel.state.observe {
            advanceUntilIdle()

            expectValues(
                UiState(
                    mainIconRes = R.drawable.ic_play_circle_outline_24,
                    beatsPerMinuteLabel = "$stubBeatsPerMinute"
                )
            )
        }
    }

    @Test
    fun `converts state collection errors turning state to null`() = runStateHolderTest { viewModel ->
        whenever(mockGetMetronomeSettings.invoke()).thenReturn(flow { throw RuntimeException() })

        viewModel.state.observe {
            advanceUntilIdle()

            expectValues(null)
        }
    }

    @Test
    fun `retries state collection on request`() = runStateHolderTest {  viewModel ->
        whenever(mockGetMetronomeSettings.invoke()).thenReturn(flow { throw RuntimeException() })

        viewModel.state.observe {
            advanceUntilIdle()
            viewModel.handleCommand(Command.Retry)
            advanceUntilIdle()

            verify(mockGetMetronomeSettings, times(2)).invoke()
        }
    }

    @Test
    fun `relays metronome beats after being created`() = runStateHolderTest { viewModel ->
        val beatsChannel = Channel<Unit>()
        whenever(mockGetMetronomeBeat.invoke()).thenReturn(beatsChannel.consumeAsFlow())

        viewModel.effects.observe {
            repeat(3) {
                launch { beatsChannel.send(Unit) }
            }

            advanceUntilIdle()
            expectValues(
                Effect.Beat,
                Effect.Beat,
                Effect.Beat
            )
        }
    }

    private companion object {
        private const val stubBeatsPerMinute = 60
        private val stubMetronomeSettings = Settings(
            beatsPerMinute = stubBeatsPerMinute
        )
    }
}