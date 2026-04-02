package ru.itis.core.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

//у меня тут конечно беда с семантикой но исправлять больно..
object StylesCustom {

    val h1 = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Start,
    )

    val h11 = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Start,
    )

    val h2 = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Start,
    )

    val h4 = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Start,
    )

    val h5 = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Start,
    )

    val h7 = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
    )

    val h8 = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Start,
    )

    val h9 = TextStyle(
        fontSize = 32.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
    )

    val body2 = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Start,
    )

    val body3 = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Start,
    )

    val body3Underline = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Start,
        textDecoration = TextDecoration.Underline
    )

    val body4Light = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Start,
    )

    val body5 = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Start,
    )

    val body7 = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
    )

    val body8 = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
    )
}