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
import studio.codescape.metronome.conductor.application.repository.ConductorSettingsRepository
import studio.codescape.metronome.conductor.application.usecase.settings.GetConductorSettingsImpl
import studio.codescape.metronome.conductor.domain.model.settings.Settings

class GetConductorSettingsImplTest {

    @Mock
    private lateinit var mockConductorSettingsRepository: ConductorSettingsRepository

    private lateinit var getMetronomeImpl: GetConductorSettingsImpl

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun `returns default metronome settings if not set`() = runTest {
        whenever(mockConductorSettingsRepository.settings).thenReturn(flowOf(null))
        assertEquals(
            Settings(
                beatsPerMinute = 60
            ),
            GetConductorSettingsImpl(mockConductorSettingsRepository).invoke().first()
        )
    }
}