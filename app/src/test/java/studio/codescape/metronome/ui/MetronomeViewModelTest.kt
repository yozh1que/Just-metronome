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
import org.mockito.kotlin.whenever
import studio.codescape.metronome.R
import studio.codescape.metronome.domain.model.ConductorSettings
import studio.codescape.metronome.domain.model.Metronome
import studio.codescape.metronome.domain.model.Settings
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext

class MetronomeViewModelTest : StateHolderTest<MetronomeViewModel>() {

    @Mock
    private lateinit var mockMetronome: Metronome

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): MetronomeViewModel =
        MetronomeViewModel(mockMetronome)

    @Before
    override fun before() {
        super.before()
        whenever(mockMetronome.state).thenReturn(emptyFlow())
    }

    @Test
    fun `collects metronome state after being created`() = runStateHolderTest { viewModel ->
        whenever(mockMetronome.state).thenReturn(
            flowOf(
                Metronome.State.Ready.Paused(
                    stubMetronomeSettings
                )
            )
        )

        viewModel.state.observe {
            advanceUntilIdle()

            expectValues(
                MetronomeViewModel.LOADING_STATE,
                MetronomeViewModel.State(
                    mainIcon = MetronomeViewModel.State.MainIcon.Drawable(R.drawable.ic_play_circle_outline_24),
                    beatsPerMinuteLabel = "$stubBeatsPerMinute"
                )
            )
        }
    }

    @Test
    fun `converts state collection errors turning state to null`() =
        runStateHolderTest { viewModel ->
            whenever(mockMetronome.state).thenReturn(flow { throw RuntimeException() })

            viewModel.state.observe {
                advanceUntilIdle()

                expectValues(
                    MetronomeViewModel.LOADING_STATE,
                    null
                )
            }
        }

// TODO: restore
//    @Test
//    fun `retries state collection on request`() = runStateHolderTest { viewModel ->
//        whenever(mockMetronome.state).thenReturn(flow { throw RuntimeException() })
//
//        viewModel.state.observe {
//            advanceUntilIdle()
//            viewModel.handleCommand(MetronomeViewModel.Command.Retry)
//            advanceUntilIdle()
//
//            verify(mockMetronome.state, times(2)).invoke()
//        }
//    }
//
    @Test
    fun `relays metronome beats after being created`() = runStateHolderTest { viewModel ->
        val beatsChannel = Channel<Unit>()
        whenever(mockMetronome.beats).thenReturn(beatsChannel.consumeAsFlow())

        viewModel.effects.observe {
            repeat(3) {
                launch { beatsChannel.send(Unit) }
            }

            advanceUntilIdle()
            expectValues(
                MetronomeViewModel.Effect.ShowBeat,
                MetronomeViewModel.Effect.ShowBeat,
                MetronomeViewModel.Effect.ShowBeat
            )
        }
    }

    private companion object {
        private const val stubBeatsPerMinute = 60
        private val stubMetronomeSettings = Settings(
            conductorSettings = ConductorSettings(stubBeatsPerMinute),
        )
    }
}