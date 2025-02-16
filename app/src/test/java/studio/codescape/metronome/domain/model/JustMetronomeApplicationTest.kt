package studio.codescape.metronome.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.whenever
import studio.codescape.metronome.di.AppComponent
import studio.codescape.metronome.di.SessionComponent
import studio.codescape.metronome.test.coroutineDispatchers
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext

class JustMetronomeApplicationTest : StateHolderTest<JustMetronomeApplication>() {

    @Mock
    private lateinit var mockAppComponent: AppComponent

    @Mock
    private lateinit var mockSessionComponent: SessionComponent

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): JustMetronomeApplication =
        JustMetronomeApplication(
            parentCoroutineContext = parentCoroutineContext,
            coroutineDispatchers = parentCoroutineContext.coroutineDispatchers(),
            createAppComponent = { mockAppComponent },
            createSessionComponent = { _, _ -> mockSessionComponent }
        )

    @Test
    fun `restarts session scope whenever it propagates exception`() = runStateHolderTest { app ->
        val sessionScope = CoroutineScope(app.coroutineContext)
        whenever(mockSessionComponent.sessionCoroutineScope).thenReturn(sessionScope)

        app.state.observe {
            advanceUntilIdle()
            app.handleCommand(JustMetronomeApplication.Command.StartSession)
            advanceUntilIdle()
            sessionScope.launch { throw RuntimeException() }
            advanceUntilIdle()

            expectValues(
                JustMetronomeApplication.State.Initializing,
                JustMetronomeApplication.State.Ready.AppReady(mockAppComponent),
                JustMetronomeApplication.State.Ready.SessionReady(
                    appComponent = mockAppComponent,
                    sessionComponent = mockSessionComponent
                ),
                JustMetronomeApplication.State.Ready.SessionReady(
                    index = 1,
                    appComponent = mockAppComponent,
                    sessionComponent = mockSessionComponent
                )
            )
        }
    }
}