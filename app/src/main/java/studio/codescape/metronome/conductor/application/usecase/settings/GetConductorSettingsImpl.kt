package studio.codescape.metronome.conductor.application.usecase.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.codescape.metronome.conductor.application.repository.ConductorSettingsRepository
import studio.codescape.metronome.conductor.domain.model.settings.Settings
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings

class GetConductorSettingsImpl(
    private val conductorSettingsRepository: ConductorSettingsRepository
) : GetConductorSettings {
    override fun invoke(): Flow<Settings> = conductorSettingsRepository
        .settings
        .map { metronome -> metronome ?: defaultSettings }

    private companion object {
        private val defaultSettings = Settings(
            beatsPerMinute = 60
        )
    }
}