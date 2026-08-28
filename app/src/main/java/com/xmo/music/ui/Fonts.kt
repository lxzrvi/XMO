package com.xmo.music.ui

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.xmo.music.R

object XmoFont {

    /*
     * XMO branding only.
     */
    val logo =
        FontFamily(
            Font(
                R.font.xmo_logo_text
            )
        )

    /*
     * Profile / username.
     */
    val user =
        FontFamily(
            Font(
                R.font.xmo_user_text
            )
        )

    /*
     * Headings and strong song titles.
     */
    val bold =
        FontFamily(
            Font(
                R.font.xmo_bold
            )
        )

    /*
     * General interface text.
     */
    val normal =
        FontFamily(
            Font(
                R.font.xmo_normal
            )
        )

    /*
     * Controls and labels.
     */
    val medium =
        FontFamily(
            Font(
                R.font.xmo_medium
            )
        )

    /*
     * Artist names, metadata and secondary text.
     */
    val thin =
        FontFamily(
            Font(
                R.font.xmo_thin
            )
        )
}
