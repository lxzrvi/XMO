package com.xmo.music.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore("xmo")

object Store {
    private val orderKey = stringPreferencesKey("home_order")
    private val catsKey = stringPreferencesKey("categories")

    val defaults = listOf("songs", "albums", "liked", "artists")

    suspend fun order(context: Context): List<String> {
        val raw = context.dataStore.data.first()[orderKey] ?: return defaults
        return raw.split("|").filter { it.isNotBlank() }.ifEmpty { defaults }
    }

    suspend fun categories(context: Context): List<UserCategory> {
        val raw = context.dataStore.data.first()[catsKey] ?: return emptyList()
        return raw.lines().mapNotNull { line ->
            val p = line.split(";", limit = 4)
            if (p.size < 3) null else UserCategory(
                p[0], p[1], p[2].toIntOrNull() ?: 0,
                p.getOrNull(3)?.split(",")?.mapNotNull(String::toLongOrNull)?.toSet()
                    ?: emptySet()
            )
        }
    }

    suspend fun saveOrder(context: Context, order: List<String>) {
        context.dataStore.edit { it[orderKey] = order.joinToString("|") }
    }

    suspend fun saveCategories(context: Context, cats: List<UserCategory>) {
        val raw = cats.joinToString("\n") {
            "${it.id};${it.name.replace(";", "")};${it.icon};${it.songIds.joinToString(",")}"
        }
        context.dataStore.edit { it[catsKey] = raw }
    }
}
