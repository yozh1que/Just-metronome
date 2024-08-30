package studio.codescape.metronome.player.domain.model

sealed interface Command {
    @JvmInline
    value class SetSound(val uri: String) : Command
}