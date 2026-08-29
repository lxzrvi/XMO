package com.xmo.music.ui.nowplaying

import android.content.Context
import android.net.Uri
import com.xmo.music.data.LyricLine
import com.xmo.music.data.SongLyrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal suspend fun readLyrics(
    context: Context,
    uri: Uri
): SongLyrics? =
    withContext(Dispatchers.IO) {
        runCatching {
            val source =
                context.contentResolver
                    .openInputStream(uri)
                    ?.bufferedReader()
                    ?.use {
                        it.readText()
                    }
                    ?: return@runCatching null

            parseLyrics(source)
        }.getOrNull()
    }

internal fun parseLyrics(
    source: String
): SongLyrics {
    val output =
        mutableListOf<LyricLine>()

    val timestamp =
        Regex(
            """\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]"""
        )

    val metadata =
        Regex(
            """\[(ar|ti|al|by|offset|re|ve):.*]""",
            RegexOption.IGNORE_CASE
        )

    source
        .lineSequence()
        .forEach { raw ->

            val stamps =
                timestamp
                    .findAll(raw)
                    .toList()

            if (stamps.isNotEmpty()) {
                val text =
                    timestamp
                        .replace(raw, "")
                        .trim()

                if (text.isNotEmpty()) {
                    stamps.forEach { match ->

                        val minute =
                            match.groupValues[1]
                                .toLongOrNull()
                                ?: 0L

                        val second =
                            match.groupValues[2]
                                .toLongOrNull()
                                ?: 0L

                        val fractionText =
                            match.groupValues[3]

                        val fraction =
                            when (
                                fractionText.length
                            ) {
                                1 ->
                                    (
                                        fractionText
                                            .toLongOrNull()
                                            ?: 0L
                                        ) * 100L

                                2 ->
                                    (
                                        fractionText
                                            .toLongOrNull()
                                            ?: 0L
                                        ) * 10L

                                3 ->
                                    fractionText
                                        .toLongOrNull()
                                        ?: 0L

                                else ->
                                    0L
                            }

                        output +=
                            LyricLine(
                                timeMs =
                                    minute *
                                        60_000L +
                                        second *
                                            1_000L +
                                        fraction,
                                text = text
                            )
                    }
                }
            } else {
                val text =
                    raw.trim()

                if (
                    text.isNotEmpty() &&
                    !metadata.matches(text)
                ) {
                    output +=
                        LyricLine(
                            timeMs = null,
                            text = text
                        )
                }
            }
        }

    val synced =
        output.any {
            it.timeMs != null
        }

    return SongLyrics(
        lines =
            if (synced) {
                output.sortedBy {
                    it.timeMs
                        ?: Long.MAX_VALUE
                }
            } else {
                output
            },
        synced = synced,
        source = "Local"
    )
}

internal fun currentLyricIndex(
    lyrics: SongLyrics,
    position: Long
): Int {
    if (!lyrics.synced) {
        return -1
    }

    var result = -1

    lyrics.lines
        .forEachIndexed { index, line ->

            val timestamp =
                line.timeMs
                    ?: return@forEachIndexed

            if (timestamp <= position) {
                result = index
            } else {
                /*
                 * Input is timestamp sorted. No later timestamp can
                 * become active, though forEachIndexed itself cannot
                 * be broken cheaply.
                 */
            }
        }

    return result
}
