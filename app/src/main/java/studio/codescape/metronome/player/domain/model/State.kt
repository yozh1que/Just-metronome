package studio.codescape.metronome.player.domain.model

sealed interface State {

    val soundUri: String

    data class Loading(
        override val soundUri: String
    ) : State

    data class Ready(
        override val soundUri: String
    ) : State

    data class Failure(
        override val soundUri: String
    ) : State
}