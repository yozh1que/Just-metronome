package studio.codescape.metronome.conductor.domain.usecase

import studio.codescape.metronome.conductor.domain.model.Conductor

interface CreateConductor {
    suspend operator fun invoke(): Conductor?
}