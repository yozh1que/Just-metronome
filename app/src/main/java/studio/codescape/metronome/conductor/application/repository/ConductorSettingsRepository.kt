package studio.codescape.metronome.conductor.application.repository

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.conductor.domain.model.settings.Settings

interface ConductorSettingsRepository {
    val settings: Flow<Settings?>

    suspend fun set(settings: Settings)
}