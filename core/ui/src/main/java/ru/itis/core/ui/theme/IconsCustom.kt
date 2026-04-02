package ru.itis.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import ru.itis.core.ui.R

object IconsCustom {

    @Composable
    fun profileIcon(): ImageVector = ImageVector.vectorResource(R.drawable.ic_account)

    @Composable
    fun mapIcon(): ImageVector = ImageVector.vectorResource(R.drawable.ic_map)

    @Composable
    fun arrowBackIcon(): ImageVector = ImageVector.vectorResource(R.drawable.ic_arrow_back)

    @Composable
    fun tabLogOutIcon(): ImageVector = ImageVector.vectorResource(R.drawable.ic_logout)

    @Composable
    fun visibilityIcon(): ImageVector = ImageVector.vectorResource(R.drawable.ic_visibility)

    @Composable
    fun visibilityOffIcon(): ImageVector = ImageVector.vectorResource(R.drawable.ic_visibility_off)

    @Composable
    fun editIcon(): ImageVector = ImageVector.vectorResource(R.drawable.ic_pencil)

    @Composable
    fun addPhotoIcon(): ImageVector = ImageVector.vectorResource(R.drawable.ic_add_photo)
}