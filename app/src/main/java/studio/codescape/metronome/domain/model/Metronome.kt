package studio.codescape.metronome.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.conductor.domain.model.Conductor
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import studio.codescape.metronome.di.SessionScope
import kotlin.coroutines.CoroutineContext

@SessionScope
@Inject
class Metronome(
    conductor: Conductor,
    getConductorSettings: GetConductorSettings,
    val parentCoroutineContext: CoroutineContext
) : CoroutineScope {

    sealed interface State {

        data object Loading : State {
            override val settings: Settings? = null
        }

        val settings: Settings?

        sealed interface Ready : State {

            override val settings: Settings

            data class Resumed(
                override val settings: Settings
            ) : Ready

            data class Paused(
                override val settings: Settings
            ) : Ready

        }
    }

    val state: Flow<State> = combine(
        conductor.state,
        getConductorSettings()
    ) { conductorState, conductorSettings ->
        when (conductorState) {
            is Conductor.State.Paused -> State.Ready.Paused(
                settings = Settings(conductorSettings),
            )

            is Conductor.State.Resumed -> State.Ready.Resumed(
                settings = Settings(conductorSettings)
            )
        }
    }
        .stateIn(this, SharingStarted.WhileSubscribed(), State.Loading)

    val beats = conductor.effects
        .map { }


    override val coroutineContext: CoroutineContext = parentCoroutineContext + Job()

}


typealias ConductorSettings = studio.codescape.metronome.conductor.domain.model.settings.Settings
typealias PlayerSettings = studio.codescape.metronome.player.domain.model.settings.Settings