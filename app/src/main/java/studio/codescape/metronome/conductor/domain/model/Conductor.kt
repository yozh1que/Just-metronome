package studio.codescape.metronome.conductor.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.conductor.di.ConductorScope
import studio.codescape.metronome.conductor.domain.usecase.settings.SettingsInteractor
import kotlin.coroutines.CoroutineContext

@Inject
@ConductorScope
class Conductor(
    private val settingsInteractor: SettingsInteractor,
    parentCoroutineContext: CoroutineContext
) : CoroutineScope {

    interface Command {
        object Toggle : Command

        @JvmInline
        value class SetBeatsPerMinute(
            val beatsPerMinute: Int
        ) : Command
    }

    sealed interface State {

        data object Paused : State

        data object Resumed : State

    }

    interface Effect {
        data object Beat : Effect
    }

    override val coroutineContext: CoroutineContext = parentCoroutineContext + Job()

    val state: Flow<State>
    val effects: Flow<Effect>

    private val commands: Channel<Command> = Channel()

    init {
        state = produceState()
            .stateIn(this, SharingStarted.Eagerly, INITIAL_STATE)
        effects = produceEffects()
    }

    private fun produceState(): Flow<State> = commands
        .receiveAsFlow()
        .filter { command -> command == Command.Toggle }
        .scan<Command, State>(INITIAL_STATE) { currentState, _ ->
            State.Paused.takeIf { currentState is State.Resumed } ?: State.Resumed
        }

    private fun produceEffects(): Flow<Effect> = state
        .flatMapLatest { state ->
            when (state) {
                State.Resumed -> settingsInteractor
                    .settings
                    .flatMapLatest { settings ->
                        flow {
                            while (isActive) {
                                emit(Effect.Beat)
                                delay(ONE_MINUTE_MILLIS / settings.beatsPerMinute)
                            }
                        }
                    }

                else -> emptyFlow()
            }
        }


    fun handleCommand(command: Command) {
        launch {
            commands.send(command)
        }
    }

    private companion object {
        private val INITIAL_STATE = State.Paused
        private const val ONE_MINUTE_MILLIS = 1000L * 60
    }

}