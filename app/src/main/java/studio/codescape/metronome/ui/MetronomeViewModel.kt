package studio.codescape.metronome.ui

import androidx.annotation.DrawableRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.R
import studio.codescape.metronome.conductor.domain.model.settings.Settings
import studio.codescape.metronome.di.SessionScope
import studio.codescape.metronome.domain.model.Metronome

@SessionScope
@Inject
class MetronomeViewModel(
    private val metronome: Metronome
) : ViewModel() {

    sealed interface Command {
        data object TogglePlayback : Command
//        data object Retry : Command
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

    private val commands = MutableSharedFlow<Command>()

    val state = produceState()
//        .onStart { metronome.handleCommand(,) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = LOADING_STATE
        )

//    val effects: Flow<Effect> = produceEffects()

    init {
        produceSideEffects()
    }
    private fun produceState() =
        metronome.state.map { metronomeState ->
            State(
                mainIcon = when (metronomeState) {
                    is Metronome.State.Ready.Paused -> State.MainIcon.Drawable(R.drawable.ic_play_circle_outline_24)
                    is Metronome.State.Ready.Resumed -> State.MainIcon.Drawable(R.drawable.ic_pause_circle_outline_24)
                    else -> State.MainIcon.IndeterminateProgress
                },
                beatsPerMinuteLabel = metronomeState.settings?.conductorSettings?.beatsPerMinuteLabel
                    ?: ""
            )


//                .catch { e ->
//                    Timber.e(e, "Metronome state collection failed.")
//                    emit(null)
//                }
        }

    private fun produceSideEffects() {
        viewModelScope.launch {
            commands
                .filterIsInstance<Command.TogglePlayback>()
                .collect {
                    metronome.handleCommand(Metronome.Command.TogglePlayback)
                }
        }
    }

//    private fun produceEffects() =
//        getInitIntents().flatMapLatest {
//            metronome.beats
//                .map { Effect.ShowBeat }
//                .catch { e ->
//                    Timber.e(e, "Metronome beat collection failed.")
//                }
//        }

//    private fun getInitIntents() = commands
//        .filterIsInstance<Command.Retry>()
//        .map { Unit }
//        .onStart { emit(Unit) }

    fun handleCommand(command: Command) {
        viewModelScope.launch {
            commands.emit(command)
        }
    }

    private val Settings.beatsPerMinuteLabel: String
        get() = "$beatsPerMinute"

    internal companion object {
        internal val LOADING_STATE = State(mainIcon = State.MainIcon.IndeterminateProgress, "")
    }
}


