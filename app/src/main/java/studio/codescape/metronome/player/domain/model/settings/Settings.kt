package studio.codescape.metronome.player.domain.model.settings

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val soundUri: String
)