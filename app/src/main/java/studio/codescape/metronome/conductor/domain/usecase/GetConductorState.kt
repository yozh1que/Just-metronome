package studio.codescape.metronome.conductor.domain.usecase

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.conductor.domain.model.Conductor
import studio.codescape.metronome.conductor.domain.model.State

class GetConductorState(
    private val conductor: Conductor
) {
    operator fun invoke(): Flow<State> = conductor.state
}