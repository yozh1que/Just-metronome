package studio.codescape.metronome.player.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import studio.codescape.metronome.domain.model.Metronome
import studio.codescape.metronome.player.domain.usecase.GetSoundUri
import kotlin.coroutines.CoroutineContext

// resource loading
// start/stops metronome
// observes metronome effects
class Player(
    metronome: Metronome,
    getSoundUri: GetSoundUri,
    parentCoroutineContext: CoroutineContext,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext = parentCoroutineContext + Job()

    val state: Flow<State> = combine(
        getSoundUri(),
        metronome.state
    ) { soundUri, metronomeState ->
        if (soundUri != null) {
            State.Ready(metronomeState)
        } else {
            State.Loading
        }
    }
        .catch { emit(State.Failure) }
        .stateIn(this, SharingStarted.Lazily, State.Loading)


}