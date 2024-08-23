package studio.codescape.metronome.test.observer

import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

class TestObserverTest {


    @Test
    fun `collects data from flow`() = runTest {
        val data = listOf(1, 2, 3, 4, 5)
        flowOf(data).flatMapConcat(::flowOf).observe {
            advanceUntilIdle()
            expectValues(data)
        }
    }

    @Test
    fun `collects no data from flow`() = runTest {
        emptyFlow<Int>().observe {
            advanceUntilIdle()
            expectNoValues()
        }
    }
}