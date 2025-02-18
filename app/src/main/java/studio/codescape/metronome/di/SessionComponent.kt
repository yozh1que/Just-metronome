package studio.codescape.metronome.di

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import studio.codescape.metronome.common.di.CoroutineDispatchers
import studio.codescape.metronome.conductor.di.ConductorComponent
import studio.codescape.metronome.conductor.di.create
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import studio.codescape.metronome.domain.model.Metronome
import studio.codescape.metronome.player.di.PlayerComponent
import studio.codescape.metronome.player.di.create
import studio.codescape.metronome.player.domain.usecase.settings.GetPlayerSettings
import studio.codescape.metronome.ui.MetronomeScreen
import kotlin.coroutines.CoroutineContext

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class SessionScope

typealias SessionCoroutineScope = CoroutineScope

typealias GetPlayerComponent = (
    context: Context,
    coroutineDispatchers: CoroutineDispatchers,
    sessionCoroutineScope: SessionCoroutineScope
) -> PlayerComponent

@Component
@SessionScope
abstract class SessionComponent(
    @Component val appComponent: AppComponent,
    @get:Provides val parentCoroutineContext: CoroutineContext,
    @get:Provides val getPlayerComponent: GetPlayerComponent = ::getPlayerComponent
) : MetronomeUiComponent {

    abstract val metronome: Metronome
    abstract val sessionCoroutineScope: SessionCoroutineScope

    @SessionScope
    @Provides
    fun metronome(
        sessionCoroutineScope: SessionCoroutineScope,
        conductorComponent: ConductorComponent,
        playerComponent: PlayerComponent,
    ): Metronome = Metronome(
        conductorComponent.conductor,
        playerComponent.player,
        sessionCoroutineScope.coroutineContext
    )

    @SessionScope
    @Provides
    internal fun sessionCoroutineScope(parentCoroutineContext: CoroutineContext): SessionCoroutineScope =
        CoroutineScope(parentCoroutineContext)

    @SessionScope
    @Provides
    internal fun conductorComponent(
        context: Context,
        coroutineDispatchers: CoroutineDispatchers,
        sessionCoroutineScope: SessionCoroutineScope,
    ): ConductorComponent = ConductorComponent::class.create(
        context = context,
        coroutineDispatchers = coroutineDispatchers,
        parentCoroutineContext = sessionCoroutineScope.coroutineContext
    )

    @Provides
    internal fun getConductorSettings(conductorComponent: ConductorComponent): GetConductorSettings =
        conductorComponent.getConductorSettings

    @SessionScope
    @Provides
    internal fun playerComponent(
        context: Context,
        coroutineDispatchers: CoroutineDispatchers,
        sessionCoroutineScope: SessionCoroutineScope,
        getPlayerComponent: GetPlayerComponent
    ): PlayerComponent = getPlayerComponent(context, coroutineDispatchers, sessionCoroutineScope)

    @Provides
    internal fun getPlayerSettings(playerComponent: PlayerComponent): GetPlayerSettings =
        playerComponent.getPlayerSettings

    companion object {
        internal fun getPlayerComponent(
            context: Context,
            coroutineDispatchers: CoroutineDispatchers,
            sessionCoroutineScope: SessionCoroutineScope
        ): PlayerComponent = PlayerComponent::class.create(
            applicationContext = context,
            coroutineDispatchers = coroutineDispatchers,
            parentCoroutineContext = sessionCoroutineScope.coroutineContext
        )
    }

}

interface MetronomeUiComponent {
    val metronomeScreen: MetronomeScreen
}