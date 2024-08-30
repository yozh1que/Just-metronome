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
import studio.codescape.metronome.application.repository.MetronomeSettingsRepository
import studio.codescape.metronome.application.usecase.settings.GetMetronomeSettingsImpl
import studio.codescape.metronome.domain.model.settings.Settings

class GetMetronomeSettingsImplTest {

    @Mock
    private lateinit var mockMetronomeSettingsRepository: MetronomeSettingsRepository

    private lateinit var getMetronomeImpl: GetMetronomeSettingsImpl

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `returns default metronome settings if not set`() = runTest {
        whenever(mockMetronomeSettingsRepository.settings).thenReturn(flowOf(null))
        assertEquals(
            Settings(
                beatsPerMinute = 60
            ),
            GetMetronomeSettingsImpl(mockMetronomeSettingsRepository).invoke().first()
        )
    }
}