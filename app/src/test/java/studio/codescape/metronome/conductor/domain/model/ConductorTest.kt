package studio.codescape.metronome.conductor.domain.model

import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import studio.codescape.metronome.conductor.domain.model.settings.Settings
import studio.codescape.metronome.conductor.domain.repository.SettingsRepository
import studio.codescape.metronome.conductor.domain.usecase.settings.GetConductorSettings
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext

class ConductorTest : StateHolderTest<Conductor>() {

    @Mock
    private lateinit var mockGetConductorSettings: GetConductorSettings

    @Mock
    private lateinit var mockSettingsRepository: SettingsRepository

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): Conductor = Conductor(
        getConductorSettings = mockGetConductorSettings,
        settingsRepository = mockSettingsRepository,
        parentCoroutineContext = parentCoroutineContext
    )

    @Before
    override fun before() {
        super.before()
        whenever(mockGetConductorSettings.invoke()).thenReturn(flowOf(STUB_SETTINGS))
    }

    @Test
    fun `toggles metronome state between idle and running states`() =
        runStateHolderTest { metronome ->
            metronome.state.observe {
                advanceUntilIdle()
                metronome.handleCommand(Conductor.Command.Toggle)
                advanceUntilIdle()
                expectValues(
                    Conductor.State.Loading,
                    Conductor.State.Paused(STUB_SETTINGS),
                    Conductor.State.Resumed(STUB_SETTINGS)
                )
            }
        }

    @Test
    fun `publishes metronome beats when running state is active`() =
        runStateHolderTest { metronome ->
            val oneMinuteMillis = 1000L * 60
            fun advanceTimeFor1Beat() = advanceTimeBy(oneMinuteMillis / STUB_SETTINGS.beatsPerMinute)
            metronome.effects.observe {
                advanceUntilIdle()

                metronome.handleCommand(Conductor.Command.Toggle)

                repeat(5) { iter ->
                    advanceTimeFor1Beat()
                    expectValues(*(0..iter).map { Conductor.Effect.Beat }.toTypedArray())
                }
                verify(mockGetConductorSettings).invoke()
            }
        }

    private companion object {
        private val STUB_SETTINGS = Settings(
            beatsPerMinute = 60
        )
    }

}