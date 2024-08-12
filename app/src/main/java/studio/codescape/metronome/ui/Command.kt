package studio.codescape.metronome.ui

sealed interface Command {
    data object Retry : Command
}