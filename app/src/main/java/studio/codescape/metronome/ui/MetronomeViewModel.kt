package studio.codescape.metronome.ui

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.codescape.metronome.R
import studio.codescape.metronome.conductor.domain.model.settings.Settings
import studio.codescape.metronome.domain.model.Metronome
import timber.log.Timber

class MetronomeViewModel(
    private val metronome: Metronome
) : ViewModel() {

    sealed interface Command {
        data object Retry : Command
    }

    data class State(
        val mainIcon: MainIcon,
        val beatsPerMinuteLabel: String
    ) {
        sealed interface MainIcon {
            data object IndeterminateProgress : MainIcon

            @JvmInline
            value class Drawable(
                @DrawableRes
                val iconRes: Int
            ) : MainIcon
        }
    }

    interface Effect {
        object ShowBeat : Effect
    }

    private val _commands = Channel<Command>()

    val state: Flow<State?> = produceState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = LOADING_STATE
        )

    val effects: Flow<Effect> = produceEffects()

    private fun produceState() = getInitIntents()
        .flatMapLatest {
            metronome.state.map<Metronome.State, State?> { metronomeState ->
//                when (metronomeState) {
//                    Metronome.State.Loading -> State.MainIcon.IndeterminateProgress
//                    is Metronome.State.Ready.Paused -> TODO()
//                    is Metronome.State.Ready.Resumed -> TODO()
//                }
                State(
                    mainIcon = when (metronomeState) {
                        is Metronome.State.Ready.Paused -> State.MainIcon.Drawable(R.drawable.ic_play_circle_outline_24)
                        is Metronome.State.Ready.Resumed -> State.MainIcon.Drawable(R.drawable.ic_pause_circle_outline_24)
                        else -> State.MainIcon.IndeterminateProgress
                    },
                    beatsPerMinuteLabel = metronomeState.settings?.conductorSettings?.beatsPerMinuteLabel
                        ?: ""
                )

            }
                .catch { e ->
                    Timber.e(e, "Metronome state collection failed.")
                    emit(null)
                }
        }


    private fun produceEffects() =
        getInitIntents().flatMapLatest {
            metronome.beats
                .map { Effect.ShowBeat }
                .catch { e ->
                    Timber.e(e, "Metronome beat collection failed.")
                }
        }

    private fun getInitIntents() = _commands
        .receiveAsFlow()
        .mapNotNull { command -> Unit.takeIf { command == Command.Retry } }
        .onStart { emit(Unit) }

    fun handleCommand(command: Command) {
        viewModelScope.launch {
            _commands.send(command)
        }
    }

    private val Settings.beatsPerMinuteLabel: String
        get() = "$beatsPerMinute"

    internal companion object {
        internal val LOADING_STATE = State(mainIcon = State.MainIcon.IndeterminateProgress, "")
    }
}


