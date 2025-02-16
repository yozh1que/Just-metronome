package studio.codescape.metronome.di

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import studio.codescape.metronome.conductor.di.ConductorComponent
import studio.codescape.metronome.conductor.di.create
import studio.codescape.metronome.domain.model.Metronome
import studio.codescape.metronome.player.di.PlayerComponent
import studio.codescape.metronome.player.di.create
import studio.codescape.metronome.ui.MetronomeScreen
import kotlin.coroutines.CoroutineContext

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class SessionScope

typealias SessionCoroutineScope = CoroutineScope

typealias GetPlayerComponent = (context: Context, sessionCoroutineScope: SessionCoroutineScope) -> PlayerComponent

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
        conductorComponent.getConductorSettings,
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
        sessionCoroutineScope: SessionCoroutineScope,
    ): ConductorComponent = ConductorComponent::class.create(
        context = context,
        parentCoroutineContext = sessionCoroutineScope.coroutineContext
    )

    @SessionScope
    @Provides
    internal fun playerComponent(
        context: Context,
        sessionCoroutineScope: SessionCoroutineScope,
        getPlayerComponent: GetPlayerComponent
    ): PlayerComponent = getPlayerComponent(context, sessionCoroutineScope)

    companion object {
        internal fun getPlayerComponent(
            context: Context,
            sessionCoroutineScope: SessionCoroutineScope
        ): PlayerComponent = PlayerComponent::class.create(
            context = context,
            parentCoroutineContext = sessionCoroutineScope.coroutineContext
        )
    }

}

interface MetronomeUiComponent {
    val metronomeScreen: MetronomeScreen
}