package studio.codescape.metronome.player.domain.model

sealed interface State {

    data object Loading : State

    data class Ready(
        val metronomeState: studio.codescape.metronome.domain.model.State
    ) : State

    data object Failure: State
}