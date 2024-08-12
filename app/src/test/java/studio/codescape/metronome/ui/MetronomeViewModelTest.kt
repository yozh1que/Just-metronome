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
import studio.codescape.metronome.domain.model.Metronome
import studio.codescape.metronome.domain.usecase.GetMetronome
import studio.codescape.metronome.domain.usecase.GetMetronomeBeat
import studio.codescape.metronome.test.ViewModelTest
import studio.codescape.metronome.test.observer.observe

class MetronomeViewModelTest : ViewModelTest<MetronomeViewModel>() {

    @Mock
    private lateinit var mockGetMetronome: GetMetronome

    @Mock
    private lateinit var mockGetMetronomeBeat: GetMetronomeBeat

    override fun createViewModel(): MetronomeViewModel = MetronomeViewModel(
        getMetronome = mockGetMetronome,
        getMetronomeBeat = mockGetMetronomeBeat
    )

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        whenever(mockGetMetronome.invoke()).thenReturn(emptyFlow())
        whenever(mockGetMetronomeBeat.invoke()).thenReturn(emptyFlow())
    }

    @Test
    fun `collects metronome state on when created`() = runViewModelTest { viewModel ->
        whenever(mockGetMetronome.invoke()).thenReturn(flowOf(stubIdleMetronome))

        viewModel.state.observe {
            advanceUntilIdle()

            expectValues(
                State(
                    mainIconRes = R.drawable.ic_play_circle_outline_24,
                    beatsPerMinuteLabel = "$stubBeatsPerMinute"
                )
            )
        }
    }

    @Test
    fun `converts state collection errors turning state to null`() = runViewModelTest { viewModel ->
        whenever(mockGetMetronome.invoke()).thenReturn(flow { throw RuntimeException() })

        viewModel.state.observe {
            advanceUntilIdle()

            expectValues(null)
        }
    }

    @Test
    fun `retries state collection on request`() = runViewModelTest { viewModel ->
        whenever(mockGetMetronome.invoke()).thenReturn(flow { throw RuntimeException() })

        viewModel.state.observe {
            advanceUntilIdle()
            viewModel.handleCommand(Command.Retry)
            advanceUntilIdle()

            verify(mockGetMetronome, times(2)).invoke()
        }
    }

    @Test
    fun `collects metronome beat when created`() = runViewModelTest { viewModel ->
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
        private const val stubBeatsPerMinute = 0
        private val stubIdleMetronome = Metronome.Idle(0)
    }
}