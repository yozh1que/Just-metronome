package studio.codescape.metronome.application.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import studio.codescape.metronome.application.repository.MetronomeRepository
import studio.codescape.metronome.domain.model.Metronome

class GetMetronomeImplTest {

    @Mock
    private lateinit var mockMetronomeRepository: MetronomeRepository

    private lateinit var getMetronomeImpl: GetMetronomeImpl

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `returns default metronome if not set`() = runTest {
        whenever(mockMetronomeRepository.metronome).thenReturn(flowOf(null))
        assertEquals(
            Metronome.Idle(60),
            GetMetronomeImpl(mockMetronomeRepository).invoke().first()
        )
    }
}