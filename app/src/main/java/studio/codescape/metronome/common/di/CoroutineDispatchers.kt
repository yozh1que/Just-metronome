package studio.codescape.metronome.common.di

import kotlinx.coroutines.CoroutineDispatcher

typealias MainDispatcher = CoroutineDispatcher
typealias IoDispatcher = CoroutineDispatcher

class CoroutineDispatchers(
    val main: MainDispatcher,
    val io: IoDispatcher
)