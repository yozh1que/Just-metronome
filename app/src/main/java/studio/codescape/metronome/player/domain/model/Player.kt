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
import studio.codescape.metronome.player.domain.usecase.GetSoundLoaded
import studio.codescape.metronome.player.domain.usecase.PlayBeatSound
import studio.codescape.metronome.player.domain.usecase.settings.SettingsInteractor
import kotlin.coroutines.CoroutineContext

class Player(
    private val settingsInteractor: SettingsInteractor,
    getSoundLoaded: GetSoundLoaded,
    private val playBeatSound: PlayBeatSound,
    parentCoroutineContext: CoroutineContext,
) : CoroutineScope {

    sealed interface Command {
        @JvmInline
        value class SetSound(val uri: String) : Command
        data object PlaySound : Command
    }

    sealed interface State {

        val soundUri: String

        data class Loading(
            override val soundUri: String
        ) : State

        data class Ready(
            override val soundUri: String
        ) : State

        data class Failure(
            override val soundUri: String
        ) : State
    }
    
    // splitting command channels to avoid suspending either of type subscribers due to high load
    private val playSoundCommands = Channel<Command.PlaySound>()
    private val setSoundCommands = Channel<Command.SetSound>()

    override val coroutineContext: CoroutineContext = parentCoroutineContext + Job()

    val state: Flow<State> =
        settingsInteractor
            .settings
            .flatMapLatest { settings ->
                getSoundLoaded()
                    .map<Unit, State> { State.Ready(settings.soundUri) }
                    .onStart { emit(State.Loading(settings.soundUri)) }
                    .catch { emit(State.Failure(settings.soundUri)) }
            }
            .stateIn(this, SharingStarted.Lazily, null)
            .filterNotNull()

    init {
        produceSideEffects()
    }

    private fun produceSideEffects() {
        playSoundBeats()
        updateSoundUriSetting()
    }

    private fun updateSoundUriSetting() {
        launch {
            setSoundCommands
                .receiveAsFlow()
                .collect { command ->
                    settingsInteractor.setSoundUri(command.uri)
                }
        }
    }

    private fun playSoundBeats() {
        launch {
            state
                .filterIsInstance<State.Ready>()
                .flatMapLatest { playSoundCommands.receiveAsFlow() }
                .collect {
                    playBeatSound()
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
