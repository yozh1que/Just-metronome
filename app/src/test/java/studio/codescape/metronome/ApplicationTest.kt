package studio.codescape.metronome

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.whenever
import studio.codescape.metronome.di.AppComponent
import studio.codescape.metronome.di.SessionComponent
import studio.codescape.metronome.domain.model.Application
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext

class ApplicationTest : StateHolderTest<Application>() {

    @Mock
    private lateinit var mockAppComponent: AppComponent

    @Mock
    private lateinit var mockSessionComponent: SessionComponent

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): Application =
        Application(
            parentCoroutineContext = parentCoroutineContext,
            createAppComponent = { mockAppComponent },
            createSessionComponent = { _, _-> mockSessionComponent }
        )

    @Test
    fun `restarts session scope whenever it propagates exception`() = runStateHolderTest { app ->
        val sessionScope = CoroutineScope(app.coroutineContext)
        whenever(mockSessionComponent.sessionCoroutineScope).thenReturn(sessionScope)

        app.state.observe {
            advanceUntilIdle()
            app.handleCommand(Application.Command.Start)
            advanceUntilIdle()
            sessionScope.launch { throw RuntimeException() }
            advanceUntilIdle()

            expectValues(
                Application.State.Loading,
                Application.State.Idle(mockAppComponent),
                Application.State.Session(
                    appComponent = mockAppComponent,
                    sessionComponent = mockSessionComponent
                ),
                Application.State.Session(
                    index = 1,
                    appComponent = mockAppComponent,
                    sessionComponent = mockSessionComponent
                )
            )
        }


    }
}