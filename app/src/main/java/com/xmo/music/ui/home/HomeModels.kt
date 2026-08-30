package com.xmo.music.ui.home

internal enum class HomeMode {
    Home,
    Liked,
    Categories
}

internal sealed interface HomePage {
    data object Root : HomePage

    data class Category(
        val id: String
    ) : HomePage

    data class CategoryPicker(
        val id: String
    ) : HomePage
}
