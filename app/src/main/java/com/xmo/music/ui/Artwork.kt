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
        Color(0xFF35353A)

    /*
     * Finished colours.
     */
    private val cache =
        ConcurrentHashMap<String, Color>()

    /*
     * Prevent multiple tiles requesting the SAME album art
     * from decoding/palette-processing it simultaneously.
     */
    private val locks =
        ConcurrentHashMap<String, Mutex>()

    suspend fun color(
        context: Context,
        uri: Uri?
    ): Color {
        if (uri == null) {
            return fallback
        }

        val key =
            uri.toString()

        cache[key]?.let {
            return it
        }

        val mutex =
            locks.getOrPut(key) {
                Mutex()
            }

        return mutex.withLock {

            /*
             * Another coroutine may have filled the cache
             * while this request waited for the mutex.
             */
            cache[key]?.let {
                return@withLock it
            }

            val result =
                withContext(
                    Dispatchers.IO
                ) {
                    runCatching {

                        context
                            .contentResolver
                            .openInputStream(uri)
                            ?.use { stream ->

                                val bitmap =
                                    BitmapFactory
                                        .decodeStream(
                                            stream
                                        )
                                        ?: return@use null

                                /*
                                 * Generate Palette ONCE.
                                 *
                                 * Previous implementation could
                                 * generate it twice for one bitmap.
                                 */
                                val palette =
                                    Palette
                                        .from(bitmap)
                                        .generate()

                                val rgb =
                                    palette
                                        .vibrantSwatch
                                        ?.rgb
                                        ?: palette
                                            .dominantSwatch
                                            ?.rgb

                                /*
                                 * Palette is finished with bitmap.
                                 */
                                bitmap.recycle()

                                rgb
                            }

                    }
                        .getOrNull()
                        ?.let {
                            Color(it)
                        }
                        ?: fallback
                }

            cache[key] =
                result

            locks.remove(
                key,
                mutex
            )

            result
        }
    }

    /*
     * Allows SongTile to obtain an already computed colour
     * synchronously without launching unnecessary work.
     */
    fun cached(
        uri: Uri?
    ): Color? {
        if (uri == null) {
            return fallback
        }

        return cache[
            uri.toString()
        ]
    }

    fun end(
        color: Color,
        theme: XmoTheme
    ): Color {
        return when (theme) {

            XmoTheme.Dark ->
                Color(
                    ColorUtils.blendARGB(
                        color.toArgb(),
                        0xFF16161A.toInt(),
                        .72f
                    )
                )

            XmoTheme.Light ->
                Color(
                    ColorUtils.blendARGB(
                        color.toArgb(),
                        0xFFFFFFFF.toInt(),
                        .72f
                    )
                )

            XmoTheme.Amoled ->
                Color(
                    ColorUtils.blendARGB(
                        color.toArgb(),
                        0xFF050505.toInt(),
                        .78f
                    )
                )
        }
    }
}
