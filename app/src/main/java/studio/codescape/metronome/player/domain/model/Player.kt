package studio.codescape.metronome.player.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.codescape.metronome.domain.model.Effect
import studio.codescape.metronome.domain.model.Metronome
import studio.codescape.metronome.player.domain.usecase.GetSoundLoaded
import studio.codescape.metronome.player.domain.usecase.PlayBeatSound
import studio.codescape.metronome.player.domain.usecase.settings.SettingsInteractor
import kotlin.coroutines.CoroutineContext

// resource loading
// observes metronome effects
class Player(
    private val metronome: Metronome,
    private val settingsInteractor: SettingsInteractor,
    getSoundLoaded: GetSoundLoaded,
    private val playBeatSound: PlayBeatSound,
    parentCoroutineContext: CoroutineContext,
) : CoroutineScope {

    private val commands = Channel<Command>()

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
        launch {
            state
                .filterIsInstance<State.Ready>()
                .flatMapLatest { state -> state.consumeBeatEffects() }
                .collect {
                    playBeatSound()
                }
        }
        launch {
            commands
                .receiveAsFlow()
                .filterIsInstance<Command.SetSound>()
                .collect { command ->
                    settingsInteractor.setSoundUri(command.uri)
                }
        }
    }

    private fun State.Ready.consumeBeatEffects() = metronome
        .effects
        .filter { effect -> effect == Effect.Beat }

    fun handleCommand(command: Command) {
        launch {
            commands.send(command)
        }
    }


}