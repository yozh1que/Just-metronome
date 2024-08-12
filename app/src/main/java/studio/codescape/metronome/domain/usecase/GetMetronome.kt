package studio.codescape.metronome.domain.usecase

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.domain.model.Metronome

interface GetMetronome {
    operator fun invoke(): Flow<Metronome>
}