package studio.codescape.metronome.player.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.media3.exoplayer.ExoPlayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope
import studio.codescape.metronome.common.di.CoroutineDispatchers
import studio.codescape.metronome.player.application.repository.SettingsRepositoryDataStoreImpl
import studio.codescape.metronome.player.application.usecase.GetSoundLoadedImpl
import studio.codescape.metronome.player.application.usecase.PlaySoundImpl
import studio.codescape.metronome.player.domain.model.Player
import studio.codescape.metronome.player.domain.repository.SettingsRepository
import studio.codescape.metronome.player.domain.usecase.GetSoundLoaded
import studio.codescape.metronome.player.domain.usecase.PlaySound
import kotlin.coroutines.CoroutineContext


typealias PlayerStorageFileName = String

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class PlayerScope

typealias GetMediaPlayer = (context: Context) -> androidx.media3.common.Player

@PlayerScope
@Component
abstract class PlayerComponent(
    @get:Provides val applicationContext: Context,
    @get:Provides val coroutineDispatchers: CoroutineDispatchers,
    @get:Provides val parentCoroutineContext: CoroutineContext,
    @get:Provides val storageFileName: PlayerStorageFileName = DEFAULT_DATA_STORE_FILE_NAME,
    @get:Provides val getMediaPlayer: GetMediaPlayer = ::getMediaPlayer
) : PlayerSettings {

    abstract val player: Player

    internal val PlaySoundImpl.bind: PlaySound
        @Provides get() = this
    internal val GetSoundLoadedImpl.bind: GetSoundLoaded
        @Provides get() = this

    @Provides
    @PlayerScope
    internal fun mediaPlayer(
        context: Context,
        getMediaPlayer: GetMediaPlayer
    ): androidx.media3.common.Player = getMediaPlayer(context)

     companion object {
        const val DEFAULT_DATA_STORE_FILE_NAME = "player settings"
        fun getMediaPlayer(context: Context): androidx.media3.common.Player =
            ExoPlayer.Builder(context).build()
    }
}

interface PlayerSettings {

    // TODO: remove from public API
    val SettingsRepositoryDataStoreImpl.bind: SettingsRepository
        @Provides get() = this

    @Provides
    @PlayerScope
    fun dataStore(
        context: Context,
        coroutineDispatchers: CoroutineDispatchers,
        parentCoroutineContext: CoroutineContext,
        storageFileName: PlayerStorageFileName
    ): DataStore<androidx.datastore.preferences.core.Preferences> =
        PreferenceDataStoreFactory.create(
            scope = CoroutineScope(parentCoroutineContext + Job() + coroutineDispatchers.io)
        ) {
            context.preferencesDataStoreFile(storageFileName)
        }
}