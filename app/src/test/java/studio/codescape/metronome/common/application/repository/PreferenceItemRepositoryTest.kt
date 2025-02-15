package studio.codescape.metronome.common.application.repository

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import studio.codescape.metronome.conductor.domain.model.settings.Settings
import studio.codescape.metronome.test.StateHolderTest
import studio.codescape.metronome.test.observer.observe
import kotlin.coroutines.CoroutineContext
import kotlin.reflect.typeOf

@RunWith(RobolectricTestRunner::class)
class PreferenceItemRepositoryTest : StateHolderTest<PreferenceItemRepository<Settings>>() {

    override fun createStateHolder(parentCoroutineContext: CoroutineContext): PreferenceItemRepository<Settings> {
        val datastore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(parentCoroutineContext)
        ) {
            RuntimeEnvironment.getApplication().preferencesDataStoreFile(DATA_STORE_FILE_NAME)
        }
        return PreferenceItemRepository(
            datastore,
            key = SETTING_KEY,
            type = typeOf<Settings>()
        )
    }

    @Test
    fun `stores values`() = runStateHolderTest { repository ->
        repository.values.observe {
            advanceUntilIdle()
            launch { repository.set(STUB_SETTINGS) }
            advanceUntilIdle()

            expectValues(
                null, // initially empty
                STUB_SETTINGS,
            )
        }
    }

    private companion object {
        private const val DATA_STORE_FILE_NAME = "METRONOME_STORAGE"
        private const val SETTING_KEY = "conductor settings"
        private val STUB_SETTINGS = Settings(60)
    }

}