package studio.codescape.metronome.domain.model

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.codescape.metronome.di.AppComponent
import studio.codescape.metronome.di.SessionComponent
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Application(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    createAppComponent: () -> AppComponent,
    private val createSessionComponent: (appComponent: AppComponent, parentCoroutineContext: CoroutineContext) -> SessionComponent,
) : CoroutineScope {

    sealed interface Command {
        data object Start : Command
        data object Restart : Command
        data object Stop : Command
    }

    sealed interface State {

        val appComponent: AppComponent

        data class Idle(
            override val appComponent: AppComponent
        ) : State

        data class Session(
            override val appComponent: AppComponent,
            val sessionComponent: SessionComponent,
            val index: Int = 0
        ) : State {
            fun cancel() {
                sessionComponent.sessionCoroutineScope.cancel()
            }
        }

    }

    private val commands = Channel<Command>()

    override val coroutineContext: CoroutineContext =
        parentCoroutineContext + SupervisorJob() + CoroutineExceptionHandler { context, throwable ->
            handleCommand(Command.Restart)
        }

    val state = commands
        .receiveAsFlow()
        .scan<Command, State>(State.Idle(createAppComponent())) { state, command ->
            when (command) {
                Command.Start -> if (state is State.Idle) {
                    State.Session(
                        index = 0,
                        appComponent = state.appComponent,
                        sessionComponent = createSessionComponent(state.appComponent, this@Application.coroutineContext)
                    )
                } else {
                    state
                }

                Command.Restart -> if (state is State.Session) {
                    state.cancel()
                    State.Session(
                        appComponent = state.appComponent,
                        sessionComponent = createSessionComponent(state.appComponent, this@Application.coroutineContext),
                        index = state.index + 1
                    )
                } else {
                    state
                }

                Command.Stop -> State.Idle(state.appComponent)
            }
        }
        .stateIn(this, SharingStarted.Lazily, null)
        .filterNotNull()


    fun handleCommand(command: Command) {
        launch { commands.send(command) }
    }

}