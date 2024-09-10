package studio.codescape.metronome.conductor.domain.usecase

import studio.codescape.metronome.conductor.domain.model.Command
import studio.codescape.metronome.conductor.domain.model.Conductor

class ToggleConductor(
    private val conductor: Conductor
) {
    operator fun invoke() = conductor.handleCommand(Command.Toggle)
}