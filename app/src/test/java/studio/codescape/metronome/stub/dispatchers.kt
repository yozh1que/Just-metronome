package studio.codescape.metronome.stub

import kotlinx.coroutines.CoroutineDispatcher
import studio.codescape.metronome.common.di.CoroutineDispatchers
import kotlin.coroutines.CoroutineContext

fun CoroutineContext.coroutineDispatchers() = CoroutineDispatchers(
    main = get(CoroutineDispatcher.Key)!!,
    io = get(CoroutineDispatcher.Key)!!,
    computation = get(CoroutineDispatcher.Key)!!,
)