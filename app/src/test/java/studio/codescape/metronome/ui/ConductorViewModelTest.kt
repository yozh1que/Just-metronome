package studio.codescape.metronome.ui

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
import studio.codescape.metronome.conductor.domain.model.State
import studio.codescape.metronome.conductor.domain.model.settings.Settings
import studio.codescape.metronome.conductor.domain.usecase.GetBeat
import studio.codescape.metronome.conductor.domain.usecase.GetConductorState
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext

class ConductorViewModelTest : StateHolderTest<MetronomeViewModel>() {

    @Mock
    private lateinit var mockGetConductorState: GetConductorState

    @Mock
    private lateinit var mockGetConductorSettings: GetConductorSettings

    @Mock
    private lateinit var mockGetBeat: GetBeat

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): MetronomeViewModel =
        MetronomeViewModel(
            getConductorState = mockGetConductorState,
            getConductorSettings = mockGetConductorSettings,
            getBeat = mockGetBeat
        )

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        whenever(mockGetConductorState.invoke()).thenReturn(emptyFlow())
        whenever(mockGetConductorSettings.invoke()).thenReturn(emptyFlow())
        whenever(mockGetBeat.invoke()).thenReturn(emptyFlow())
    }

    @Test
    fun `collects metronome state after being created`() = runStateHolderTest { viewModel ->
        whenever(mockGetConductorState.invoke()).thenReturn(flowOf(State.Paused))
        whenever(mockGetConductorSettings.invoke()).thenReturn(flowOf(stubMetronomeSettings))

        viewModel.state.observe {
            advanceUntilIdle()

            expectValues(
                UiState(
                    mainIcon = UiState.MainIcon.Drawable(R.drawable.ic_play_circle_outline_24),
                    beatsPerMinuteLabel = "$stubBeatsPerMinute"
                )
            )
        }
    }

    @Test
    fun `converts state collection errors turning state to null`() =
        runStateHolderTest { viewModel ->
            whenever(mockGetConductorSettings.invoke()).thenReturn(flow { throw RuntimeException() })

            viewModel.state.observe {
                advanceUntilIdle()

                expectValues(null)
            }
        }

    @Test
    fun `retries state collection on request`() = runStateHolderTest { viewModel ->
        whenever(mockGetConductorSettings.invoke()).thenReturn(flow { throw RuntimeException() })

        viewModel.state.observe {
            advanceUntilIdle()
            viewModel.handleCommand(Command.Retry)
            advanceUntilIdle()

            verify(mockGetConductorSettings, times(2)).invoke()
        }
    }

    @Test
    fun `relays metronome beats after being created`() = runStateHolderTest { viewModel ->
        val beatsChannel = Channel<Unit>()
        whenever(mockGetBeat.invoke()).thenReturn(beatsChannel.consumeAsFlow())

        viewModel.effects.observe {
            repeat(3) {
                launch { beatsChannel.send(Unit) }
            }

            advanceUntilIdle()
            expectValues(
                Effect.ShowBeat,
                Effect.ShowBeat,
                Effect.ShowBeat
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