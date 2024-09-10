package studio.codescape.metronome.conductor.application.usecase.settings

import kotlinx.coroutines.flow.first
import studio.codescape.metronome.conductor.application.repository.ConductorSettingsRepository
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import studio.codescape.metronome.conductor.domain.usecase.settings.SetBeatsPerMinuteSettings

class SetBeatsPerMinuteSettingsImpl(
    private val getConductorSettings: GetConductorSettings,
    private val conductorSettingsRepository: ConductorSettingsRepository
) : SetBeatsPerMinuteSettings {
    override suspend fun invoke(beatsPerMinute: Int) {
        conductorSettingsRepository.set(
            getConductorSettings().first().copy(beatsPerMinute = beatsPerMinute)
        )
    }

}