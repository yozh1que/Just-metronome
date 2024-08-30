package studio.codescape.metronome.application.usecase.settings

import kotlinx.coroutines.flow.first
import studio.codescape.metronome.application.repository.MetronomeSettingsRepository
import studio.codescape.metronome.domain.usecase.settings.GetMetronomeSettings
import studio.codescape.metronome.domain.usecase.settings.SetBeatsPerMinuteSettings

class SetBeatsPerMinuteSettingsImpl(
    private val getMetronomeSettings: GetMetronomeSettings,
    private val metronomeSettingsRepository: MetronomeSettingsRepository
) : SetBeatsPerMinuteSettings {
    override suspend fun invoke(beatsPerMinute: Int) {
        metronomeSettingsRepository.set(
            getMetronomeSettings().first().copy(beatsPerMinute = beatsPerMinute)
        )
    }

}