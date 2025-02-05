package studio.codescape.metronome.conductor.application.usecase.settings

import kotlinx.coroutines.flow.first
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.common.qualifiers.Singleton
import studio.codescape.metronome.conductor.application.repository.ConductorSettingsRepository
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import studio.codescape.metronome.conductor.domain.usecase.settings.SetBeatsPerMinuteSettings

@Inject
@Singleton
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