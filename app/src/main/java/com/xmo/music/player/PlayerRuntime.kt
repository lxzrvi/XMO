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

internal class PlayerRuntime(
    private val context: Context
) {
    private val mainHandler =
        Handler(Looper.getMainLooper())

    private val mutableState =
        MutableStateFlow(PlaybackState())

    val state: StateFlow<PlaybackState> =
        mutableState.asStateFlow()

    private var controllerFuture:
        ListenableFuture<MediaController>? = null

    private var controller:
        MediaController? = null

    private var songQueue: List<Song> =
        emptyList()

    private var sleepDeadline: Long? =
        null

    private val listener =
        object : Player.Listener {
            override fun onEvents(
                player: Player,
                events: Player.Events
            ) {
                publish(player)
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
                context,
                ComponentName(
                    context,
                    PlaybackService::class.java
                )
            )

        val future =
            MediaController.Builder(
                context,
                token
            ).buildAsync()

        controllerFuture = future

        future.addListener(
            {
                runCatching {
                    future.get()
                }.onSuccess { result ->
                    if (controllerFuture !== future) {
                        MediaController.releaseFuture(future)
                        return@onSuccess
                    }

                    controller = result
                    result.addListener(listener)
                    publish(result)
                }.onFailure {
                    if (controllerFuture === future) {
                        controllerFuture = null
                    }

                    mutableState.value =
                        PlaybackState()
                }
            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun release() {
        mainHandler.removeCallbacks(sleepTick)
        sleepDeadline = null

        controller?.removeListener(listener)

        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }

        controllerFuture = null
        controller = null
        songQueue = emptyList()
        mutableState.value = PlaybackState()
    }

    fun play(
        songs: List<Song>,
        index: Int
    ) {
        val player = controller ?: return
        if (songs.isEmpty()) return

        val safeIndex =
            index.coerceIn(0, songs.lastIndex)

        songQueue = songs.toList()

        player.setMediaItems(
            songQueue.map { it.toMediaItem() },
            safeIndex,
            0L
        )

        player.prepare()
        player.play()
        publish(player)
    }

    fun playNext(song: Song) {
        val player = controller

        if (
            player == null ||
            player.mediaItemCount == 0 ||
            player.currentMediaItemIndex < 0
        ) {
            play(listOf(song), 0)
            return
        }

        val currentIndex =
            player.currentMediaItemIndex

        val existingIndex =
            songQueue.indexOfFirst {
                it.id == song.id
            }

        if (existingIndex >= 0) {
            player.removeMediaItem(existingIndex)

            songQueue =
                songQueue.toMutableList()
                    .also {
                        it.removeAt(existingIndex)
                    }

            val adjustedCurrent =
                if (existingIndex < currentIndex) {
                    currentIndex - 1
                } else {
                    currentIndex
                }

            val target =
                (adjustedCurrent + 1)
                    .coerceAtMost(songQueue.size)

            player.addMediaItem(
                target,
                song.toMediaItem()
            )

            songQueue =
                songQueue.toMutableList()
                    .also {
                        it.add(target, song)
                    }
        } else {
            val target =
                (currentIndex + 1)
                    .coerceAtMost(songQueue.size)

            player.addMediaItem(
                target,
                song.toMediaItem()
            )

            songQueue =
                songQueue.toMutableList()
                    .also {
                        it.add(target, song)
                    }
        }

        publish(player)
    }

    fun playSong(songId: Long) {
        val player = controller ?: return

        val index =
            songQueue.indexOfFirst {
                it.id == songId
            }

        if (index < 0) return

        player.seekTo(index, 0L)
        player.play()
        publish(player)
    }

    fun playQueueIndex(index: Int) {
        val player = controller ?: return

        if (index !in 0 until player.mediaItemCount) {
            return
        }

        player.seekTo(index, 0L)
        player.play()
        publish(player)
    }

    fun togglePlayPause() {
        val player = controller ?: return

        if (player.isPlaying) {
            player.pause()
        } else {
            if (player.playbackState == Player.STATE_ENDED) {
                val index =
                    player.currentMediaItemIndex.coerceAtLeast(0)

                if (index < player.mediaItemCount) {
                    player.seekTo(index, 0L)
                }
            }

            player.play()
        }

        publish(player)
    }

    fun play() {
        val player = controller ?: return
        player.play()
        publish(player)
    }

    fun pause() {
        val player = controller ?: return
        player.pause()
        publish(player)
    }

    fun previous(
        restartThresholdMs: Long
    ) {
        val player = controller ?: return

        if (player.currentPosition > restartThresholdMs) {
            player.seekTo(0L)
        } else if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else {
            player.seekTo(0L)
        }

        publish(player)
    }

    fun previousItem() {
        val player = controller ?: return

        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
            publish(player)
        }
    }

    fun next() {
        val player = controller ?: return

        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
            publish(player)
        }
    }

    fun seekTo(position: Long) {
        val player = controller ?: return
        val duration = player.duration

        val target =
            if (duration > 0L) {
                position.coerceIn(0L, duration)
            } else {
                position.coerceAtLeast(0L)
            }

        player.seekTo(target)
        publish(player)
    }

    fun seekBy(amountMs: Long) {
        val player = controller ?: return

        seekTo(
            player.currentPosition + amountMs
        )
    }

    fun closePlayback() {
        mainHandler.removeCallbacks(sleepTick)
        sleepDeadline = null
        songQueue = emptyList()

        val player = controller

        if (player != null) {
            player.pause()
            player.stop()
            player.clearMediaItems()
            publish(player)
        } else {
            mutableState.value =
                PlaybackState()
        }
    }

    fun setShuffle(enabled: Boolean) {
        val player = controller ?: return
        player.shuffleModeEnabled = enabled
        publish(player)
    }

    fun toggleShuffle() {
        val player = controller ?: return

        player.shuffleModeEnabled =
            !player.shuffleModeEnabled

        publish(player)
    }

    fun setRepeatMode(mode: Int) {
        val player = controller ?: return

        player.repeatMode =
            when (mode) {
                Player.REPEAT_MODE_ONE ->
                    Player.REPEAT_MODE_ONE

                Player.REPEAT_MODE_ALL ->
                    Player.REPEAT_MODE_ALL

                else ->
                    Player.REPEAT_MODE_OFF
            }

        publish(player)
    }

    fun cycleRepeatMode() {
        val player = controller ?: return

        player.repeatMode =
            when (player.repeatMode) {
                Player.REPEAT_MODE_OFF ->
                    Player.REPEAT_MODE_ALL

                Player.REPEAT_MODE_ALL ->
                    Player.REPEAT_MODE_ONE

                else ->
                    Player.REPEAT_MODE_OFF
            }

        publish(player)
    }

    fun setPlaybackParameters(
        speed: Float,
        pitch: Float
    ) {
        val player = controller ?: return

        player.playbackParameters =
            PlaybackParameters(
                speed.coerceIn(.25f, 3f),
                pitch.coerceIn(.5f, 2f)
            )

        publish(player)
    }

    fun setPlaybackSpeed(speed: Float) {
        val player = controller ?: return

        setPlaybackParameters(
            speed,
            player.playbackParameters.pitch
        )
    }

    private val sleepTick =
        object : Runnable {
            override fun run() {
                val deadline =
                    sleepDeadline ?: return

                val remaining =
                    (
                        deadline -
                            SystemClock.elapsedRealtime()
                        ).coerceAtLeast(0L)

                if (remaining <= 0L) {
                    sleepDeadline = null
                    controller?.pause()
                    publish()
                    return
                }

                publish()

                mainHandler.postDelayed(
                    this,
                    minOf(1_000L, remaining)
                )
            }
        }

    fun setSleepTimer(durationMs: Long) {
        mainHandler.removeCallbacks(sleepTick)

        if (durationMs <= 0L) {
            sleepDeadline = null
            publish()
            return
        }

        sleepDeadline =
            SystemClock.elapsedRealtime() +
                durationMs

        mainHandler.post(sleepTick)
    }

    fun cancelSleepTimer() {
        sleepDeadline = null
        mainHandler.removeCallbacks(sleepTick)
        publish()
    }

    fun sleepTimerRemaining(): Long {
        val deadline =
            sleepDeadline ?: return 0L

        return (
            deadline -
                SystemClock.elapsedRealtime()
            ).coerceAtLeast(0L)
    }

    fun publish() {
        controller?.let(::publish)
    }

    fun currentSong(): Song? {
        val id =
            mutableState.value.currentSongId
                ?: return null

        return songQueue.firstOrNull {
            it.id == id
        }
    }

    fun queue(): List<Song> =
        songQueue

    fun queueSong(index: Int): Song? =
        songQueue.getOrNull(index)

    private fun publish(player: Player) {
        val item = player.currentMediaItem
        val metadata = item?.mediaMetadata

        val duration =
            player.duration.takeIf {
                it > 0L
            } ?: 0L

        val position =
            player.currentPosition
                .coerceAtLeast(0L)
                .let {
                    if (duration > 0L) {
                        it.coerceAtMost(duration)
                    } else {
                        it
                    }
                }

        val parameters =
            player.playbackParameters

        mutableState.value =
            PlaybackState(
                connected = true,
                currentSongId =
                    item?.mediaId?.toLongOrNull(),
                title =
                    metadata?.title?.toString().orEmpty(),
                artist =
                    metadata?.artist?.toString().orEmpty(),
                album =
                    metadata?.albumTitle?.toString().orEmpty(),
                artworkUri =
                    metadata?.artworkUri?.toString(),
                isPlaying =
                    player.isPlaying,
                duration =
                    duration,
                position =
                    position,
                currentIndex =
                    if (item != null) {
                        player.currentMediaItemIndex
                    } else {
                        -1
                    },
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
}

private fun Song.toMediaItem(): MediaItem {
    val metadata =
        MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(artist)
            .setAlbumTitle(album)
            .setAlbumArtist(albumArtist)
            .setArtworkUri(artwork)
            .build()

    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setMediaMetadata(metadata)
        .build()
}
