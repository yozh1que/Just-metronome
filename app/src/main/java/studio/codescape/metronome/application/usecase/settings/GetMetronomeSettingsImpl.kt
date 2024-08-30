package studio.codescape.metronome.application.usecase.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import studio.codescape.metronome.application.repository.MetronomeSettingsRepository
import studio.codescape.metronome.domain.model.settings.Settings
import studio.codescape.metronome.domain.usecase.settings.GetMetronomeSettings

class GetMetronomeSettingsImpl(
    private val metronomeSettingsRepository: MetronomeSettingsRepository
) : GetMetronomeSettings {
    override fun invoke(): Flow<Settings> = metronomeSettingsRepository
        .settings
        .map { metronome -> metronome ?: defaultSettings }

    private companion object {
        private val defaultSettings = Settings(
            beatsPerMinute = 60
        )
    }
}