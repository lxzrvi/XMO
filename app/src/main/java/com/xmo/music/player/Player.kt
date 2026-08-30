package com.xmo.music.player

import android.content.Context
import androidx.media3.common.Player
import com.xmo.music.data.Song
import kotlinx.coroutines.flow.StateFlow

data class PlaybackState(
    val connected: Boolean = false,
    val currentSongId: Long? = null,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val artworkUri: String? = null,
    val isPlaying: Boolean = false,
    val duration: Long = 0L,
    val position: Long = 0L,
    val currentIndex: Int = -1,
    val hasPrevious: Boolean = false,
    val hasNext: Boolean = false,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int =
        Player.REPEAT_MODE_OFF,
    val playbackSpeed: Float = 1f,
    val playbackPitch: Float = 1f,
    val sleepTimerRemainingMs: Long = 0L
)

class XmoPlayer(
    context: Context
) {
    private val runtime =
        PlayerRuntime(
            context.applicationContext
        )

    val state: StateFlow<PlaybackState>
        get() =
            runtime.state

    /*
     * =========================================================
     * CONNECTION
     * =========================================================
     */

    fun connect() {
        runtime.connect()
    }

    fun release() {
        runtime.release()
    }

    /*
     * =========================================================
     * PLAYBACK
     * =========================================================
     */

    fun play(
        songs: List<Song>,
        index: Int
    ) {
        runtime.play(
            songs,
            index
        )
    }

    fun playSong(
        songId: Long
    ) {
        runtime.playSong(
            songId
        )
    }

    fun playQueueIndex(
        index: Int
    ) {
        runtime.playQueueIndex(
            index
        )
    }

    fun togglePlayPause() {
        runtime.togglePlayPause()
    }

    fun play() {
        runtime.play()
    }

    fun pause() {
        runtime.pause()
    }

    /*
     * Music-player previous behavior:
     *
     * > 3 seconds -> restart
     * near start   -> actual previous item
     */
    fun previous() {
        runtime.previous(
            restartThresholdMs =
                3_000L
        )
    }

    /*
     * Always asks Media3 for the actual previous queue item.
     */
    fun previousItem() {
        runtime.previousItem()
    }

    fun next() {
        runtime.next()
    }

    fun seekTo(
        position: Long
    ) {
        runtime.seekTo(
            position
        )
    }

    fun seekBy(
        amountMs: Long
    ) {
        runtime.seekBy(
            amountMs
        )
    }

    /*
     * =========================================================
     * REAL CLOSE
     * =========================================================
     *
     * Used by MiniPlayer X / downward dismiss.
     *
     * This is different from merely hiding the UI:
     * Media3 playback is stopped, queue is cleared and
     * currentSongId becomes null.
     */
    fun closePlayback() {
        runtime.closePlayback()
    }

    /*
     * =========================================================
     * SHUFFLE / REPEAT
     * =========================================================
     */

    fun setShuffle(
        enabled: Boolean
    ) {
        runtime.setShuffle(
            enabled
        )
    }

    fun toggleShuffle() {
        runtime.toggleShuffle()
    }

    fun setRepeatMode(
        mode: Int
    ) {
        runtime.setRepeatMode(
            mode
        )
    }

    fun cycleRepeatMode() {
        runtime.cycleRepeatMode()
    }

    /*
     * =========================================================
     * SPEED / PITCH
     * =========================================================
     */

    fun setPlaybackParameters(
        speed: Float,
        pitch: Float
    ) {
        runtime.setPlaybackParameters(
            speed,
            pitch
        )
    }

    fun setPlaybackSpeed(
        speed: Float
    ) {
        runtime.setPlaybackSpeed(
            speed
        )
    }

    /*
     * =========================================================
     * SLEEP TIMER
     * =========================================================
     */

    fun setSleepTimer(
        durationMs: Long
    ) {
        runtime.setSleepTimer(
            durationMs
        )
    }

    fun cancelSleepTimer() {
        runtime.cancelSleepTimer()
    }

    fun sleepTimerRemaining(): Long =
        runtime.sleepTimerRemaining()

    /*
     * =========================================================
     * STATE / QUEUE
     * =========================================================
     */

    fun refreshPosition() {
        runtime.publish()
    }

    fun currentSong(): Song? =
        runtime.currentSong()

    fun queue(): List<Song> =
        runtime.queue()

    fun queueSong(
        index: Int
    ): Song? =
        runtime.queueSong(
            index
        )
}
