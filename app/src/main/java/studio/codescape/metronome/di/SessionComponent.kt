package studio.codescape.metronome.di

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import studio.codescape.metronome.conductor.di.ConductorComponent
import studio.codescape.metronome.conductor.di.create
import studio.codescape.metronome.domain.model.Metronome
import studio.codescape.metronome.ui.MetronomeScreen
import kotlin.coroutines.CoroutineContext

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class SessionScope

typealias SessionCoroutineScope = CoroutineScope

@Component
@SessionScope
abstract class SessionComponent(
    @Component val appComponent: AppComponent,
    @get:Provides val parentCoroutineContext: CoroutineContext,
) : MetronomeUiComponent {

    abstract val metronome: Metronome
    abstract val sessionCoroutineScope: SessionCoroutineScope

    @SessionScope
    @Provides
    fun metronome(
        conductorComponent: ConductorComponent,
        sessionCoroutineScope: SessionCoroutineScope,
    ): Metronome = Metronome(
        conductorComponent.conductor,
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

}

interface MetronomeUiComponent {
    val metronomeScreen: MetronomeScreen
}