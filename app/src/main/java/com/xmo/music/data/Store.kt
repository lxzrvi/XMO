package com.xmo.music.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(
    name = "xmo"
)

object Store {

    private val orderKey =
        stringPreferencesKey(
            "home_order"
        )

    private val catsKey =
        stringPreferencesKey(
            "categories"
        )

    private val searchHistoryKey =
        stringPreferencesKey(
            "search_history"
        )

    val defaults =
        listOf(
            "songs",
            "albums",
            "liked",
            "artists"
        )

    suspend fun order(
        context: Context
    ): List<String> {
        val raw =
            context.dataStore
                .data
                .first()[orderKey]
                ?: return defaults

        return raw
            .split("|")
            .filter {
                it.isNotBlank()
            }
            .ifEmpty {
                defaults
            }
    }

    suspend fun categories(
        context: Context
    ): List<UserCategory> {
        val raw =
            context.dataStore
                .data
                .first()[catsKey]
                ?: return emptyList()

        return raw
            .lines()
            .mapNotNull { line ->

                val p =
                    line.split(
                        ";",
                        limit = 4
                    )

                if (p.size < 3) {
                    null
                } else {
                    UserCategory(
                        id = p[0],

                        name = p[1],

                        icon =
                            p[2]
                                .toIntOrNull()
                                ?: 0,

                        songIds =
                            p.getOrNull(3)
                                ?.split(",")
                                ?.mapNotNull(
                                    String::toLongOrNull
                                )
                                ?.toSet()
                                ?: emptySet()
                    )
                }
            }
    }

    suspend fun saveOrder(
        context: Context,
        order: List<String>
    ) {
        context.dataStore.edit {
            it[orderKey] =
                order.joinToString(
                    "|"
                )
        }
    }

    suspend fun saveCategories(
        context: Context,
        cats: List<UserCategory>
    ) {
        val raw =
            cats.joinToString(
                "\n"
            ) {
                "${
                    it.id
                };${
                    it.name
                        .replace(
                            ";",
                            ""
                        )
                        .replace(
                            "\n",
                            " "
                        )
                };${
                    it.icon
                };${
                    it.songIds
                        .joinToString(",")
                }"
            }

        context.dataStore.edit {
            it[catsKey] =
                raw
        }
    }

    /*
     * =========================================================
     * SEARCH HISTORY
     * =========================================================
     */

    suspend fun searchHistory(
        context: Context
    ): List<String> {
        val raw =
            context.dataStore
                .data
                .first()[searchHistoryKey]
                ?: return emptyList()

        return raw
            .lines()
            .map {
                it.trim()
            }
            .filter {
                it.isNotEmpty()
            }
            .distinctBy {
                it.lowercase()
            }
            .take(30)
    }

    suspend fun addSearch(
        context: Context,
        query: String
    ): List<String> {
        val clean =
            query
                .trim()
                .replace(
                    "\n",
                    " "
                )

        if (clean.isBlank()) {
            return searchHistory(
                context
            )
        }

        val current =
            searchHistory(
                context
            )

        val next =
            buildList {
                add(clean)

                current.forEach {
                    if (
                        !it.equals(
                            clean,
                            ignoreCase = true
                        )
                    ) {
                        add(it)
                    }
                }
            }
                .take(30)

        saveSearchHistory(
            context,
            next
        )

        return next
    }

    suspend fun removeSearch(
        context: Context,
        query: String
    ): List<String> {
        val next =
            searchHistory(
                context
            )
                .filterNot {
                    it.equals(
                        query,
                        ignoreCase = true
                    )
                }

        saveSearchHistory(
            context,
            next
        )

        return next
    }

    suspend fun clearSearchHistory(
        context: Context
    ) {
        context.dataStore.edit {
            it.remove(
                searchHistoryKey
            )
        }
    }

    private suspend fun saveSearchHistory(
        context: Context,
        history: List<String>
    ) {
        context.dataStore.edit {
            it[searchHistoryKey] =
                history
                    .take(30)
                    .joinToString(
                        "\n"
                    )
        }
    }
}
