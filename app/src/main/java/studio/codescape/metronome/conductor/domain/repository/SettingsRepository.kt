package studio.codescape.metronome.conductor.domain.repository

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.conductor.domain.model.settings.Settings

interface SettingsRepository {
    val settings: Flow<Settings?>

    suspend fun set(settings: Settings)
}