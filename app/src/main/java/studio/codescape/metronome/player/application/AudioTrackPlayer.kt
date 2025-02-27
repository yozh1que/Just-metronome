package studio.codescape.metronome.player.application

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaCodec
import android.media.MediaExtractor
import android.media.MediaFormat
import android.os.Looper
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.TextureView
import androidx.annotation.OptIn
import androidx.media3.common.BasePlayer
import androidx.media3.common.DeviceInfo
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import java.io.ByteArrayOutputStream

private const val MEDIA_TRACK_INDEX = 0
private const val INPUT_AWAIT_TIMEOUT_MICROS = 10000L

@OptIn(UnstableApi::class)
class AudioTrackPlayer(private val context: Context) : BasePlayer() {

    private lateinit var audioTrack: AudioTrack

    private var state = STATE_IDLE

    private var listener: Player.Listener? = null

    private var soundMediaItem: MediaItem? = null

    override fun prepare() {
        updateState(STATE_BUFFERING)
        audioTrack =
            soundMediaItem?.let(::getLoadedAudioTrack)
                ?: throw IllegalStateException("No media item set")
        updateState(STATE_READY)
    }

    private fun getLoadedAudioTrack(mediaItem: MediaItem): AudioTrack {
        val extractor = MediaExtractor()
        extractor.setDataSource(
            context.assets.openFd(
                mediaItem.localConfiguration!!.uri.toString()
                    .replace(
                        "file:///android_asset/",
                        ""
                    )
            )
        )

        val format = extractor.getTrackFormat(MEDIA_TRACK_INDEX)

        val sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)

        val channelConfig = when (format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)) {
            1 -> AudioFormat.CHANNEL_OUT_MONO
            else -> AudioFormat.CHANNEL_OUT_STEREO
        }

        val pcmData = decodeAudioTrack(extractor, format)

        return AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build(),
            AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate)
                .setChannelMask(channelConfig)
                .build(),
            pcmData.size,
            AudioTrack.MODE_STATIC,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        ).also { audioTrack ->
            audioTrack.write(pcmData, 0, pcmData.size)
        }
    }

    private fun decodeAudioTrack(extractor: MediaExtractor, format: MediaFormat): ByteArray {
        var decoder: MediaCodec? = null
        val pcmData = ByteArrayOutputStream()
        try {
            extractor.selectTrack(MEDIA_TRACK_INDEX)
            decoder =
                MediaCodec.createDecoderByType(format.getString(MediaFormat.KEY_MIME)!!).apply {
                    configure(format, null, null, 0)
                    start()
                }
            val bufferInfo = MediaCodec.BufferInfo()
            var sawInputEOS = false
            var sawOutputEOS = false

            while (!sawOutputEOS) {
                if (!sawInputEOS) {
                    val inputIndex = decoder.dequeueInputBuffer(INPUT_AWAIT_TIMEOUT_MICROS)
                    if (inputIndex >= 0) {
                        val inputBuffer = decoder.getInputBuffer(inputIndex)
                        val sampleSize = extractor.readSampleData(inputBuffer!!, 0)

                        if (sampleSize >= 0) {
                            decoder.queueInputBuffer(
                                inputIndex,
                                0,
                                sampleSize,
                                extractor.sampleTime,
                                extractor.sampleFlags
                            )
                            extractor.advance()
                        } else {
                            sawInputEOS = true
                            decoder.queueInputBuffer(
                                inputIndex,
                                0,
                                0,
                                0,
                                MediaCodec.BUFFER_FLAG_END_OF_STREAM
                            )
                        }
                    }
                }

                val outputIndex =
                    decoder.dequeueOutputBuffer(bufferInfo, INPUT_AWAIT_TIMEOUT_MICROS)
                when {
                    outputIndex >= 0 -> {
                        val outputBuffer = decoder.getOutputBuffer(outputIndex)
                        if (bufferInfo.size > 0) {
                            val chunk = ByteArray(bufferInfo.size)
                            outputBuffer?.get(chunk)
                            outputBuffer?.clear()
                            pcmData.write(chunk)
                        }

                        decoder.releaseOutputBuffer(outputIndex, false)

                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            sawOutputEOS = true
                        }
                    }
                }
            }
        } finally {
            decoder?.stop()
            decoder?.release()
            extractor.release()
        }

        return pcmData.toByteArray()
    }

    override fun setPlayWhenReady(playWhenReady: Boolean) {
        when (playWhenReady) {
            true -> if (state == STATE_READY) {
                audioTrack.stop()
//                audioTrack.reloadStaticData()
                audioTrack.play()
            }

            false -> if (state == STATE_READY) {
                audioTrack.stop()
            }
        }
    }

    override fun setMediaItems(mediaItems: MutableList<MediaItem>, resetPosition: Boolean) {
        soundMediaItem = mediaItems.firstOrNull()
    }

    override fun setMediaItems(
        mediaItems: MutableList<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ) = setMediaItems(mediaItems)

    override fun getCurrentMediaItemIndex(): Int = MEDIA_TRACK_INDEX

    private fun updateState(@Player.State state: Int) {
        this.state = state
        listener?.onPlaybackStateChanged(state)
    }

    override fun addListener(listener: Player.Listener) {
        this.listener = listener
    }

    override fun removeListener(listener: Player.Listener) {
        if (this.listener == listener) {
            this.listener = null
        }
    }

    override fun stop() {
        playWhenReady = false
    }


    override fun release() {
        audioTrack.release()
    }

    override fun seekTo(
        mediaItemIndex: Int,
        positionMs: Long,
        seekCommand: Int,
        isRepeatingCurrentItem: Boolean
    ) {
        // no op
    }

    // the rest is irrelevant for now

    override fun getApplicationLooper(): Looper {
        TODO("getApplicationLooper Not yet implemented")
    }

    override fun addMediaItems(index: Int, mediaItems: MutableList<MediaItem>) {
        TODO("Not yet implemented")
    }

    override fun moveMediaItems(fromIndex: Int, toIndex: Int, newIndex: Int) {
        TODO("Not yet implemented")
    }

    override fun replaceMediaItems(
        fromIndex: Int,
        toIndex: Int,
        mediaItems: MutableList<MediaItem>
    ) {
        TODO("Not yet implemented")
    }

    override fun removeMediaItems(fromIndex: Int, toIndex: Int) {
        TODO("Not yet implemented")
    }

    override fun getAvailableCommands(): Player.Commands {
        TODO("Not yet implemented")
    }

    override fun getPlaybackState(): Int {
        TODO("Not yet implemented")
    }

    override fun getPlaybackSuppressionReason(): Int {
        TODO("Not yet implemented")
    }

    override fun getPlayerError(): PlaybackException? {
        TODO("Not yet implemented")
    }

    override fun getPlayWhenReady(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setRepeatMode(repeatMode: Int) {
        TODO("Not yet implemented")
    }

    override fun getRepeatMode(): Int {
        TODO("Not yet implemented")
    }

    override fun setShuffleModeEnabled(shuffleModeEnabled: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getShuffleModeEnabled(): Boolean {
        TODO("Not yet implemented")
    }

    override fun isLoading(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getSeekBackIncrement(): Long {
        TODO("Not yet implemented")
    }

    override fun getSeekForwardIncrement(): Long {
        TODO("Not yet implemented")
    }

    override fun getMaxSeekToPreviousPosition(): Long {
        TODO("Not yet implemented")
    }

    override fun setPlaybackParameters(playbackParameters: PlaybackParameters) {
        TODO("Not yet implemented")
    }

    override fun getPlaybackParameters(): PlaybackParameters {
        TODO("Not yet implemented")
    }

    override fun getCurrentTracks(): Tracks {
        TODO("Not yet implemented")
    }

    override fun getTrackSelectionParameters(): TrackSelectionParameters {
        TODO("Not yet implemented")
    }

    override fun setTrackSelectionParameters(parameters: TrackSelectionParameters) {
        TODO("Not yet implemented")
    }

    override fun getMediaMetadata(): MediaMetadata {
        TODO("Not yet implemented")
    }

    override fun getPlaylistMetadata(): MediaMetadata {
        TODO("Not yet implemented")
    }

    override fun setPlaylistMetadata(mediaMetadata: MediaMetadata) {
        TODO("Not yet implemented")
    }

    override fun getCurrentTimeline(): Timeline {
        TODO("Not yet implemented")
    }

    override fun getCurrentPeriodIndex(): Int {
        TODO("Not yet implemented")
    }

    override fun getDuration(): Long {
        TODO("Not yet implemented")
    }

    override fun getCurrentPosition(): Long {
        TODO("Not yet implemented")
    }

    override fun getBufferedPosition(): Long {
        TODO("Not yet implemented")
    }

    override fun getTotalBufferedDuration(): Long {
        TODO("Not yet implemented")
    }

    override fun isPlayingAd(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getCurrentAdGroupIndex(): Int {
        TODO("Not yet implemented")
    }

    override fun getCurrentAdIndexInAdGroup(): Int {
        TODO("Not yet implemented")
    }

    override fun getContentPosition(): Long {
        TODO("Not yet implemented")
    }

    override fun getContentBufferedPosition(): Long {
        TODO("Not yet implemented")
    }

    override fun getAudioAttributes(): androidx.media3.common.AudioAttributes {
        TODO("Not yet implemented")
    }

    override fun setVolume(volume: Float) {
        TODO("Not yet implemented")
    }

    override fun getVolume(): Float {
        TODO("Not yet implemented")
    }

    override fun clearVideoSurface() {
        TODO("Not yet implemented")
    }

    override fun clearVideoSurface(surface: Surface?) {
        TODO("Not yet implemented")
    }

    override fun setVideoSurface(surface: Surface?) {
        TODO("Not yet implemented")
    }

    override fun setVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {
        TODO("Not yet implemented")
    }

    override fun clearVideoSurfaceHolder(surfaceHolder: SurfaceHolder?) {
        TODO("Not yet implemented")
    }

    override fun setVideoSurfaceView(surfaceView: SurfaceView?) {
        TODO("Not yet implemented")
    }

    override fun clearVideoSurfaceView(surfaceView: SurfaceView?) {
        TODO("Not yet implemented")
    }

    override fun setVideoTextureView(textureView: TextureView?) {
        TODO("Not yet implemented")
    }

    override fun clearVideoTextureView(textureView: TextureView?) {
        TODO("Not yet implemented")
    }

    override fun getVideoSize(): VideoSize {
        TODO("Not yet implemented")
    }

    override fun getSurfaceSize(): Size {
        TODO("Not yet implemented")
    }

    override fun getCurrentCues(): CueGroup {
        TODO("Not yet implemented")
    }

    override fun getDeviceInfo(): DeviceInfo {
        TODO("Not yet implemented")
    }

    override fun getDeviceVolume(): Int {
        TODO("Not yet implemented")
    }

    override fun isDeviceMuted(): Boolean {
        TODO("Not yet implemented")
    }

    override fun setDeviceVolume(volume: Int) {
        TODO("Not yet implemented")
    }

    override fun setDeviceVolume(volume: Int, flags: Int) {
        TODO("Not yet implemented")
    }

    override fun increaseDeviceVolume() {
        TODO("Not yet implemented")
    }

    override fun increaseDeviceVolume(flags: Int) {
        TODO("Not yet implemented")
    }

    override fun decreaseDeviceVolume() {
        TODO("Not yet implemented")
    }

    override fun decreaseDeviceVolume(flags: Int) {
        TODO("Not yet implemented")
    }

    override fun setDeviceMuted(muted: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setDeviceMuted(muted: Boolean, flags: Int) {
        TODO("Not yet implemented")
    }

    override fun setAudioAttributes(
        audioAttributes: androidx.media3.common.AudioAttributes,
        handleAudioFocus: Boolean
    ) {
        TODO("Not yet implemented")
    }

}