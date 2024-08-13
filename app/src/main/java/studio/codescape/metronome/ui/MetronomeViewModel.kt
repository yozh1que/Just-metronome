@file:OptIn(ExperimentalCoroutinesApi::class)

package studio.codescape.metronome.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import studio.codescape.metronome.domain.usecase.GetMetronomeSettings
import studio.codescape.metronome.domain.usecase.GetMetronomeState
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
            initialValue = idleUiState
        )
        .filter { it != idleUiState }

    val effects: Flow<Effect> = produceEffects()
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed()
        )

    private fun produceState() = initMetronomeDataCollection()
        .flatMapLatest {
            combine<State, Settings, UiState?>(
                getMetronomeState(),
                getMetronomeSettings()
            ) { state, settings ->
                UiState(
                    mainIconRes = when (state) {
                        is State.Idle -> R.drawable.ic_play_circle_outline_24
                        is State.Running -> R.drawable.ic_pause_circle_outline_24
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
        initMetronomeDataCollection().flatMapLatest {
            getMetronomeBeat()
                .map { Effect.Beat }
                .catch { e ->
                    Timber.e(e, "Metronome beat collection failed.")
                }
        }

    private fun initMetronomeDataCollection() = _commands
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
        private val idleUiState = UiState(0, "")
    }
}


