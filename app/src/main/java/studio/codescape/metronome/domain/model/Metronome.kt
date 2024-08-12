package studio.codescape.metronome.domain.model

sealed interface Metronome {

    val beatsPerMinute: Int

    data class Idle(
        override val beatsPerMinute: Int
    ) : Metronome

    data class Running(
        override val beatsPerMinute: Int
    ) : Metronome
}