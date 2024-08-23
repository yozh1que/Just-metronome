package studio.codescape.metronome.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.codescape.metronome.R
import studio.codescape.metronome.domain.model.State
import studio.codescape.metronome.domain.model.settings.Settings
import studio.codescape.metronome.domain.usecase.GetMetronomeBeat
import studio.codescape.metronome.domain.usecase.GetMetronomeState
import studio.codescape.metronome.domain.usecase.settings.GetMetronomeSettings
import timber.log.Timber

class MetronomeViewModel(
    private val getMetronomeState: GetMetronomeState,
    private val getMetronomeSettings: GetMetronomeSettings,
    private val getMetronomeBeat: GetMetronomeBeat,
) : ViewModel() {

    private val _commands = Channel<Command>()

    val state: Flow<UiState?> = produceState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = loadingUiState
        )
        .filter { it != loadingUiState }

    val effects: Flow<Effect> = produceEffects()
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed()
        )

    private fun produceState() = getInitIntents()
        .flatMapLatest {
            combine<State, Settings, UiState?>(
                getMetronomeState(),
                getMetronomeSettings()
            ) { state, settings ->
                UiState(
                    mainIcon = when (state) {
                        is State.Paused -> UiState.MainIcon.Drawable(R.drawable.ic_play_circle_outline_24)
                        is State.Resumed -> UiState.MainIcon.Drawable(R.drawable.ic_pause_circle_outline_24)
//                        State.Loading -> UiState.MainIcon.IndeterminateProgress
                    },
                    beatsPerMinuteLabel = settings.beatsPerMinuteLabel
                )

            }
                .catch { e ->
                    Timber.e(e, "Metronome state collection failed.")
                    emit(null)
                }
        }


    private fun produceEffects() =
        getInitIntents().flatMapLatest {
            getMetronomeBeat()
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

    private companion object {
        private val loadingUiState = UiState(mainIcon = UiState.MainIcon.IndeterminateProgress, "")
    }
}


