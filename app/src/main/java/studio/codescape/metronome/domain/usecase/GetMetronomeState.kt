package studio.codescape.metronome.domain.usecase

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.domain.model.Metronome
import studio.codescape.metronome.domain.model.State

class GetMetronomeState(
    private val metronome: Metronome
) {
    operator fun invoke(): Flow<State> = metronome.state
}