package studio.codescape.metronome.domain.model

import kotlinx.serialization.Serializable
@Serializable
sealed interface Metronome {

    val beatsPerMinute: Int

    @Serializable
    data class Idle(
        override val beatsPerMinute: Int
    ) : Metronome

    @Serializable
    data class Running(
        override val beatsPerMinute: Int
    ) : Metronome
}