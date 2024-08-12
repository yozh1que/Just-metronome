package studio.codescape.metronome.ui

sealed interface Command {
    object TogglePlayback : Command
}