@file:OptIn(ExperimentalCoroutinesApi::class)

package studio.codescape.metronome.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
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
import studio.codescape.metronome.domain.model.Metronome
import studio.codescape.metronome.domain.usecase.GetMetronome
import studio.codescape.metronome.domain.usecase.GetMetronomeBeat
import timber.log.Timber

class MetronomeViewModel(
    private val getMetronome: GetMetronome,
    private val getMetronomeBeat: GetMetronomeBeat
) : ViewModel() {

    private val _commands = Channel<Command>()

    val state: Flow<State?> = produceState()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = idleState
        )
        .filter { it != idleState }

    val effects: Flow<Effect> = produceEffects()
        .shareIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed()
        )

    private fun produceState() = initMetronomeDataCollection()
        .flatMapLatest {
            getMetronome()
                .map<Metronome, State?> { metronome ->
                    State(
                        mainIconRes = when (metronome) {
                            is Metronome.Idle -> R.drawable.ic_play_circle_outline_24
                            is Metronome.Running -> R.drawable.ic_pause_circle_outline_24
                        },
                        beatsPerMinuteLabel = metronome.beatsPerMinuteLabel
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

    private val Metronome.beatsPerMinuteLabel: String
        get() = "$beatsPerMinute"

    private companion object {
        private val idleState = State(0, "")
    }
}


