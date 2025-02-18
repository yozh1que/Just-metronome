//package studio.codescape.metronome.ui
//
//import kotlinx.coroutines.flow.flowOf
//import kotlinx.coroutines.test.advanceUntilIdle
//import org.junit.Test
//import org.mockito.Mock
//import org.mockito.kotlin.whenever
//import studio.codescape.metronome.R
//import studio.codescape.metronome.domain.model.Metronome
//import studio.codescape.metronome.domain.model.settings.Settings
//import studio.codescape.metronome.domain.usecase.settings.GetMetronomeSettings
//import studio.codescape.metronome.test.StateHolderTest
//import studio.codescape.metronome.test.observer.observe
//import kotlin.coroutines.CoroutineContext
//
//class MetronomeViewModelTest : StateHolderTest<MetronomeViewModel>() {
//
//    @Mock
//    private lateinit var mockMetronome: Metronome
//
//    @Mock
//    private lateinit var mockGetMetronomeSettings: GetMetronomeSettings
//
//    override fun createStateHolder(parentCoroutineContext: CoroutineContext): MetronomeViewModel =
//        MetronomeViewModel(mockMetronome, mockGetMetronomeSettings)
//
//    @Test
//    fun `collects metronome state after being created`() = runStateHolderTest(before = {
//        whenever(mockMetronome.state).thenReturn(flowOf(Metronome.State.Ready.Paused))
//        whenever(mockGetMetronomeSettings.invoke()).thenReturn(flowOf(STUB_CONDUCTOR_SETTINGS))
//    }) { viewModel ->
//        viewModel.state.observe {
//            advanceUntilIdle()
//
//            expectValues(
//                MetronomeViewModel.LOADING_STATE,
//                MetronomeViewModel.State(
//                    mainIcon = MetronomeViewModel.State.MainIcon.Drawable(R.drawable.ic_play_circle_outline_24),
//                    beatsPerMinuteLabel = "$STUB_BEATS_PER_MINUTE"
//                )
//            )
//        }
//    }
//
//// TODO: remove?
////    @Test
////    fun `converts state collection errors turning state to null`() =
////        runStateHolderTest { viewModel ->
////            whenever(mockMetronome.state).thenReturn(flow { throw RuntimeException() })
////
////            viewModel.state.observe {
////                advanceUntilIdle()
////
////                expectValues(
////                    MetronomeViewModel.LOADING_STATE,
////                    null
////                )
////            }
////        }
//
//// TODO: restore?
////    @Test
////    fun `retries state collection on request`() = runStateHolderTest { viewModel ->
////        whenever(mockMetronome.state).thenReturn(flow { throw RuntimeException() })
////
////        viewModel.state.observe {
////            advanceUntilIdle()
////            viewModel.handleCommand(MetronomeViewModel.Command.Retry)
////            advanceUntilIdle()
////
////            verify(mockMetronome.state, times(2)).invoke()
////        }
////    }
////
////    @Test
////    fun `relays metronome beats after being created`() = runStateHolderTest { viewModel ->
////        val beatsChannel = Channel<Unit>()
////        whenever(mockMetronome.beats).thenReturn(beatsChannel.consumeAsFlow())
////
////        viewModel.effects.observe {
////            repeat(3) {
////                launch { beatsChannel.send(Unit) }
////            }
////
////            advanceUntilIdle()
////            expectValues(
////                MetronomeViewModel.Effect.ShowBeat,
////                MetronomeViewModel.Effect.ShowBeat,
////                MetronomeViewModel.Effect.ShowBeat
////            )
////        }
////    }
//
//    private companion object {
//        private const val STUB_BEATS_PER_MINUTE = 60
//        private val STUB_CONDUCTOR_SETTINGS = Settings(
//            conductorSettings = ConduSTUB_BEATS_PER_MINUTE)
//    }
//}