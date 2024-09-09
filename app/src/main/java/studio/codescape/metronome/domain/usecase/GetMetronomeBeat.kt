package studio.codescape.metronome.domain.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import studio.codescape.metronome.domain.model.Effect
import studio.codescape.metronome.domain.model.Metronome

class GetMetronomeBeat(
    val metronome: Metronome
) {
    operator fun invoke(): Flow<Unit> = metronome
        .effects
        .filter { effect -> effect == Effect.Beat }
        .map { }
}