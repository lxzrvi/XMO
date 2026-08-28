package com.xmo.music.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import com.xmo.music.XmoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

object Artwork {

    private val fallback =
        Color(
            0xFF35353A
        )

    /*
     * =========================================================
     * MEMORY CACHE
     * =========================================================
     *
     * Palette extraction is artwork-based, not Song-based.
     * Multiple songs sharing one album therefore share one result.
     */

    private val colors =
        ConcurrentHashMap<String, Color>()

    /*
     * One decode at a time per URI.
     *
     * If twelve visible cards share the same album, only the first
     * coroutine performs bitmap decode/Palette extraction.
     */
    private val locks =
        ConcurrentHashMap<String, Mutex>()

    suspend fun color(
        context: Context,
        uri: Uri?
    ): Color {
        if (
            uri ==
            null
        ) {
            return fallback
        }

        val key =
            uri.toString()

        colors[key]
            ?.let {
                return it
            }

        val mutex =
            locks.getOrPut(
                key
            ) {
                Mutex()
            }

        return try {
            mutex.withLock {
                colors[key]
                    ?.let {
                        return@withLock it
                    }

                val extracted =
                    withContext(
                        Dispatchers.IO
                    ) {
                        extract(
                            context,
                            uri
                        )
                    }

                colors[key] =
                    extracted

                extracted
            }
        } finally {
            /*
             * Remove only our own lock. A later request can safely
             * create another one, but will hit the color cache first.
             */
            locks.remove(
                key,
                mutex
            )
        }
    }

    /*
     * Synchronous lookup for Compose hot paths.
     */
    fun cached(
        uri: Uri?
    ): Color? {
        if (
            uri ==
            null
        ) {
            return fallback
        }

        return colors[
            uri.toString()
        ]
    }

    /*
     * Optional cache invalidation for a changed local artwork URI.
     */
    fun invalidate(
        uri: Uri?
    ) {
        if (
            uri ==
            null
        ) {
            return
        }

        colors.remove(
            uri.toString()
        )
    }

    fun clear() {
        colors.clear()
        locks.clear()
    }

    /*
     * =========================================================
     * THEME-AWARE GRADIENT DESTINATION
     * =========================================================
     */

    fun end(
        color: Color,
        theme: XmoTheme
    ): Color =
        when (
            theme
        ) {
            XmoTheme.Dark ->
                blend(
                    color,
                    Color(
                        0xFF121216
                    ),
                    .72f
                )

            XmoTheme.Light ->
                blend(
                    color,
                    Color(
                        0xFFF7F8FA
                    ),
                    .74f
                )

            XmoTheme.Amoled ->
                blend(
                    color,
                    Color.Black,
                    .80f
                )
        }

    /*
     * Darker companion used by artwork-driven large backgrounds.
     */
    fun deep(
        color: Color,
        theme: XmoTheme
    ): Color =
        when (
            theme
        ) {
            XmoTheme.Light ->
                blend(
                    color,
                    Color(
                        0xFFE6E8EC
                    ),
                    .60f
                )

            XmoTheme.Dark ->
                blend(
                    color,
                    Color(
                        0xFF08090C
                    ),
                    .76f
                )

            XmoTheme.Amoled ->
                blend(
                    color,
                    Color.Black,
                    .88f
                )
        }

    /*
     * =========================================================
     * EXTRACTION
     * =========================================================
     */

    private fun extract(
        context: Context,
        uri: Uri
    ): Color {
        val bitmap =
            runCatching {
                context
                    .contentResolver
                    .openInputStream(
                        uri
                    )
                    ?.use { stream ->

                        BitmapFactory
                            .decodeStream(
                                stream
                            )
                    }
            }
                .getOrNull()
                ?: return fallback

        return try {
            val palette =
                Palette
                    .from(
                        bitmap
                    )
                    /*
                     * Palette does not need every source pixel.
                     * Resizing analysis keeps large embedded covers
                     * from creating unnecessary CPU work.
                     */
                    .resizeBitmapArea(
                        112 *
                            112
                    )
                    .maximumColorCount(
                        16
                    )
                    .generate()

            val rgb =
                palette
                    .vibrantSwatch
                    ?.rgb
                    ?: palette
                        .lightVibrantSwatch
                        ?.rgb
                    ?: palette
                        .darkVibrantSwatch
                        ?.rgb
                    ?: palette
                        .dominantSwatch
                        ?.rgb

            rgb?.let {
                Color(
                    it
                )
            } ?: fallback
        } catch (
            _: Throwable
        ) {
            fallback
        } finally {
            if (
                !bitmap.isRecycled
            ) {
                bitmap.recycle()
            }
        }
    }

    private fun blend(
        from: Color,
        to: Color,
        ratio: Float
    ): Color =
        Color(
            ColorUtils.blendARGB(
                from.toArgb(),
                to.toArgb(),
                ratio.coerceIn(
                    0f,
                    1f
                )
            )
        )
}
