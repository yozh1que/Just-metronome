@file:OptIn(ExperimentalCoroutinesApi::class)

package studio.codescape.metronome.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import studio.codescape.metronome.domain.usecase.GetMetronomeSettings
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class Metronome(
    private val getMetronomeSettings: GetMetronomeSettings,
    parentCoroutineContext: CoroutineContext = EmptyCoroutineContext
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = parentCoroutineContext + Job()

    val state: Flow<State>
    val effects: Flow<Effect>

    private val commands: Channel<Command> = Channel()

    init {
        state = produceState()
            .stateIn(this, SharingStarted.Lazily, null)
            .filterNotNull()
        effects = produceEffects()
    }

    private fun produceState(): Flow<State> = commands
        .receiveAsFlow()
        .filter { command -> command == Command.Toggle }
        .scan(false) { startMetronome, _ -> !startMetronome }
        .map { startMetronome ->
            State.Running.takeIf { startMetronome } ?: State.Idle
        }

    private fun produceEffects(): Flow<Effect> = state
        .filterIsInstance<State.Running>()
        .flatMapConcat { state ->
            getMetronomeSettings()
                .flatMapConcat { settings ->
                    flow {
                        while (isActive) {
                            emit(Effect.Beat)
                            delay(oneMinuteMillis / settings.beatsPerMinute)
                        }
                    }
                }
        }


    fun handleCommand(command: Command) {
        launch {
            commands.send(command)
        }
    }

    private companion object {
        private const val oneMinuteMillis = 1000L * 60
    }

}