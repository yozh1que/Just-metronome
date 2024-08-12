package studio.codescape.metronome.domain.usecase

import kotlinx.coroutines.flow.Flow

interface GetMetronomeBeat {
    operator fun invoke(): Flow<Unit>
}