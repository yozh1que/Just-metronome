package studio.codescape.metronome.domain.model.settings

data class Settings(
    val conductorSettings: ConductorSettings,
    val playerSettings: PlayerSettings
)

typealias ConductorSettings = studio.codescape.metronome.conductor.domain.model.settings.Settings
typealias PlayerSettings = studio.codescape.metronome.player.domain.model.settings.Settings