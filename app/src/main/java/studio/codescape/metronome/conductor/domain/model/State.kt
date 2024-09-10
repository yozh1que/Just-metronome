package studio.codescape.metronome.conductor.domain.model

sealed interface State {

    data object Paused : State

    data object Resumed : State

}