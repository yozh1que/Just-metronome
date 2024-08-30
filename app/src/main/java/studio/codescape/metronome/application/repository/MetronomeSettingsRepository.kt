package studio.codescape.metronome.application.repository

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.domain.model.settings.Settings

interface MetronomeSettingsRepository {
    val settings: Flow<Settings?>

    suspend fun set(settings: Settings)
}