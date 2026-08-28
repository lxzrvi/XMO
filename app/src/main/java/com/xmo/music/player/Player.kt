package com.xmo.music.player

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.xmo.music.data.Song
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/*
 * State exposed to Compose.
 *
 * ExoPlayer itself remains inside PlaybackService.
 */
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
    val hasNext: Boolean = false
)

class XmoPlayer(
    context: Context
) {

    private val appContext =
        context.applicationContext

    private val _state =
        MutableStateFlow(
            PlaybackState()
        )

    val state: StateFlow<PlaybackState> =
        _state.asStateFlow()

    /*
     * MediaController is UI/application-side proxy
     * to the MediaSessionService.
     */
    private var controller:
        MediaController? = null

    private var controllerFuture:
        ListenableFuture<MediaController>? = null

    /*
     * Real Song queue retained for XMO UI operations.
     *
     * Actual playback queue also exists in Media3 as MediaItems.
     */
    private var songQueue:
        List<Song> = emptyList()

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
     * ---------------------------------------------------------
     * CONNECT
     * ---------------------------------------------------------
     */
    fun connect() {
        if (
            controller != null ||
            controllerFuture != null
        ) {
            return
        }

        val sessionToken =
            SessionToken(
                appContext,

                ComponentName(
                    appContext,
                    PlaybackService::class.java
                )
            )

        val future =
            MediaController
                .Builder(
                    appContext,
                    sessionToken
                )
                .buildAsync()

        controllerFuture =
            future

        future.addListener(
            {
                /*
                 * This callback runs on main executor.
                 */
                runCatching {
                    future.get()
                }
                    .onSuccess {
                            mediaController ->

                        /*
                         * XmoPlayer could have been released while
                         * asynchronous connection was happening.
                         */
                        if (
                            controllerFuture !==
                            future
                        ) {
                            mediaController
                                .release()

                            return@onSuccess
                        }

                        controllerFuture =
                            null

                        controller =
                            mediaController

                        mediaController
                            .addListener(
                                listener
                            )

                        publishState(
                            mediaController
                        )
                    }
                    .onFailure {
                        if (
                            controllerFuture ===
                            future
                        ) {
                            controllerFuture =
                                null
                        }

                        _state.value =
                            PlaybackState(
                                connected =
                                    false
                            )
                    }
            },

            ContextCompat
                .getMainExecutor(
                    appContext
                )
        )
    }

    /*
     * ---------------------------------------------------------
     * PLAY QUEUE
     * ---------------------------------------------------------
     *
     * This is the main entry point when a SongTile is tapped.
     */
    fun play(
        songs: List<Song>,
        index: Int
    ) {
        val mediaController =
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
            songs

        val mediaItems =
            songs.map {
                it.toMediaItem()
            }

        mediaController
            .setMediaItems(
                mediaItems,
                safeIndex,
                0L
            )

        mediaController
            .prepare()

        mediaController
            .play()

        publishState(
            mediaController
        )
    }

    /*
     * Convenience API.
     */
    fun playSong(
        song: Song,
        queue: List<Song>
    ) {
        val index =
            queue.indexOfFirst {
                it.id ==
                    song.id
            }

        if (
            index >= 0
        ) {
            play(
                queue,
                index
            )
        }
    }

    /*
     * ---------------------------------------------------------
     * PLAY / PAUSE
     * ---------------------------------------------------------
     */
    fun play() {
        controller
            ?.play()
    }

    fun pause() {
        controller
            ?.pause()
    }

    fun togglePlayPause() {
        val mediaController =
            controller
                ?: return

        if (
            mediaController
                .isPlaying
        ) {
            mediaController
                .pause()
        } else {
            /*
             * If playback reached the end, play() alone may leave
             * it at the final position. Seek back when appropriate.
             */
            if (
                mediaController
                    .playbackState ==
                Player.STATE_ENDED
            ) {
                mediaController
                    .seekTo(
                        mediaController
                            .currentMediaItemIndex
                            .coerceAtLeast(
                                0
                            ),
                        0L
                    )
            }

            mediaController
                .play()
        }
    }

    /*
     * ---------------------------------------------------------
     * PREVIOUS / NEXT
     * ---------------------------------------------------------
     */
    fun previous() {
        val mediaController =
            controller
                ?: return

        /*
         * Common music-player behaviour:
         * if song has played >3 sec, Previous restarts it.
         */
        if (
            mediaController
                .currentPosition >
            3_000L
        ) {
            mediaController
                .seekTo(
                    0L
                )

            return
        }

        if (
            mediaController
                .hasPreviousMediaItem()
        ) {
            mediaController
                .seekToPreviousMediaItem()
        } else {
            mediaController
                .seekTo(
                    0L
                )
        }
    }

    fun next() {
        val mediaController =
            controller
                ?: return

        if (
            mediaController
                .hasNextMediaItem()
        ) {
            mediaController
                .seekToNextMediaItem()
        }
    }

    /*
     * ---------------------------------------------------------
     * SEEK
     * ---------------------------------------------------------
     */
    fun seekTo(
        positionMs: Long
    ) {
        val mediaController =
            controller
                ?: return

        val duration =
            mediaController
                .duration

        val target =
            if (
                duration > 0L
            ) {
                positionMs.coerceIn(
                    0L,
                    duration
                )
            } else {
                positionMs.coerceAtLeast(
                    0L
                )
            }

        mediaController
            .seekTo(
                target
            )
    }

    fun seekBy(
        amountMs: Long
    ) {
        val mediaController =
            controller
                ?: return

        seekTo(
            mediaController
                .currentPosition +
                amountMs
        )
    }

    /*
     * ---------------------------------------------------------
     * QUEUE
     * ---------------------------------------------------------
     */
    fun queue():
        List<Song> {
        return songQueue
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

    /*
     * ---------------------------------------------------------
     * POSITION REFRESH
     * ---------------------------------------------------------
     *
     * Player.Listener doesn't emit every progress millisecond.
     * NowPlaying can call this from a light coroutine every
     * ~250ms while visible.
     *
     * No fake timer:
     * value always comes directly from MediaController.
     */
    fun refreshPosition() {
        controller
            ?.let {
                publishState(
                    it
                )
            }
    }

    /*
     * ---------------------------------------------------------
     * RELEASE CONTROLLER
     * ---------------------------------------------------------
     *
     * Releasing controller does NOT destroy PlaybackService.
     * Music can continue through MediaSession/notification.
     */
    fun release() {
        val mediaController =
            controller

        if (
            mediaController != null
        ) {
            mediaController
                .removeListener(
                    listener
                )

            mediaController
                .release()
        }

        controller =
            null

        controllerFuture
            ?.let { future ->
                MediaController
                    .releaseFuture(
                        future
                    )
            }

        controllerFuture =
            null

        songQueue =
            emptyList()

        _state.value =
            PlaybackState()
    }

    /*
     * ---------------------------------------------------------
     * STATE
     * ---------------------------------------------------------
     */
    private fun publishState(
        player: Player
    ) {
        val mediaItem =
            player
                .currentMediaItem

        val metadata =
            mediaItem
                ?.mediaMetadata

        val duration =
            player
                .duration
                .takeIf {
                    it > 0L
                }
                ?: 0L

        _state.value =
            PlaybackState(
                connected =
                    true,

                currentSongId =
                    mediaItem
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
                    player
                        .isPlaying,

                duration =
                    duration,

                position =
                    player
                        .currentPosition
                        .coerceAtLeast(
                            0L
                        ),

                currentIndex =
                    player
                        .currentMediaItemIndex,

                hasPrevious =
                    player
                        .hasPreviousMediaItem(),

                hasNext =
                    player
                        .hasNextMediaItem()
            )
    }
}

/*
 * =============================================================
 * SONG -> MEDIA ITEM
 * =============================================================
 */

private fun Song.toMediaItem():
    MediaItem {

    val metadata =
        MediaMetadata
            .Builder()
            .setTitle(
                title
            )
            .setArtist(
                artist
            )
            .setAlbumTitle(
                album
            )
            .setArtworkUri(
                artwork
            )
            .build()

    return MediaItem
        .Builder()

        /*
         * MediaStore ID becomes stable Media3 ID.
         */
        .setMediaId(
            id.toString()
        )

        /*
         * content://media/... URI
         */
        .setUri(
            uri
        )

        .setMediaMetadata(
            metadata
        )

        .build()
}
