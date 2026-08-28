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

    private var controllerFuture:
        ListenableFuture<MediaController>? =
        null

    private var controller:
        MediaController? = null

    private var songQueue:
        List<Song> = emptyList()

    private val listener =
        object : Player.Listener {
            override fun onEvents(
                player: Player,
                events: Player.Events
            ) {
                publishState(player)
            }
        }

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

        controllerFuture = future

        future.addListener(
            {
                runCatching {
                    future.get()
                }.onSuccess { result ->

                    if (
                        controllerFuture !== future
                    ) {
                        return@onSuccess
                    }

                    controller = result

                    result.addListener(
                        listener
                    )

                    publishState(result)

                }.onFailure {
                    if (
                        controllerFuture === future
                    ) {
                        controllerFuture = null
                    }

                    _state.value =
                        PlaybackState()
                }
            },
            ContextCompat.getMainExecutor(
                appContext
            )
        )
    }

    fun play(
        songs: List<Song>,
        index: Int
    ) {
        val player =
            controller ?: return

        if (songs.isEmpty()) {
            return
        }

        val safeIndex =
            index.coerceIn(
                0,
                songs.lastIndex
            )

        songQueue = songs

        player.setMediaItems(
            songs.map {
                it.toMediaItem()
            },
            safeIndex,
            0L
        )

        player.prepare()
        player.play()

        publishState(player)
    }

    fun togglePlayPause() {
        val player =
            controller ?: return

        if (player.isPlaying) {
            player.pause()
        } else {
            if (
                player.playbackState ==
                Player.STATE_ENDED
            ) {
                player.seekTo(
                    player.currentMediaItemIndex
                        .coerceAtLeast(0),
                    0L
                )
            }

            player.play()
        }
    }

    fun previous() {
        val player =
            controller ?: return

        if (
            player.currentPosition >
            3_000L
        ) {
            player.seekTo(0L)
        } else if (
            player.hasPreviousMediaItem()
        ) {
            player.seekToPreviousMediaItem()
        } else {
            player.seekTo(0L)
        }
    }

    fun next() {
        controller
            ?.takeIf {
                it.hasNextMediaItem()
            }
            ?.seekToNextMediaItem()
    }

    fun seekTo(
        position: Long
    ) {
        val player =
            controller ?: return

        val duration =
            player.duration

        val target =
            if (duration > 0) {
                position.coerceIn(
                    0L,
                    duration
                )
            } else {
                position.coerceAtLeast(0L)
            }

        player.seekTo(target)
    }

    fun refreshPosition() {
        controller?.let {
            publishState(it)
        }
    }

    fun currentSong(): Song? {
        val id =
            _state.value.currentSongId
                ?: return null

        return songQueue.firstOrNull {
            it.id == id
        }
    }

    fun queue(): List<Song> =
        songQueue

    fun release() {
        controller?.removeListener(
            listener
        )

        /*
         * buildAsync lifecycle is owned through this Future.
         */
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }

        controllerFuture = null
        controller = null

        _state.value =
            PlaybackState()
    }

    private fun publishState(
        player: Player
    ) {
        val item =
            player.currentMediaItem

        val metadata =
            item?.mediaMetadata

        _state.value =
            PlaybackState(
                connected = true,

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
                    player.duration
                        .takeIf {
                            it > 0
                        }
                        ?: 0L,

                position =
                    player.currentPosition
                        .coerceAtLeast(0L),

                currentIndex =
                    player.currentMediaItemIndex,

                hasPrevious =
                    player.hasPreviousMediaItem(),

                hasNext =
                    player.hasNextMediaItem()
            )
    }
}

private fun Song.toMediaItem():
    MediaItem {

    val metadata =
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setArtworkUri(artwork)
            .build()

    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(metadata)
        .build()
}
