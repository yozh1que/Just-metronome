package studio.codescape.metronome.conductor.domain.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val beatsPerMinute: Int
)