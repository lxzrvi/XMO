package com.xmo.music.data

import android.content.Context

object HomeCategoryAppearanceStore {
    private const val PREFS =
        "xmo_category_appearance"

    fun cover(
        context: Context,
        categoryId: String
    ): String? =
        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        ).getString(
            "cover_$categoryId",
            null
        )

    fun saveCover(
        context: Context,
        categoryId: String,
        cover: String?
    ) {
        val editor =
            context.getSharedPreferences(
                PREFS,
                Context.MODE_PRIVATE
            ).edit()

        if (cover == null) {
            editor.remove(
                "cover_$categoryId"
            )
        } else {
            editor.putString(
                "cover_$categoryId",
                cover
            )
        }

        editor.apply()
    }

    fun delete(
        context: Context,
        categoryId: String
    ) {
        context.getSharedPreferences(
            PREFS,
            Context.MODE_PRIVATE
        )
            .edit()
            .remove(
                "cover_$categoryId"
            )
            .apply()
    }
}
