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
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import studio.codescape.metronome.di.SessionScope
import studio.codescape.metronome.player.domain.model.Player
import kotlin.coroutines.CoroutineContext

@SessionScope
@Inject
class Metronome(
    private val conductor: Conductor,
    private val player: Player,
    private val getConductorSettings: GetConductorSettings,
    parentCoroutineContext: CoroutineContext
) : CoroutineScope {

    sealed interface Command {
        data object TogglePlayback : Command
    }

    sealed interface State {

        data object Loading : State {
            override val settings: Settings? = null
        }

        val settings: Settings?

        sealed interface Ready : State {

            override val settings: Settings

            data class Resumed(
                override val settings: Settings
            ) : Ready

            data class Paused(
                override val settings: Settings
            ) : Ready

        }
    }

    override val coroutineContext: CoroutineContext = parentCoroutineContext + Job()

    val state: Flow<State> = produceState()

    private val commands = MutableSharedFlow<Command>()

    init {
        produceSideEffects()
    }

    private fun produceState() = combine(
        conductor.state,
        getConductorSettings()
    ) { conductorState, conductorSettings ->
        when (conductorState) {
            is Conductor.State.Paused -> State.Ready.Paused(
                settings = Settings(conductorSettings),
            )

            is Conductor.State.Resumed -> State.Ready.Resumed(
                settings = Settings(conductorSettings)
            )
        }
    }
        .stateIn(this, SharingStarted.WhileSubscribed(), State.Loading)

    private fun produceSideEffects() {
        launch {
            commands.filterIsInstance<Command.TogglePlayback>()
                .collect {
                    conductor.handleCommand(Conductor.Command.Toggle)
                }
        }
        launch {
            state
                .flatMapLatest { state ->
                    when (state) {
                        is State.Ready.Resumed -> conductor.effects
                        else -> emptyFlow()
                    }
                }
                .collect {
                    player.handleCommand(Player.Command.PlaySound)
                }

        }
    }

    val beats = conductor.effects
        .map { }

    fun handleCommand(command: Command) {
        launch {
            commands.emit(command)
        }
    }

}


typealias ConductorSettings = studio.codescape.metronome.conductor.domain.model.settings.Settings
typealias PlayerSettings = studio.codescape.metronome.player.domain.model.settings.Settings