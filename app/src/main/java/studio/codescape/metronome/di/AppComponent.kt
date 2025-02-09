package studio.codescape.metronome.di

import android.content.Context
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class AppScope

@AppScope
@Component
abstract class AppComponent(
    @get:Provides
    val applicationContext: Context
)

