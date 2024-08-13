package studio.codescape.metronome.domain.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val beatsPerMinute: Int
)