package studio.codescape.metronome.conductor.domain.usecase.settings

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.conductor.domain.model.settings.Settings

class SettingsInteractor(
    private val getConductorSettings: GetConductorSettings,
    private val setBeatsPerMinuteSettings: SetBeatsPerMinuteSettings
) {
    val settings: Flow<Settings>
        get() = getConductorSettings()

    suspend fun setBeatsPerMinute(beatsPerMinute: Int) = setBeatsPerMinuteSettings(beatsPerMinute)
}