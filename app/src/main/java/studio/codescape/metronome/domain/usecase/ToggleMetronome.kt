package studio.codescape.metronome.domain.usecase

import studio.codescape.metronome.domain.model.Command
import studio.codescape.metronome.domain.model.Metronome

class ToggleMetronome(
    private val metronome: Metronome
) {
    operator fun invoke() = metronome.handleCommand(Command.Toggle)
}