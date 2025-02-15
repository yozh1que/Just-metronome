package studio.codescape.metronome.player.domain.model

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import studio.codescape.metronome.player.domain.model.Player.Command
import studio.codescape.metronome.player.domain.model.Player.State
import studio.codescape.metronome.player.domain.model.settings.Settings
import studio.codescape.metronome.player.domain.repository.SettingsRepository
import studio.codescape.metronome.player.domain.usecase.GetSoundLoaded
import studio.codescape.metronome.player.domain.usecase.PlaySound
import studio.codescape.metronome.player.domain.usecase.settings.GetPlayerSettings
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext

class PlayerTest : StateHolderTest<Player>() {

    @Mock
    private lateinit var mockSettingsRepository: SettingsRepository

    @Mock
    private lateinit var mockGetPlayerSettings: GetPlayerSettings

    @Mock
    private lateinit var mockGetSoundLoaded: GetSoundLoaded

    @Mock
    private lateinit var mockPlaySound: PlaySound


    override fun createStateHolder(parentCoroutineContext: CoroutineContext): Player = Player(
        mockGetPlayerSettings,
        mockSettingsRepository,
        mockGetSoundLoaded,
        mockPlaySound,
        parentCoroutineContext
    )

    @Before
    override fun before() {
        super.before()
        whenever(mockGetPlayerSettings.invoke()).thenReturn(flowOf(stubSettings))
        doNothing().whenever(mockPlaySound).invoke()
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
                State.Loading(stubSettings),
                State.Ready(stubSettings)
            )
        }
    }

    @Test
    fun `in ready state plays sound when command is received`() = runStateHolderTest { player ->
        player.state.observe {
            whenever(mockGetSoundLoaded.invoke()).thenReturn(flowOf(Unit))

            advanceUntilIdle()

            player.handleCommand(Command.PlaySound)

            advanceUntilIdle()

            verify(mockPlaySound).invoke()
        }
    }

    private companion object {
        private const val stubSoundUri = ""
        private val stubSettings = Settings(soundUri = stubSoundUri)
    }
}