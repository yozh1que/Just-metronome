@file:OptIn(ExperimentalCoroutinesApi::class)

package studio.codescape.metronome.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import studio.codescape.metronome.domain.usecase.settings.SettingsInteractor
import timber.log.Timber
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Metronome(
    private val settingsInteractor: SettingsInteractor,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = parentCoroutineContext + Job()

    val state: Flow<State>
    val effects: Flow<Effect>

    private val commands: Channel<Command> = Channel()

    init {
        state = produceState()
            .stateIn(this, SharingStarted.Eagerly, initialState)
        effects = produceEffects()
    }

    private fun produceState(): Flow<State> =
        produceReadyState()

    private fun produceReadyState() = commands
        .receiveAsFlow()
        .filter { command -> command == Command.Toggle }
        .scan<Command, State>(initialState) { currentState, _ ->
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
                                Timber.d("DEBUG_BEAT, emit")
                                emit(Effect.Beat)
                                delay(oneMinuteMillis / settings.beatsPerMinute)
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
        private val initialState = State.Paused
        private const val oneMinuteMillis = 1000L * 60
    }

}