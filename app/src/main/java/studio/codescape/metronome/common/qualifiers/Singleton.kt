package studio.codescape.metronome.common.qualifiers

import me.tatarka.inject.annotations.Scope

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class Singleton