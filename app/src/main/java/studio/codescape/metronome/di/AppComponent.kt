package studio.codescape.metronome.di

import android.content.Context
import kotlinx.coroutines.Dispatchers
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import studio.codescape.metronome.common.di.ComputationDispatcher
import studio.codescape.metronome.common.di.CoroutineDispatchers
import studio.codescape.metronome.common.di.IoDispatcher
import studio.codescape.metronome.common.di.MainDispatcher

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class AppScope

@AppScope
@Component
abstract class AppComponent(
    @get:Provides
    val applicationContext: Context
) : CoroutineDispatcherComponent

interface CoroutineDispatcherComponent {


    val dispatchers: CoroutineDispatchers

    @Provides
    fun io(): IoDispatcher = Dispatchers.IO

    @Provides
    fun computation(): ComputationDispatcher = Dispatchers.Default

    @Provides
    fun main(): MainDispatcher = Dispatchers.Main
}

