package studio.codescape.metronome.domain.model

sealed interface State {

    data object Idle : State

    data object Running : State

}