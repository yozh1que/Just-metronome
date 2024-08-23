package studio.codescape.metronome.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.coroutines.CoroutineContext

abstract class StateHolderTest<T> {

    abstract fun createStateHolder(parentCoroutineContext: CoroutineContext): T

    fun runStateHolderTest(
        before: TestScope.() -> Unit = {},
        block: suspend TestScope.(stateHolder: T) -> Unit
    ) = runTest {
        Dispatchers.setMain(coroutineContext[CoroutineDispatcher.Key]!!)
        before()
        block(createStateHolder(coroutineContext))
    }

}