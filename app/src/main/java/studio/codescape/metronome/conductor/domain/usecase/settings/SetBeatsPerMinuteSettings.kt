package studio.codescape.metronome.conductor.domain.usecase.settings

import kotlinx.coroutines.flow.first
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.conductor.domain.repository.SettingsRepository

@Inject
class SetBeatsPerMinuteSettings(
    private val getConductorSettings: GetConductorSettings,
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(beatsPerMinute: Int) {
        settingsRepository.set(
            getConductorSettings().first().copy(beatsPerMinute = beatsPerMinute)
        )
    }

}