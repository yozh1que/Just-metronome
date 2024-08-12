package studio.codescape.metronome.test.observer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals

context(CoroutineScope)
class TestObserver<T>(
    private val flow: Flow<T>
) {

    private val values: MutableList<T> = mutableListOf()
    private var job: Job? = null

    init {
        collect()
    }

    private fun collect() {
        println("$coroutineContext")
        job = launch {
            flow
                .collect { value ->
                    println("collect $value")
                    values.add(value)
                }
        }
    }

    fun expectValues(vararg values: T) {
        assertEquals(values.toList(), this.values)
    }

    fun expectNoValues() {
        assertEquals(emptyList<T>(), this.values)
    }

    fun clear() {
        job?.cancel()
        job = null
    }

}

context(CoroutineScope)
fun <T> Flow<T>.observer(): TestObserver<T> = TestObserver(this)

context(CoroutineScope)
fun <T> Flow<T>.observe(block: TestObserver<T>.() -> Unit) = with(TestObserver(this)) {
    block()
    clear()
}