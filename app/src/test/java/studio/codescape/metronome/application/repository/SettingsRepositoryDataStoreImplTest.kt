package studio.codescape.metronome.application.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import studio.codescape.metronome.conductor.application.repository.SettingsRepositoryDataStoreImpl
import studio.codescape.metronome.conductor.domain.model.settings.Settings
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext

@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryDataStoreImplTest : StateHolderTest<SettingsRepositoryDataStoreImpl>() {

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): SettingsRepositoryDataStoreImpl {
        val datastore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(parentCoroutineContext)
        ) {
            RuntimeEnvironment.getApplication().preferencesDataStoreFile(DATA_STORE_FILE_NAME)
        }
        return SettingsRepositoryDataStoreImpl(
            datastore
        )
    }


    @Test
    fun `stores conductor settings`() = runStateHolderTest { repository ->
        repository.settings.observe {
            advanceUntilIdle()
            launch { repository.set(STUB_SETTINGS) }
            advanceUntilIdle()

            expectValues(
                null,
                STUB_SETTINGS,
            )
        }
    }

    private companion object {
        private const val DATA_STORE_FILE_NAME = "METRONOME_STORAGE"

        private val STUB_SETTINGS = Settings(60)
    }

}