package studio.codescape.metronome.conductor.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.conductor.di.ConductorScope
import studio.codescape.metronome.conductor.domain.model.settings.Settings
import studio.codescape.metronome.conductor.domain.repository.SettingsRepository
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import kotlin.coroutines.CoroutineContext

@Inject
@ConductorScope
class Conductor(
    private val getConductorSettings: GetConductorSettings,
    private val settingsRepository: SettingsRepository,
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

        val settings: Settings?

        data object Loading : State {
            override val settings: Settings? = null
        }

        data class Paused(
            override val settings: Settings
        ) : State

        data class Resumed(
            override val settings: Settings
        ) : State

    }

    interface Effect {
        data object Beat : Effect
    }

    override val coroutineContext: CoroutineContext =
        parentCoroutineContext + Job()

    private val commands = MutableSharedFlow<Command>()

    val state: Flow<State> = produceState()
        .stateIn(this, SharingStarted.WhileSubscribed(), INITIAL_STATE)

    val effects: Flow<Effect> = produceBeatEffects()

    init {
        produceSideEffects()
    }

    private fun produceState(): Flow<State> = combine(
        commands
            .filterIsInstance<Command.Toggle>()
            .scan<Command, Boolean>(false) { resumed, _ -> !resumed },
        getConductorSettings(),
    ) { resumed, settings ->
        if (resumed) {
            State.Resumed(settings)
        } else {
            State.Paused(settings)
        }
    }


    private fun produceBeatEffects(): Flow<Effect> = state
        .flatMapLatest { state ->
            when (state) {
                is State.Resumed -> flow {
                    val delay = ONE_MINUTE_MILLIS / state.settings.beatsPerMinute
                    while (isActive) {
                        emit(Effect.Beat)
                        delay(delay)
                    }
                }

                else -> emptyFlow()
            }
        }

    private fun produceSideEffects() {
        launch {
            commands
                .filterIsInstance<Command.SetBeatsPerMinute>()
                .mapNotNull { state.first().settings }
                .collect { settings ->
                    settingsRepository.set(settings)
                }
        }
    }

    fun handleCommand(command: Command) {
        launch {
            commands.emit(command)
        }
    }

    private companion object {
        private val INITIAL_STATE = State.Loading
        private const val ONE_MINUTE_MILLIS = 1000L * 60
    }

}