package studio.codescape.metronome.domain.usecase.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import studio.codescape.metronome.domain.model.settings.Settings
import studio.codescape.metronome.player.domain.usecase.settings.GetPlayerSettings

@Inject
class GetMetronomeSettings(
    private val getConductorSettings: GetConductorSettings,
    private val getPlayerSettings: GetPlayerSettings
) {
    operator fun invoke(): Flow<Settings> = combine(
        getConductorSettings(),
        getPlayerSettings(),
        ::Settings
    )
}