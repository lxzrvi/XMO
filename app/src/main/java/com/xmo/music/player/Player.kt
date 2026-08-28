package com.xmo.music.player

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.xmo.music.data.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    /*
     * Remaining real sleep-timer time.
     * 0 = no active timer.
     */
    val sleepTimerRemainingMs: Long = 0L
)

class XmoPlayer(
    context: Context
) {
    private val appContext =
        context.applicationContext

    private val mainHandler =
        Handler(
            Looper.getMainLooper()
        )

    private val _state =
        MutableStateFlow(
            PlaybackState()
        )

    val state: StateFlow<PlaybackState> =
        _state.asStateFlow()

    private var controllerFuture:
        ListenableFuture<MediaController>? =
        null

    private var controller:
        MediaController? =
        null

    /*
     * Real Song objects corresponding to the Media3 queue.
     */
    private var songQueue:
        List<Song> =
        emptyList()

    /*
     * Sleep timer uses monotonic elapsed time so wall-clock
     * changes do not alter the timer.
     */
    private var sleepDeadline:
        Long? =
        null

    private val sleepTick =
        object : Runnable {
            override fun run() {
                val deadline =
                    sleepDeadline
                        ?: return

                val remaining =
                    (
                        deadline -
                            SystemClock.elapsedRealtime()
                        )
                        .coerceAtLeast(
                            0L
                        )

                if (
                    remaining <= 0L
                ) {
                    sleepDeadline =
                        null

                    controller?.pause()

                    publishState()

                    return
                }

                publishState()

                mainHandler.postDelayed(
                    this,
                    minOf(
                        1_000L,
                        remaining
                    )
                )
            }
        }

    private val listener =
        object : Player.Listener {

            override fun onEvents(
                player: Player,
                events: Player.Events
            ) {
                publishState(
                    player
                )
            }
        }

    /*
     * =========================================================
     * CONNECTION
     * =========================================================
     */

    fun connect() {
        if (
            controller != null ||
            controllerFuture != null
        ) {
            return
        }

        val token =
            SessionToken(
                appContext,

                ComponentName(
                    appContext,
                    PlaybackService::class.java
                )
            )

        val future =
            MediaController.Builder(
                appContext,
                token
            )
                .buildAsync()

        controllerFuture =
            future

        future.addListener(
            {
                runCatching {
                    future.get()
                }.onSuccess { result ->

                    if (
                        controllerFuture !==
                        future
                    ) {
                        MediaController
                            .releaseFuture(
                                future
                            )

                        return@onSuccess
                    }

                    controller =
                        result

                    result.addListener(
                        listener
                    )

                    rebuildQueueFromController(
                        result
                    )

                    publishState(
                        result
                    )

                }.onFailure {

                    if (
                        controllerFuture ===
                        future
                    ) {
                        controllerFuture =
                            null
                    }

                    _state.value =
                        PlaybackState()
                }
            },

            ContextCompat
                .getMainExecutor(
                    appContext
                )
        )
    }

    /*
     * =========================================================
     * QUEUE / PLAYBACK
     * =========================================================
     */

    fun play(
        songs: List<Song>,
        index: Int
    ) {
        val player =
            controller
                ?: return

        if (
            songs.isEmpty()
        ) {
            return
        }

        val safeIndex =
            index.coerceIn(
                0,
                songs.lastIndex
            )

        songQueue =
            songs.toList()

        player.setMediaItems(
            songQueue.map {
                it.toMediaItem()
            },
            safeIndex,
            0L
        )

        player.prepare()
        player.play()

        publishState(
            player
        )
    }

    fun playSong(
        songId: Long
    ) {
        val player =
            controller
                ?: return

        val index =
            songQueue.indexOfFirst {
                it.id ==
                    songId
            }

        if (
            index < 0
        ) {
            return
        }

        player.seekTo(
            index,
            0L
        )

        player.play()

        publishState(
            player
        )
    }

    fun playQueueIndex(
        index: Int
    ) {
        val player =
            controller
                ?: return

        if (
            index !in
            0 until
                player.mediaItemCount
        ) {
            return
        }

        player.seekTo(
            index,
            0L
        )

        player.play()

        publishState(
            player
        )
    }

    fun togglePlayPause() {
        val player =
            controller
                ?: return

        if (
            player.isPlaying
        ) {
            player.pause()
        } else {
            if (
                player.playbackState ==
                Player.STATE_ENDED
            ) {
                val index =
                    player
                        .currentMediaItemIndex
                        .coerceAtLeast(
                            0
                        )

                if (
                    index <
                    player.mediaItemCount
                ) {
                    player.seekTo(
                        index,
                        0L
                    )
                }
            }

            player.play()
        }

        publishState(
            player
        )
    }

    fun play() {
        val player =
            controller
                ?: return

        player.play()

        publishState(
            player
        )
    }

    fun pause() {
        val player =
            controller
                ?: return

        player.pause()

        publishState(
            player
        )
    }

    fun previous() {
        val player =
            controller
                ?: return

        /*
         * Music-player convention:
         *
         * after 3 seconds -> restart current song
         * near beginning -> actual previous item
         */
        if (
            player.currentPosition >
            3_000L
        ) {
            player.seekTo(
                0L
            )
        } else if (
            player.hasPreviousMediaItem()
        ) {
            player.seekToPreviousMediaItem()
        } else {
            player.seekTo(
                0L
            )
        }

        publishState(
            player
        )
    }

    fun previousItem() {
        val player =
            controller
                ?: return

        if (
            player.hasPreviousMediaItem()
        ) {
            player.seekToPreviousMediaItem()

            publishState(
                player
            )
        }
    }

    fun next() {
        val player =
            controller
                ?: return

        if (
            player.hasNextMediaItem()
        ) {
            player.seekToNextMediaItem()

            publishState(
                player
            )
        }
    }

    fun seekTo(
        position: Long
    ) {
        val player =
            controller
                ?: return

        val duration =
            player.duration

        val target =
            if (
                duration > 0L
            ) {
                position.coerceIn(
                    0L,
                    duration
                )
            } else {
                position.coerceAtLeast(
                    0L
                )
            }

        player.seekTo(
            target
        )

        publishState(
            player
        )
    }

    fun seekBy(
        amountMs: Long
    ) {
        val player =
            controller
                ?: return

        seekTo(
            player.currentPosition +
                amountMs
        )
    }

    /*
     * =========================================================
     * SHUFFLE
     * =========================================================
     */

    fun setShuffle(
        enabled: Boolean
    ) {
        val player =
            controller
                ?: return

        player.shuffleModeEnabled =
            enabled

        publishState(
            player
        )
    }

    fun toggleShuffle() {
        val player =
            controller
                ?: return

        player.shuffleModeEnabled =
            !player.shuffleModeEnabled

        publishState(
            player
        )
    }

    /*
     * =========================================================
     * REPEAT
     * =========================================================
     */

    fun setRepeatMode(
        mode: Int
    ) {
        val player =
            controller
                ?: return

        player.repeatMode =
            when (mode) {
                Player.REPEAT_MODE_ONE ->
                    Player.REPEAT_MODE_ONE

                Player.REPEAT_MODE_ALL ->
                    Player.REPEAT_MODE_ALL

                else ->
                    Player.REPEAT_MODE_OFF
            }

        publishState(
            player
        )
    }

    fun cycleRepeatMode() {
        val player =
            controller
                ?: return

        player.repeatMode =
            when (
                player.repeatMode
            ) {
                Player.REPEAT_MODE_OFF ->
                    Player.REPEAT_MODE_ALL

                Player.REPEAT_MODE_ALL ->
                    Player.REPEAT_MODE_ONE

                else ->
                    Player.REPEAT_MODE_OFF
            }

        publishState(
            player
        )
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
        val player =
            controller
                ?: return

        val safeSpeed =
            speed.coerceIn(
                .25f,
                3f
            )

        val safePitch =
            pitch.coerceIn(
                .5f,
                2f
            )

        player.playbackParameters =
            PlaybackParameters(
                safeSpeed,
                safePitch
            )

        publishState(
            player
        )
    }

    fun setPlaybackSpeed(
        speed: Float
    ) {
        val player =
            controller
                ?: return

        setPlaybackParameters(
            speed =
                speed,

            pitch =
                player
                    .playbackParameters
                    .pitch
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
        mainHandler.removeCallbacks(
            sleepTick
        )

        if (
            durationMs <= 0L
        ) {
            sleepDeadline =
                null

            publishState()

            return
        }

        sleepDeadline =
            SystemClock
                .elapsedRealtime() +
                durationMs

        mainHandler.post(
            sleepTick
        )
    }

    fun cancelSleepTimer() {
        sleepDeadline =
            null

        mainHandler.removeCallbacks(
            sleepTick
        )

        publishState()
    }

    fun sleepTimerRemaining():
        Long {
        val deadline =
            sleepDeadline
                ?: return 0L

        return (
            deadline -
                SystemClock.elapsedRealtime()
            )
            .coerceAtLeast(
                0L
            )
    }

    /*
     * =========================================================
     * STATE / QUEUE
     * =========================================================
     */

    fun refreshPosition() {
        publishState()
    }

    fun currentSong():
        Song? {
        val id =
            _state
                .value
                .currentSongId
                ?: return null

        return songQueue
            .firstOrNull {
                it.id ==
                    id
            }
    }

    fun queue():
        List<Song> =
        songQueue

    fun queueSong(
        index: Int
    ): Song? =
        songQueue
            .getOrNull(
                index
            )

    fun release() {
        mainHandler.removeCallbacks(
            sleepTick
        )

        sleepDeadline =
            null

        controller?.removeListener(
            listener
        )

        controllerFuture?.let {
            MediaController
                .releaseFuture(
                    it
                )
        }

        controllerFuture =
            null

        controller =
            null

        songQueue =
            emptyList()

        _state.value =
            PlaybackState()
    }

    /*
     * =========================================================
     * PUBLISH
     * =========================================================
     */

    private fun publishState() {
        controller?.let {
            publishState(
                it
            )
        }
    }

    private fun publishState(
        player: Player
    ) {
        val item =
            player.currentMediaItem

        val metadata =
            item?.mediaMetadata

        val duration =
            player.duration
                .takeIf {
                    it > 0L
                }
                ?: 0L

        val position =
            player.currentPosition
                .coerceAtLeast(
                    0L
                )
                .let {
                    if (
                        duration >
                        0L
                    ) {
                        it.coerceAtMost(
                            duration
                        )
                    } else {
                        it
                    }
                }

        val parameters =
            player.playbackParameters

        _state.value =
            PlaybackState(
                connected =
                    true,

                currentSongId =
                    item
                        ?.mediaId
                        ?.toLongOrNull(),

                title =
                    metadata
                        ?.title
                        ?.toString()
                        .orEmpty(),

                artist =
                    metadata
                        ?.artist
                        ?.toString()
                        .orEmpty(),

                album =
                    metadata
                        ?.albumTitle
                        ?.toString()
                        .orEmpty(),

                artworkUri =
                    metadata
                        ?.artworkUri
                        ?.toString(),

                isPlaying =
                    player.isPlaying,

                duration =
                    duration,

                position =
                    position,

                currentIndex =
                    player.currentMediaItemIndex,

                hasPrevious =
                    player.hasPreviousMediaItem(),

                hasNext =
                    player.hasNextMediaItem(),

                shuffleEnabled =
                    player.shuffleModeEnabled,

                repeatMode =
                    player.repeatMode,

                playbackSpeed =
                    parameters.speed,

                playbackPitch =
                    parameters.pitch,

                sleepTimerRemainingMs =
                    sleepTimerRemaining()
            )
    }

    /*
     * If MediaController reconnects to a session that already has
     * items, retain any Song objects we can identify.
     *
     * App-level library remains the authoritative full metadata
     * source.
     */
    private fun rebuildQueueFromController(
        player: Player
    ) {
        if (
            player.mediaItemCount <= 0 ||
            songQueue.isNotEmpty()
        ) {
            return
        }

        /*
         * No fabricated Song objects are created here because
         * MediaItem metadata alone cannot reconstruct the complete
         * local Song model safely.
         */
    }
}

/*
 * =============================================================
 * SONG -> MEDIA3
 * =============================================================
 */

private fun Song.toMediaItem():
    MediaItem {

    val metadata =
        MediaMetadata.Builder()
            .setTitle(
                title
            )
            .setArtist(
                artist
            )
            .setAlbumTitle(
                album
            )
            .setAlbumArtist(
                albumArtist
            )
            .setArtworkUri(
                artwork
            )
            .build()

    return MediaItem.Builder()
        .setMediaId(
            id.toString()
        )
        .setUri(
            uri
        )
        .setMediaMetadata(
            metadata
        )
        .build()
}
