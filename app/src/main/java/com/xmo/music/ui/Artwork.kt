package com.xmo.music.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import androidx.palette.graphics.Palette
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

object Artwork {
    private val cache = ConcurrentHashMap<String, Color>()

    suspend fun color(context: Context, uri: Uri?): Color {
        if (uri == null) return Color(0xFF35353A)
        val key = uri.toString()
        cache[key]?.let { return it }

        return withContext(Dispatchers.IO) {
            val result = runCatching {
                context.contentResolver.openInputStream(uri)?.use {
                    val bitmap = BitmapFactory.decodeStream(it)
                    Palette.from(bitmap).generate().vibrantSwatch?.rgb
                        ?: Palette.from(bitmap).generate().dominantSwatch?.rgb
                }
            }.getOrNull()?.let(::Color) ?: Color(0xFF35353A)

            cache[key] = result
            result
        }
    }

    fun end(color: Color, theme: com.xmo.music.XmoTheme) =
        when (theme) {
            com.xmo.music.XmoTheme.Dark ->
                Color(ColorUtils.blendARGB(color.toArgb(), 0xFF16161A.toInt(), .72f))
            com.xmo.music.XmoTheme.Light ->
                Color(ColorUtils.blendARGB(color.toArgb(), 0xFFFFFFFF.toInt(), .72f))
            com.xmo.music.XmoTheme.Amoled ->
                Color(ColorUtils.blendARGB(color.toArgb(), 0xFF050505.toInt(), .78f))
        }
}
