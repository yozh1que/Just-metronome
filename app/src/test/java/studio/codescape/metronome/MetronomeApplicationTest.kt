package studio.codescape.metronome

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.whenever
import studio.codescape.metronome.di.AppComponent
import studio.codescape.metronome.di.SessionComponent
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext

class MetronomeApplicationTest : StateHolderTest<MetronomeApplication>() {

    @Mock
    private lateinit var mockAppComponent: AppComponent

    @Mock
    private lateinit var mockSessionComponent: SessionComponent

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): MetronomeApplication =
        MetronomeApplication(
            parentCoroutineContext = parentCoroutineContext,
            createAppComponent = { mockAppComponent },
            createSessionComponent = { mockSessionComponent }
        )

    @Test
    fun `restarts session scope whenever it propagates exception`() = runStateHolderTest { app ->
        val sessionScope = CoroutineScope(app.coroutineContext)
        whenever(mockSessionComponent.sessionCoroutineScope).thenReturn(sessionScope)

        app.state.observe {
            advanceUntilIdle()
            app.handleCommand(MetronomeApplication.Command.Start)
            advanceUntilIdle()
            sessionScope.launch { throw RuntimeException() }
            advanceUntilIdle()

            expectValues(
                MetronomeApplication.State.Idle(mockAppComponent),
                MetronomeApplication.State.Session(
                    appComponent = mockAppComponent,
                    sessionComponent = mockSessionComponent
                ),
                MetronomeApplication.State.Session(
                    index = 1,
                    appComponent = mockAppComponent,
                    sessionComponent = mockSessionComponent
                )
            )
        }


    }
}