package studio.codescape.metronome.application.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
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
import studio.codescape.metronome.conductor.application.repository.ConductorSettingsRepositoryDataStoreImpl
import studio.codescape.metronome.conductor.domain.model.settings.Settings

@RunWith(RobolectricTestRunner::class)
class ConductorSettingsRepositoryDataStoreImplTest {

    @Test
    fun `stores metronome`() = runTest {
        Dispatchers.setMain(coroutineContext[CoroutineDispatcher.Key]!!)
        val datastore = PreferenceDataStoreFactory.create(
            scope = this
        ) {
            RuntimeEnvironment.getApplication().preferencesDataStoreFile(storageKey)
        }
        advanceUntilIdle()
        val metronomeRepository = ConductorSettingsRepositoryDataStoreImpl(
            datastore
        )

        assertNull(metronomeRepository.settings.first())
        metronomeRepository.set(stubSettings)
        advanceUntilIdle()

        assertEquals(
            stubSettings,
            metronomeRepository.settings.first()
        )
        coroutineContext.cancelChildren()
    }

    private companion object {
        private const val storageKey = "METRONOME_STORAGE"
        private val stubSettings = Settings(60)
    }

}