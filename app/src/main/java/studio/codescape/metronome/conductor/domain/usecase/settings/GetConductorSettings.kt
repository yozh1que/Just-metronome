package studio.codescape.metronome.conductor.domain.usecase.settings

import kotlinx.coroutines.flow.Flow
import studio.codescape.metronome.conductor.domain.model.settings.Settings

interface GetConductorSettings {
    operator fun invoke(): Flow<Settings>
}