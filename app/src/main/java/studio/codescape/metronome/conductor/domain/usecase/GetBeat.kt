package studio.codescape.metronome.conductor.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import studio.codescape.metronome.conductor.domain.model.Effect
import studio.codescape.metronome.conductor.domain.model.Conductor

class GetBeat(
    val conductor: Conductor
) {
    operator fun invoke(): Flow<Unit> = conductor
        .effects
        .filter { effect -> effect == Effect.Beat }
        .map { }
}