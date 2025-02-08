package studio.codescape.metronome.di

import kotlinx.coroutines.CoroutineScope
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import studio.codescape.metronome.conductor.di.ConductorComponent
import studio.codescape.metronome.domain.model.Metronome
import kotlin.coroutines.CoroutineContext

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class SessionScope

@Component
@SessionScope
abstract class SessionComponent(
    @get:Provides val parentCoroutineContext: CoroutineContext,
    @Component val conductorComponent: ConductorComponent
) {


    @SessionScope
    @Provides
    internal fun sessionCoroutineScope(parentCoroutineContext: CoroutineContext) =
        CoroutineScope(parentCoroutineContext)

    abstract val metronome: Metronome
    abstract val sessionCoroutineScope: CoroutineScope

}