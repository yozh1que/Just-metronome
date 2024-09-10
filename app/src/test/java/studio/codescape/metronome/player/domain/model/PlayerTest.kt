package studio.codescape.metronome.player.domain.model

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import studio.codescape.metronome.conductor.domain.model.Conductor
import studio.codescape.metronome.player.domain.model.settings.Settings
import studio.codescape.metronome.player.domain.usecase.GetSoundLoaded
import studio.codescape.metronome.player.domain.usecase.PlayBeatSound
import studio.codescape.metronome.player.domain.usecase.settings.SettingsInteractor
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext

class PlayerTest : StateHolderTest<Player>() {

    @Mock
    private lateinit var mockConductor: Conductor

    @Mock
    private lateinit var mockGetSoundLoaded: GetSoundLoaded

    @Mock
    private lateinit var mockPlayBeatSound: PlayBeatSound

    @Mock
    private lateinit var mockSettingsInteractor: SettingsInteractor

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): Player = Player(
        mockConductor,
        mockSettingsInteractor,
        mockGetSoundLoaded,
        mockPlayBeatSound,
        parentCoroutineContext
    )

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        whenever(mockConductor.state).thenReturn(flowOf(stubConductorState))
        whenever(mockConductor.effects).thenReturn(emptyFlow())
        whenever(mockSettingsInteractor.settings).thenReturn(flowOf(stubSettings))
        doNothing().whenever(mockPlayBeatSound).invoke()
    }

    @Test
    fun `loads resources when initialized`() = runStateHolderTest { player ->
        player.state.observe {
            val soundLoaded = Channel<Unit>()
            whenever(mockGetSoundLoaded.invoke()).thenReturn(soundLoaded.receiveAsFlow())

            advanceUntilIdle()
            launch { soundLoaded.send(Unit) }
            advanceUntilIdle()

            expectValues(
                State.Loading(stubSoundUri),
                State.Ready(stubSoundUri)
            )
        }
    }

    private companion object {
        private val stubConductorState = studio.codescape.metronome.conductor.domain.model.State.Paused
        private const val stubSoundUri = ""
        private val stubSettings = Settings(soundUri = stubSoundUri)
    }
}