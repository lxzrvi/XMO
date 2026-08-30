package com.xmo.music.data

import android.content.Context
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/*
 * =============================================================
 * LOCAL EMBEDDED METADATA
 * =============================================================
 *
 * Reads metadata which Android's MediaMetadataRetriever does not
 * expose reliably.
 *
 * Current support:
 *
 * M4A / MP4:
 * - ilst -> ©lyr -> data
 *
 * Ogg Opus:
 * - OpusTags
 * - LYRICS
 * - UNSYNCEDLYRICS
 * - SYNCEDLYRICS
 *
 * The result type is deliberately not lyrics-only so additional
 * local embedded tags can be added here later without putting
 * binary container parsing inside Library.kt.
 */
internal object EmbeddedMetadata {

    data class Result(
        val lyrics: SongLyrics? = null
    )

    fun read(
        context: Context,
        uri: Uri,
        mimeType: String?
    ): Result {
        return runCatching {
            when {
                mimeType
                    ?.contains(
                        "opus",
                        ignoreCase = true
                    ) == true -> {
                    readOpus(
                        context,
                        uri
                    )
                }

                mimeType
                    ?.contains(
                        "ogg",
                        ignoreCase = true
                    ) == true -> {
                    readOpus(
                        context,
                        uri
                    )
                }

                mimeType
                    ?.contains(
                        "mp4",
                        ignoreCase = true
                    ) == true ||
                    mimeType
                        ?.contains(
                            "m4a",
                            ignoreCase = true
                        ) == true -> {
                    readM4a(
                        context,
                        uri
                    )
                }

                else -> {
                    /*
                     * MIME metadata can occasionally be missing.
                     * Try lightweight signature detection.
                     */
                    detectAndRead(
                        context,
                        uri
                    )
                }
            }
        }.getOrDefault(
            Result()
        )
    }

    /*
     * =========================================================
     * FORMAT DETECTION
     * =========================================================
     */

    private fun detectAndRead(
        context: Context,
        uri: Uri
    ): Result {
        val header =
            context
                .contentResolver
                .openInputStream(uri)
                ?.use { input ->
                    ByteArray(12)
                        .also {
                            input.read(it)
                        }
                }
                ?: return Result()

        return when {
            header.size >= 4 &&
                header[0] == 'O'.code.toByte() &&
                header[1] == 'g'.code.toByte() &&
                header[2] == 'g'.code.toByte() &&
                header[3] == 'S'.code.toByte() -> {
                readOpus(
                    context,
                    uri
                )
            }

            header.size >= 8 &&
                header[4] == 'f'.code.toByte() &&
                header[5] == 't'.code.toByte() &&
                header[6] == 'y'.code.toByte() &&
                header[7] == 'p'.code.toByte() -> {
                readM4a(
                    context,
                    uri
                )
            }

            else ->
                Result()
        }
    }

    /*
     * =========================================================
     * OGG OPUS TAGS
     * =========================================================
     *
     * Ogg pages contain packet segments. OpusTags may span page
     * boundaries, so packets are reconstructed using lacing
     * values rather than treating each page as one packet.
     */

    private fun readOpus(
        context: Context,
        uri: Uri
    ): Result {
        val input =
            context
                .contentResolver
                .openInputStream(uri)
                ?.buffered()
                ?: return Result()

        input.use {
            val packet =
                ByteArrayOutputStream()

            while (true) {
                val capture =
                    readExactly(
                        input = it,
                        count = 4
                    )
                        ?: break

                if (
                    capture[0] != 'O'.code.toByte() ||
                    capture[1] != 'g'.code.toByte() ||
                    capture[2] != 'g'.code.toByte() ||
                    capture[3] != 'S'.code.toByte()
                ) {
                    break
                }

                /*
                 * Remaining fixed Ogg page header after "OggS".
                 */
                val header =
                    readExactly(
                        input = it,
                        count = 23
                    )
                        ?: break

                val segmentCount =
                    header[22]
                        .toInt() and
                        0xFF

                val lacing =
                    readExactly(
                        input = it,
                        count =
                            segmentCount
                    )
                        ?: break

                for (
                    rawLength in lacing
                ) {
                    val length =
                        rawLength
                            .toInt() and
                            0xFF

                    val data =
                        readExactly(
                            input = it,
                            count = length
                        )
                            ?: return Result()

                    if (length > 0) {
                        packet.write(data)
                    }

                    /*
                     * A lacing value below 255 terminates the
                     * current logical packet.
                     */
                    if (length < 255) {
                        val complete =
                            packet.toByteArray()

                        packet.reset()

                        if (
                            complete.startsWithAscii(
                                "OpusTags"
                            )
                        ) {
                            return Result(
                                lyrics =
                                    parseOpusTags(
                                        complete
                                    )
                            )
                        }
                    }
                }
            }
        }

        return Result()
    }

    private fun parseOpusTags(
        packet: ByteArray
    ): SongLyrics? {
        if (
            packet.size < 16 ||
            !packet.startsWithAscii(
                "OpusTags"
            )
        ) {
            return null
        }

        var offset = 8

        fun readUInt32Le(): Long? {
            if (
                offset + 4 >
                packet.size
            ) {
                return null
            }

            val value =
                (
                    packet[offset]
                        .toLong() and
                        0xFF
                    ) or
                    (
                        (
                            packet[offset + 1]
                                .toLong() and
                                0xFF
                            ) shl 8
                        ) or
                    (
                        (
                            packet[offset + 2]
                                .toLong() and
                                0xFF
                            ) shl 16
                        ) or
                    (
                        (
                            packet[offset + 3]
                                .toLong() and
                                0xFF
                            ) shl 24
                        )

            offset += 4

            return value
        }

        val vendorLength =
            readUInt32Le()
                ?.takeIf {
                    it <= Int.MAX_VALUE
                }
                ?.toInt()
                ?: return null

        if (
            vendorLength < 0 ||
            offset + vendorLength >
            packet.size
        ) {
            return null
        }

        offset += vendorLength

        val commentCount =
            readUInt32Le()
                ?.takeIf {
                    it <= 100_000L
                }
                ?.toInt()
                ?: return null

        var best: String? = null

        repeat(commentCount) {
            val length =
                readUInt32Le()
                    ?.takeIf {
                        it <= Int.MAX_VALUE
                    }
                    ?.toInt()
                    ?: return@repeat

            if (
                length < 0 ||
                offset + length >
                packet.size
            ) {
                return@repeat
            }

            val comment =
                String(
                    packet,
                    offset,
                    length,
                    StandardCharsets.UTF_8
                )

            offset += length

            val equals =
                comment.indexOf('=')

            if (equals <= 0) {
                return@repeat
            }

            val key =
                comment
                    .substring(
                        0,
                        equals
                    )
                    .trim()
                    .uppercase()

            val value =
                comment
                    .substring(
                        equals + 1
                    )
                    .trim()

            if (value.isEmpty()) {
                return@repeat
            }

            when (key) {
                "SYNCEDLYRICS" -> {
                    /*
                     * Prefer explicitly synced data.
                     */
                    best = value
                }

                "LYRICS" -> {
                    if (best == null) {
                        best = value
                    }
                }

                "UNSYNCEDLYRICS" -> {
                    if (best == null) {
                        best = value
                    }
                }
            }
        }

        return best
            ?.let {
                parseEmbeddedLyrics(
                    source = it,
                    sourceName =
                        "Embedded Opus"
                )
            }
    }

    /*
     * =========================================================
     * M4A / MP4
     * =========================================================
     *
     * Common iTunes metadata:
     *
     * moov
     *   udta
     *     meta
     *       ilst
     *         ©lyr
     *           data
     */

    private fun readM4a(
        context: Context,
        uri: Uri
    ): Result {
        val bytes =
            context
                .contentResolver
                .openInputStream(uri)
                ?.use {
                    /*
                     * Metadata usually lives near the beginning or
                     * end, but a correct MP4 atom walker needs the
                     * real atom offsets. For local music files we
                     * read through a bounded byte array here.
                     *
                     * 128 MiB avoids pathological allocation for
                     * unusually large files.
                     */
                    readBounded(
                        it,
                        128 * 1024 * 1024
                    )
                }
                ?: return Result()

        val lyric =
            findM4aLyrics(
                bytes = bytes,
                start = 0,
                end = bytes.size,
                depth = 0
            )

        return Result(
            lyrics =
                lyric?.let {
                    parseEmbeddedLyrics(
                        source = it,
                        sourceName =
                            "Embedded M4A"
                    )
                }
        )
    }

    private fun findM4aLyrics(
        bytes: ByteArray,
        start: Int,
        end: Int,
        depth: Int
    ): String? {
        if (
            depth > 12 ||
            start < 0 ||
            end > bytes.size ||
            start >= end
        ) {
            return null
        }

        var offset =
            start

        while (
            offset + 8 <= end
        ) {
            val size32 =
                bytes.uint32Be(
                    offset
                )

            val type =
                bytes.atomType(
                    offset + 4
                )

            var headerSize =
                8

            val atomSize =
                when (size32) {
                    0L ->
                        (
                            end -
                                offset
                            ).toLong()

                    1L -> {
                        if (
                            offset + 16 >
                            end
                        ) {
                            return null
                        }

                        headerSize =
                            16

                        bytes.uint64Be(
                            offset + 8
                        )
                    }

                    else ->
                        size32
                }

            if (
                atomSize <
                headerSize.toLong() ||
                atomSize >
                Int.MAX_VALUE.toLong()
            ) {
                break
            }

            val atomEnd =
                offset +
                    atomSize.toInt()

            if (
                atomEnd <= offset ||
                atomEnd > end
            ) {
                break
            }

            if (
                type == "\u00A9lyr"
            ) {
                val lyric =
                    readLyricAtom(
                        bytes =
                            bytes,
                        start =
                            offset +
                                headerSize,
                        end =
                            atomEnd
                    )

                if (
                    !lyric.isNullOrBlank()
                ) {
                    return lyric
                }
            }

            /*
             * Only known container atoms are recursively walked.
             * 'meta' contains a four-byte version/flags field
             * before its child atoms.
             */
            if (
                type in
                mp4Containers
            ) {
                val childStart =
                    offset +
                        headerSize +
                        if (
                            type ==
                            "meta"
                        ) {
                            4
                        } else {
                            0
                        }

                if (
                    childStart <
                    atomEnd
                ) {
                    val nested =
                        findM4aLyrics(
                            bytes =
                                bytes,
                            start =
                                childStart,
                            end =
                                atomEnd,
                            depth =
                                depth + 1
                        )

                    if (
                        nested != null
                    ) {
                        return nested
                    }
                }
            }

            offset =
                atomEnd
        }

        return null
    }

    private fun readLyricAtom(
        bytes: ByteArray,
        start: Int,
        end: Int
    ): String? {
        var offset =
            start

        while (
            offset + 8 <= end
        ) {
            val size =
                bytes.uint32Be(
                    offset
                )

            if (
                size < 8L ||
                size >
                Int.MAX_VALUE.toLong()
            ) {
                return null
            }

            val atomEnd =
                offset +
                    size.toInt()

            if (
                atomEnd > end ||
                atomEnd <= offset
            ) {
                return null
            }

            val type =
                bytes.atomType(
                    offset + 4
                )

            if (
                type == "data"
            ) {
                /*
                 * MP4 metadata data atom payload:
                 *
                 * 8-byte atom header
                 * 4-byte type/flags
                 * 4-byte locale
                 * actual UTF-8 text
                 */
                val textStart =
                    offset + 16

                if (
                    textStart <=
                    atomEnd
                ) {
                    return String(
                        bytes,
                        textStart,
                        atomEnd -
                            textStart,
                        StandardCharsets.UTF_8
                    )
                        .trim(
                            '\u0000',
                            ' ',
                            '\r',
                            '\n',
                            '\t'
                        )
                        .takeIf {
                            it.isNotBlank()
                        }
                }
            }

            offset =
                atomEnd
        }

        return null
    }

    /*
     * =========================================================
     * SHARED LYRIC TEXT PARSER
     * =========================================================
     *
     * Embedded lyrics may themselves contain LRC timestamps.
     * When timestamps exist they become real synced lyrics.
     * Otherwise every non-empty line remains visible as untimed
     * lyrics.
     */

    private fun parseEmbeddedLyrics(
        source: String,
        sourceName: String
    ): SongLyrics? {
        val cleaned =
            source
                .replace(
                    "\u0000",
                    ""
                )
                .trim()

        if (
            cleaned.isEmpty()
        ) {
            return null
        }

        val timestamp =
            Regex(
                """\[(\d{1,3}):(\d{1,2})(?:[.:](\d{1,3}))?]"""
            )

        val metadata =
            Regex(
                """\[(ar|ti|al|by|offset|re|ve):.*]""",
                RegexOption.IGNORE_CASE
            )

        val output =
            mutableListOf<LyricLine>()

        cleaned
            .lineSequence()
            .forEach { raw ->

                val stamps =
                    timestamp
                        .findAll(raw)
                        .toList()

                if (
                    stamps.isNotEmpty()
                ) {
                    val text =
                        timestamp
                            .replace(
                                raw,
                                ""
                            )
                            .trim()

                    if (
                        text.isNotEmpty()
                    ) {
                        stamps.forEach {
                                match ->

                            val minute =
                                match
                                    .groupValues[1]
                                    .toLongOrNull()
                                    ?: 0L

                            val second =
                                match
                                    .groupValues[2]
                                    .toLongOrNull()
                                    ?: 0L

                            val fractionText =
                                match
                                    .groupValues[3]

                            val fraction =
                                when (
                                    fractionText.length
                                ) {
                                    1 ->
                                        (
                                            fractionText
                                                .toLongOrNull()
                                                ?: 0L
                                            ) *
                                            100L

                                    2 ->
                                        (
                                            fractionText
                                                .toLongOrNull()
                                                ?: 0L
                                            ) *
                                            10L

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
                                    text =
                                        text
                                )
                        }
                    }
                } else {
                    val text =
                        raw.trim()

                    if (
                        text.isNotEmpty() &&
                        !metadata.matches(
                            text
                        )
                    ) {
                        output +=
                            LyricLine(
                                timeMs = null,
                                text = text
                            )
                    }
                }
            }

        if (
            output.isEmpty()
        ) {
            /*
             * Some tags contain one long line rather than line
             * breaks. Do not lose genuine metadata.
             */
            return SongLyrics(
                lines =
                    listOf(
                        LyricLine(
                            text = cleaned
                        )
                    ),
                synced = false,
                source = sourceName
            )
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
            synced =
                synced,
            source =
                sourceName
        )
    }

    private val mp4Containers =
        setOf(
            "moov",
            "udta",
            "meta",
            "ilst"
        )

    /*
     * =========================================================
     * BINARY HELPERS
     * =========================================================
     */

    private fun readExactly(
        input: java.io.InputStream,
        count: Int
    ): ByteArray? {
        if (
            count < 0
        ) {
            return null
        }

        val result =
            ByteArray(count)

        var offset = 0

        while (
            offset <
            count
        ) {
            val read =
                input.read(
                    result,
                    offset,
                    count -
                        offset
                )

            if (
                read < 0
            ) {
                return null
            }

            offset += read
        }

        return result
    }

    private fun readBounded(
        input: java.io.InputStream,
        maxBytes: Int
    ): ByteArray {
        val output =
            ByteArrayOutputStream()

        val buffer =
            ByteArray(
                32 * 1024
            )

        var total = 0

        while (true) {
            val read =
                input.read(buffer)

            if (
                read < 0
            ) {
                break
            }

            if (
                total + read >
                maxBytes
            ) {
                /*
                 * Do not attempt an unbounded in-memory parse.
                 */
                break
            }

            output.write(
                buffer,
                0,
                read
            )

            total += read
        }

        return output.toByteArray()
    }

    private fun ByteArray.startsWithAscii(
        value: String
    ): Boolean {
        val target =
            value.toByteArray(
                StandardCharsets.US_ASCII
            )

        if (
            size <
            target.size
        ) {
            return false
        }

        for (
            index in
            target.indices
        ) {
            if (
                this[index] !=
                target[index]
            ) {
                return false
            }
        }

        return true
    }

    private fun ByteArray.uint32Be(
        offset: Int
    ): Long {
        if (
            offset < 0 ||
            offset + 4 >
            size
        ) {
            return 0L
        }

        return (
            (
                this[offset]
                    .toLong() and
                    0xFF
                ) shl 24
            ) or
            (
                (
                    this[offset + 1]
                        .toLong() and
                        0xFF
                    ) shl 16
                ) or
            (
                (
                    this[offset + 2]
                        .toLong() and
                        0xFF
                    ) shl 8
                ) or
            (
                this[offset + 3]
                    .toLong() and
                    0xFF
                )
    }

    private fun ByteArray.uint64Be(
        offset: Int
    ): Long {
        if (
            offset < 0 ||
            offset + 8 >
            size
        ) {
            return 0L
        }

        var value = 0L

        for (
            index in
            0 until 8
        ) {
            value =
                (
                    value shl 8
                    ) or
                    (
                        this[offset + index]
                            .toLong() and
                            0xFF
                        )
        }

        return value
    }

    private fun ByteArray.atomType(
        offset: Int
    ): String {
        if (
            offset < 0 ||
            offset + 4 >
            size
        ) {
            return ""
        }

        return String(
            this,
            offset,
            4,
            StandardCharsets.ISO_8859_1
        )
    }
}
