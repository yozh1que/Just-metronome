package studio.codescape.metronome.common.di

import kotlinx.coroutines.CoroutineDispatcher
import studio.codescape.metronome.di.AppScope

typealias MainDispatcher = CoroutineDispatcher
typealias IoDispatcher = CoroutineDispatcher
typealias ComputationDispatcher = CoroutineDispatcher

@AppScope
class CoroutineDispatchers(
    val main: MainDispatcher,
    val io: IoDispatcher,
    val computation: ComputationDispatcher
)