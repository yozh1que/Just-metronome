package studio.codescape.metronome.application.usecase

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.whenever
import studio.codescape.metronome.conductor.application.repository.SettingsRepository
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import studio.codescape.metronome.test.UnitTest

class GetConductorSettingsTest : UnitTest() {

    @Mock
    private lateinit var mockSettingsRepository: SettingsRepository

    @Test
    fun `returns default conductor settings if not set`() = runTest {
        whenever(mockSettingsRepository.settings).thenReturn(flowOf(null))

        assertEquals(
            GetConductorSettings.DEFAULT_SETTINGS,
            GetConductorSettings(mockSettingsRepository).invoke().first()
        )
    }
}