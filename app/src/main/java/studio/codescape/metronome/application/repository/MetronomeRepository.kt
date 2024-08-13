package studio.codescape.metronome.application.repository

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.domain.model.Metronome

interface MetronomeRepository {
    val metronome: Flow<Metronome?>

    suspend fun set(metronome: Metronome)
}