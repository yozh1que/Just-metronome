package studio.codescape.metronome.player.application.repository

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.player.domain.model.settings.Settings

interface PlayerSettingsRepository {
    val settings: Flow<Settings?>

    suspend fun set(settings: Settings)
}