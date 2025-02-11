package studio.codescape.metronome.domain.model

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.codescape.metronome.common.di.CoroutineDispatchers
import studio.codescape.metronome.di.AppComponent
import studio.codescape.metronome.di.AppScope
import studio.codescape.metronome.di.SessionComponent
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

@AppScope
class JustMetronomeApplication(
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext,
    private val coroutineDispatchers: CoroutineDispatchers,
    createAppComponent: (coroutineDispatchers: CoroutineDispatchers) -> AppComponent,
    private val createSessionComponent: (appComponent: AppComponent, parentCoroutineContext: CoroutineContext) -> SessionComponent,
) : CoroutineScope {

    sealed interface Command {
        data object Load : Command
        data object StartSession : Command
        data object RestartSession : Command
    }

    sealed interface State {

        data object Loading : State

        sealed interface Ready : State {
            val appComponent: AppComponent

            data class AppReady(
                override val appComponent: AppComponent
            ) : Ready

            data class SessionReady(
                override val appComponent: AppComponent,
                val sessionComponent: SessionComponent,
                val index: Int = 0
            ) : Ready {
                fun cancel() {
                    sessionComponent.sessionCoroutineScope.cancel()
                }
            }
        }
    }

    private val commands = Channel<Command>()

    override val coroutineContext: CoroutineContext =
        parentCoroutineContext + SupervisorJob() + CoroutineExceptionHandler { context, throwable ->
            handleCommand(Command.RestartSession)
        }

    val state = commands
        .receiveAsFlow()
        .onStart { emit(Command.Load) }
        .scan<Command, State>(State.Loading) { state, command ->
            when (command) {
                Command.Load -> State.Ready.AppReady(
                    createAppComponent(coroutineDispatchers)
                )
                Command.StartSession -> if (state is State.Ready.AppReady) {
                    state.toSessionReady()
                } else {
                    state
                }

                Command.RestartSession -> if (state is State.Ready.SessionReady) {
                    state.cancel()
                    state.toSessionReady(state.index + 1)
                } else {
                    state
                }
            }
        }
        .flowOn(coroutineDispatchers.computation)
        .stateIn(
            this,
            SharingStarted.Lazily,
            State.Loading
        )

    private fun State.Ready.toSessionReady(index: Int = 0) =  State.Ready.SessionReady(
        index = index,
        appComponent = appComponent,
        sessionComponent = createSessionComponent(
            appComponent,
            this@JustMetronomeApplication.coroutineContext
        )
    )

    fun handleCommand(command: Command) {
        launch { commands.send(command) }
    }

}