package studio.codescape.metronome.domain.model

sealed interface State {

    data class Transitioning(
        val targetState: State
    ) : State

    data object Idle : State

    sealed interface Ready : State {
        data object Resumed : Ready
        data object Paused : Ready
    }


}