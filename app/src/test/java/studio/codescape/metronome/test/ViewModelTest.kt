@file:OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)

package studio.codescape.metronome.test

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

abstract class ViewModelTest<T> {

    abstract fun createViewModel(): T

    fun runViewModelTest(before: TestScope.() -> Unit = {}, block: TestScope.(viewModel: T) -> Unit) = runTest {
        Dispatchers.setMain(coroutineContext[CoroutineDispatcher.Key]!!)
        before()
        block(createViewModel())
    }

}