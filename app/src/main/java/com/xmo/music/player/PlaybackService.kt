package com.xmo.music.player

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.xmo.music.MainActivity

class PlaybackService : MediaSessionService() {

    private var mediaSession:
        MediaSession? =
        null

    private var exoPlayer:
        ExoPlayer? =
        null

    override fun onCreate() {
        super.onCreate()

        val audioAttributes =
            AudioAttributes.Builder()
                .setUsage(
                    C.USAGE_MEDIA
                )
                .setContentType(
                    C.AUDIO_CONTENT_TYPE_MUSIC
                )
                .build()

        val player =
            ExoPlayer.Builder(
                this
            )
                .build()
                .apply {
                    /*
                     * Media playback owns audio focus while playing.
                     */
                    setAudioAttributes(
                        audioAttributes,
                        true
                    )

                    /*
                     * Headphones/Bluetooth disconnect:
                     * playback pauses instead of continuing through
                     * device speakers.
                     */
                    setHandleAudioBecomingNoisy(
                        true
                    )

                    /*
                     * Real Media3 defaults.
                     */
                    repeatMode =
                        Player.REPEAT_MODE_OFF

                    shuffleModeEnabled =
                        false
                }

        exoPlayer =
            player

        /*
         * Notification / lock-screen session activity.
         *
         * Tapping the Media3 notification reopens XMO rather than
         * creating an unrelated activity stack.
         */
        val sessionActivity =
            PendingIntent.getActivity(
                this,
                0,
                Intent(
                    this,
                    MainActivity::class.java
                ).apply {
                    flags =
                        Intent.FLAG_ACTIVITY_SINGLE_TOP or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
                },
                PendingIntent.FLAG_IMMUTABLE or
                    PendingIntent.FLAG_UPDATE_CURRENT
            )

        mediaSession =
            MediaSession.Builder(
                this,
                player
            )
                .setSessionActivity(
                    sessionActivity
                )
                .build()
    }

    override fun onGetSession(
        controllerInfo:
            MediaSession.ControllerInfo
    ): MediaSession? =
        mediaSession

    /*
     * MediaSessionService can remain alive while playback should
     * continue in background. If Android asks the service to
     * remove its foreground notification, playback is not faked
     * or recreated in Compose.
     */
    override fun onTaskRemoved(
        rootIntent: Intent?
    ) {
        val player =
            exoPlayer

        /*
         * If nothing is playing and the player is idle/ended,
         * allow Media3 to release the service naturally.
         */
        if (
            player != null &&
            !player.playWhenReady &&
            (
                player.playbackState ==
                    Player.STATE_IDLE ||
                    player.playbackState ==
                    Player.STATE_ENDED
                )
        ) {
            stopSelf()
        }

        super.onTaskRemoved(
            rootIntent
        )
    }

    override fun onDestroy() {
        /*
         * MediaSession must be detached before releasing the
         * ExoPlayer it owns.
         */
        mediaSession?.release()

        mediaSession =
            null

        exoPlayer?.release()

        exoPlayer =
            null

        super.onDestroy()
    }
}
