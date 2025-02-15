package studio.codescape.metronome.player.application.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.flow.Flow
import me.tatarka.inject.annotations.Inject
import studio.codescape.metronome.common.application.repository.PreferenceItemRepository
import studio.codescape.metronome.player.domain.model.settings.Settings
import kotlin.reflect.typeOf

@Inject
class SettingsRepositoryDataStoreImpl(
    dataStore: DataStore<Preferences>,
) : SettingsRepository {

    private val itemRepository = PreferenceItemRepository<Settings>(
        dataStore = dataStore,
        key = KEY,
        type = typeOf<Settings>()
    )

    override val settings: Flow<Settings?> = itemRepository.values

    override suspend fun set(settings: Settings) = itemRepository.set(settings)

    private companion object {
        private const val KEY = "player settings"
    }
}