package studio.codescape.metronome.conductor.domain.usecase

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.conductor.domain.model.Conductor

class GetConductorState(
    private val conductor: Conductor
) {
    operator fun invoke(): Flow<Conductor.State> = conductor.state
}