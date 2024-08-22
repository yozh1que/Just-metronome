package studio.codescape.metronome.domain.model

sealed interface State {

    data object Paused : State

    data object Resumed : State

}