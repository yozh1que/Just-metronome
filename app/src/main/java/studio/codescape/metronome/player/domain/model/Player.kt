package studio.codescape.metronome.player.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.common.di.CoroutineDispatchers
import studio.codescape.metronome.player.di.PlayerScope
import studio.codescape.metronome.player.domain.model.settings.Settings
import studio.codescape.metronome.player.domain.repository.SettingsRepository
import studio.codescape.metronome.player.domain.usecase.GetSoundLoaded
import studio.codescape.metronome.player.domain.usecase.PlaySound
import studio.codescape.metronome.player.domain.usecase.settings.GetPlayerSettings
import kotlin.coroutines.CoroutineContext

@PlayerScope
@Inject
class Player(
    private val getPlayerSettings: GetPlayerSettings,
    private val settingsRepository: SettingsRepository,
    private val getSoundLoaded: GetSoundLoaded,
    private val playSound: PlaySound,
    coroutineDispatchers: CoroutineDispatchers,
    parentCoroutineContext: CoroutineContext,
) : CoroutineScope {

    sealed interface Command {
        @JvmInline
        value class SetSound(val uri: String) : Command
        data object PlaySound : Command
    }

    sealed interface State {

        val settings: Settings?

        data object Loading : State {
            override val settings: Settings? = null
        }

        data class Ready(
            override val settings: Settings
        ) : State

        data class Failure(
            override val settings: Settings
        ) : State
    }

    // splitting command channels to avoid suspending either of type subscribers due to high load
    private val playSoundCommands = Channel<Command.PlaySound>()
    private val setSoundCommands = Channel<Command.SetSound>()

    override val coroutineContext: CoroutineContext = parentCoroutineContext + coroutineDispatchers.main + Job()

    val state: Flow<State> = produceState()
        .filterNotNull()

    init {
        produceSideEffects()
    }

    private fun produceState() = getPlayerSettings()
        .flatMapLatest { settings ->
            println("settings: $settings")
            getSoundLoaded()
                .map<Unit, State> { State.Ready(settings) }
                .catch { emit(State.Failure(settings)) }
        }
        .stateIn(this, SharingStarted.Lazily, State.Loading)

    private fun produceSideEffects() {
        playSoundBeats()
        updateSoundUriSetting()
    }

    private fun updateSoundUriSetting() {
        launch {
            setSoundCommands
                .receiveAsFlow()
                .map { command -> Settings(soundUri = command.uri) }
                .collect(settingsRepository::set)
        }
    }

    private fun playSoundBeats() {
        launch { // TODO: subsribe indirectly
            state
                .filterIsInstance<State.Ready>()
                .flatMapLatest { playSoundCommands.receiveAsFlow() }
                .collect {
                    playSound()
                }
        }
    }

    fun handleCommand(command: Command) {
        launch {
            when (command) {
                is Command.PlaySound -> playSoundCommands.send(command)
                is Command.SetSound -> setSoundCommands.send(command)
            }
        }
    }

}
