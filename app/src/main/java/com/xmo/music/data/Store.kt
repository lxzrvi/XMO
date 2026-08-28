package com.xmo.music.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(
    name = "xmo"
)

data class XmoProfile(
    val name: String = "XMO User",
    val avatarUri: String? = null,
    val avatarIndex: Int = 0
)

object Store {

    /*
     * Existing keys — NEVER rename.
     */
    private val orderKey =
        stringPreferencesKey(
            "home_order"
        )

    private val catsKey =
        stringPreferencesKey(
            "categories"
        )

    /*
     * Search.
     */
    private val searchHistoryKey =
        stringPreferencesKey(
            "search_history"
        )

    /*
     * Setup/profile.
     */
    private val setupCompleteKey =
        booleanPreferencesKey(
            "setup_complete"
        )

    private val profileNameKey =
        stringPreferencesKey(
            "profile_name"
        )

    private val profileAvatarUriKey =
        stringPreferencesKey(
            "profile_avatar_uri"
        )

    private val profileAvatarIndexKey =
        intPreferencesKey(
            "profile_avatar_index"
        )

    val defaults =
        listOf(
            "songs",
            "albums",
            "liked",
            "artists"
        )

    /*
     * =========================================================
     * HOME ORDER
     * =========================================================
     */
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

    /*
     * =========================================================
     * CUSTOM CATEGORIES
     * =========================================================
     */
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

    /*
     * =========================================================
     * FIRST-RUN SETUP
     * =========================================================
     */
    suspend fun setupComplete(
        context: Context
    ): Boolean {
        return context.dataStore
            .data
            .first()[setupCompleteKey]
            ?: false
    }

    suspend fun setSetupComplete(
        context: Context,
        complete: Boolean
    ) {
        context.dataStore.edit {
            it[setupCompleteKey] =
                complete
        }
    }

    /*
     * =========================================================
     * PROFILE
     * =========================================================
     */
    suspend fun profile(
        context: Context
    ): XmoProfile {
        val prefs =
            context.dataStore
                .data
                .first()

        return XmoProfile(
            name =
                prefs[profileNameKey]
                    ?.trim()
                    ?.takeIf {
                        it.isNotEmpty()
                    }
                    ?: "XMO User",

            avatarUri =
                prefs[profileAvatarUriKey]
                    ?.takeIf {
                        it.isNotBlank()
                    },

            avatarIndex =
                prefs[profileAvatarIndexKey]
                    ?: 0
        )
    }

    suspend fun saveProfile(
        context: Context,
        profile: XmoProfile
    ) {
        context.dataStore.edit {
            it[profileNameKey] =
                profile.name
                    .trim()
                    .take(32)

            it[profileAvatarIndexKey] =
                profile.avatarIndex

            val avatar =
                profile.avatarUri

            if (
                avatar.isNullOrBlank()
            ) {
                it.remove(
                    profileAvatarUriKey
                )
            } else {
                it[profileAvatarUriKey] =
                    avatar
            }
        }
    }

    /*
     * Save profile + setup flag atomically.
     */
    suspend fun finishSetup(
        context: Context,
        profile: XmoProfile
    ) {
        context.dataStore.edit {
            it[profileNameKey] =
                profile.name
                    .trim()
                    .ifBlank {
                        "XMO User"
                    }
                    .take(32)

            it[profileAvatarIndexKey] =
                profile.avatarIndex

            if (
                profile.avatarUri
                    .isNullOrBlank()
            ) {
                it.remove(
                    profileAvatarUriKey
                )
            } else {
                it[profileAvatarUriKey] =
                    profile.avatarUri
            }

            it[setupCompleteKey] =
                true
        }
    }
}
