package studio.codescape.metronome.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.conductor.domain.model.Conductor
import studio.codescape.metronome.di.SessionScope
import studio.codescape.metronome.domain.model.settings.Settings
import studio.codescape.metronome.player.domain.model.Player
import kotlin.coroutines.CoroutineContext

@SessionScope
@Inject
class Metronome(
    private val conductor: Conductor,
    private val player: Player,
    parentCoroutineContext: CoroutineContext
) : CoroutineScope {

    sealed interface Command {
        data object TogglePlayback : Command
        sealed interface UpdateSetting : Command {
            data class BeatsPerMinute(val value: Int) : UpdateSetting
        }
    }

    sealed interface State {

        val settings: Settings?

        data object Loading : State {
            override val settings: Settings? = null
        }

        data class Resumed(
            override val settings: Settings
        ) : State

        data class Paused(
            override val settings: Settings
        ) : State

    }

    override val coroutineContext: CoroutineContext = parentCoroutineContext + Job()

    val state: Flow<State> = produceState()

    private val commands = MutableSharedFlow<Command>()

    init {
        produceSideEffects()
    }

    private fun produceState() = combine(
        conductor.state,
        player.state
    ) { conductorState, playerState ->
        println("conductorState: $conductorState, playerState: $playerState")
        when {
            conductorState is Conductor.State.Resumed && playerState is Player.State.Ready ->
                State.Resumed(Settings(conductorState.settings, playerState.settings))

            conductorState is Conductor.State.Paused && playerState is Player.State.Ready -> State.Paused(
                Settings(conductorState.settings, playerState.settings)
            )

            else -> State.Loading
        }
    }.stateIn(this, SharingStarted.WhileSubscribed(), State.Loading)

    private fun produceSideEffects() {
        togglePlayback()
        launch {
            commands.filterIsInstance<Command.UpdateSetting>().collect { command ->
                when (command) {
                    is Command.UpdateSetting.BeatsPerMinute -> conductor.handleCommand(
                        Conductor.Command.SetBeatsPerMinute(command.value)
                    )
                }
            }
        }
        setupBeatPlayback()
    }

    private fun setupBeatPlayback() {
        launch {
            state.flatMapLatest { state ->
                when (state) {
                    is State.Resumed -> conductor.effects
                    else -> emptyFlow()
                }
            }.collect {
                player.handleCommand(Player.Command.PlaySound)
            }
        }
    }

    private fun togglePlayback() {
        launch {
            commands.filterIsInstance<Command.TogglePlayback>().collect {
                conductor.handleCommand(Conductor.Command.Toggle)
            }
        }
    }

    val beats = conductor.effects.map { }

    fun handleCommand(command: Command) {
        launch {
            commands.emit(command)
        }
    }

}