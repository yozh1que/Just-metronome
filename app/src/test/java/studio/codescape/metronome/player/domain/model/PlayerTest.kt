@file:OptIn(ExperimentalCoroutinesApi::class)

package studio.codescape.metronome.player.domain.model

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.whenever
import studio.codescape.metronome.domain.model.Metronome
import studio.codescape.metronome.player.domain.usecase.GetSoundUri
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext

class PlayerTest : StateHolderTest<Player>() {

    @Mock
    private lateinit var mockMetronome: Metronome

    @Mock
    private lateinit var mockGetSoundUri: GetSoundUri

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): Player = Player(
        mockMetronome,
        mockGetSoundUri,
        parentCoroutineContext
    )

    @Before
    fun before() {
        MockitoAnnotations.openMocks(this)
        whenever(mockMetronome.state).thenReturn(flowOf(stubInitialMetronomeState))
    }

    @Test
    fun `loads resources when initialized`() = runStateHolderTest(before = {
        whenever(mockGetSoundUri.invoke()).thenReturn(flowOf(stubSoundUri))
    }) { player ->
        player.state.observe {
            advanceUntilIdle()

            expectValues(
                State.Loading,
                State.Ready(
                    metronomeState = stubInitialMetronomeState
                )
            )
        }
    }

    @Test
    fun `treats null sound uri as sound being loaded`() = runStateHolderTest(before = {
        whenever(mockGetSoundUri.invoke()).thenReturn(flowOf(stubSoundUriBeingLoaded))
    }) { player ->
        player.state.observe {
            advanceUntilIdle()
            expectValues(
                State.Loading
            )
        }
    }

    private companion object {
        private val stubInitialMetronomeState = studio.codescape.metronome.domain.model.State.Paused
        private const val stubSoundUri = ""
        private val stubSoundUriBeingLoaded: String? = null
    }
}