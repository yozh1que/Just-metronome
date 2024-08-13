package studio.codescape.metronome.application.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.codescape.metronome.application.repository.MetronomeRepository
import studio.codescape.metronome.domain.model.Metronome
import studio.codescape.metronome.domain.usecase.GetMetronome

class GetMetronomeImpl(
    private val metronomeRepository: MetronomeRepository
) : GetMetronome {
    override fun invoke(): Flow<Metronome> = metronomeRepository
        .metronome
        .map { metronome -> metronome ?: defaultMetronome }

    private companion object {
        private val defaultMetronome = Metronome.Idle(60)
    }
}