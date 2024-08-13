@file:OptIn(ExperimentalStdlibApi::class, ExperimentalCoroutinesApi::class)

package studio.codescape.metronome.application.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import studio.codescape.metronome.domain.model.Metronome

@RunWith(RobolectricTestRunner::class)
class MetronomeRepositoryDataStoreImplTest {

    @Test
    fun `stores metronome`() = runTest {
        Dispatchers.setMain(coroutineContext[CoroutineDispatcher.Key]!!)
        val datastore = PreferenceDataStoreFactory.create(
            scope = this
        ) {
            RuntimeEnvironment.getApplication().preferencesDataStoreFile(storageKey)
        }
        advanceUntilIdle()
        val metronomeRepository = MetronomeRepositoryDataStoreImpl(
            datastore
        )

        assertNull(metronomeRepository.metronome.first())
        metronomeRepository.set(stubMetronome)
        advanceUntilIdle()

        assertEquals(
            stubMetronome,
            metronomeRepository.metronome.first()
        )
        coroutineContext.cancelChildren()
    }

    private companion object {
        private const val storageKey = "METRONOME_STORAGE"
        private val stubMetronome = Metronome.Running(100)
    }

}