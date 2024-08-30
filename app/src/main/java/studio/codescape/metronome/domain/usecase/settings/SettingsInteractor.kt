package studio.codescape.metronome.domain.usecase.settings

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.domain.model.settings.Settings

class SettingsInteractor(
    private val getMetronomeSettings: GetMetronomeSettings,
    private val setBeatsPerMinuteSettings: SetBeatsPerMinuteSettings
) {
    val settings: Flow<Settings>
        get() = getMetronomeSettings()

    suspend fun setBeatsPerMinute(beatsPerMinute: Int) = setBeatsPerMinuteSettings(beatsPerMinute)
}