package ru.itis.feature.profile.impl.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.valentinilk.shimmer.shimmer
import ru.itis.core.domain.model.UserModel
import ru.itis.core.ui.BaseScreen
import ru.itis.core.ui.R
import ru.itis.core.ui.component.DividerCustom
import ru.itis.core.ui.component.ImageCustom
import ru.itis.core.ui.component.PrimaryButtonCustom
import ru.itis.core.ui.component.settings.BottomBarSettings
import ru.itis.core.ui.component.settings.ButtonSettings
import ru.itis.core.ui.component.settings.ImageSettings
import ru.itis.core.ui.theme.BeaconTheme
import ru.itis.core.ui.theme.DimensionsCustom
import ru.itis.core.ui.theme.IconsCustom
import ru.itis.core.ui.theme.StylesCustom
import ru.itis.feature.profile.impl.ui.mvi.ProfileScreenEffect
import ru.itis.feature.profile.impl.ui.mvi.ProfileScreenEvent
import ru.itis.feature.profile.impl.ui.mvi.ProfileScreenState

@Composable
internal fun ProfileScreen(
    profilePreviewModel: UserModel? = null
) {

    val viewModel: ProfileViewModel = hiltViewModel()
    val pageState by viewModel.pageState.collectAsState(initial = ProfileScreenState.Initial)
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.processEvent(
            ProfileScreenEvent.OnInitProfile
        )
        viewModel.pageEffect.collect { effect ->
            when (effect) {
                is ProfileScreenEffect.Message -> Toast.makeText(context, context.getText(effect.message), Toast.LENGTH_SHORT).show()
            }
        }
    }

    BaseScreen(
        bottomBarSettings = BottomBarSettings(
            onMapClick = {
                viewModel.processEvent(
                    ProfileScreenEvent.OnMapBottomBarClick
                )
            },
            onProfileClick = { } //уже тут
        )
    ) { innerPadding ->
        LazyColumn (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
        ) {
            item {
                Text(
                    text = stringResource(R.string.title_profile),
                    style = StylesCustom.h5,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 48.dp, bottom = 16.dp)
                )
                DividerCustom(
                    modifier = Modifier
                        .padding(bottom = 32.dp)
                )
                when (pageState) {
                    is ProfileScreenState.Initial -> {}
                    is ProfileScreenState.Loading -> {
                        ShimmerProfile()
                    }

                    is ProfileScreenState.Unauthorized -> {
                        Spacer(
                            modifier = Modifier
                                .height(DimensionsCustom.bigSpacerHeight)
                        )
                        Text(
                            text = stringResource(R.string.profile_screen_unauth),
                            style = StylesCustom.h4,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier
                                .padding(bottom = DimensionsCustom.defaultSpacer)
                        )
                        PrimaryButtonCustom(
                            buttonSettings = ButtonSettings(
                                text = stringResource(R.string.profile_screen_btn_text_login),
                                onClick = {
                                    viewModel.processEvent(
                                        ProfileScreenEvent.OnLogInBtnClick
                                    )
                                }
                            )
                        )
                        Spacer(
                            modifier = Modifier
                                .height(DimensionsCustom.defaultSpacer)
                        )
                        PrimaryButtonCustom(
                            buttonSettings = ButtonSettings(
                                text = stringResource(R.string.profile_screen_btn_text_signup),
                                onClick = {
                                    viewModel.processEvent(
                                        ProfileScreenEvent.OnSignUpBtnClick
                                    )
                                }
                            )
                        )
                    }

                    is ProfileScreenState.ProfileResult -> {
                        val profileModel = (pageState as? ProfileScreenState.ProfileResult)?.result
                        //val profileModel = profilePreviewModel //для превью
                        profileModel?.let { profile ->
                            Box(
                                contentAlignment = Alignment.Center
                            ) {
                                if (profile.imageUrl.isNotBlank()) {
                                    ImageCustom(
                                        imageSettings = ImageSettings(
                                            url = profile.imageUrl,
                                            shape = CircleShape,
                                            modifier = Modifier
                                                .size(DimensionsCustom.profilePicSize)
                                        )
                                    )
                                } else {
                                    ImageCustom(
                                        imageSettings = ImageSettings(
                                            shape = CircleShape,
                                            modifier = Modifier
                                                .height(DimensionsCustom.profilePicSize)
                                                .width(DimensionsCustom.profilePicSize)
                                        ),
                                    )
                                    Icon(
                                        imageVector = IconsCustom.profileIcon(),
                                        tint = MaterialTheme.colorScheme.onSurface,
                                        contentDescription = "",
                                        modifier = Modifier
                                            .size(DimensionsCustom.iconSizeMax)
                                    )
                                }
                            }

                            Spacer(
                                modifier = Modifier
                                    .height(32.dp)
                            )

                            Text(
                                text = profile.nickname,
                                style = StylesCustom.h9,
                                color = MaterialTheme.colorScheme.onSecondary,
                                modifier = Modifier
                                    .padding(bottom = DimensionsCustom.defaultSpacer)
                            )

                            Text(
                                text = profile.phoneNumber,
                                style = StylesCustom.body8,
                                color = MaterialTheme.colorScheme.onTertiary,
                            )

                            ProfileTab(
                                tabIcon = IconsCustom.tabLogOutIcon(),
                                tabHeader = stringResource(R.string.profile_screen_tab_logout),
                                onClick = {
                                    viewModel.processEvent(
                                        ProfileScreenEvent.OnLogOutTabClick
                                    )
                                },
                                textColor = MaterialTheme.colorScheme.tertiary,
                                iconColor = MaterialTheme.colorScheme.tertiary,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun ProfileTab(
    tabIcon: ImageVector,
    tabHeader: String,
    onClick: () -> Unit,
    textColor: Color = MaterialTheme.colorScheme.onSecondary,
    iconColor: Color = MaterialTheme.colorScheme.primary,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 8.dp)
            .clickable { onClick() },
    ) {
        Icon(
            imageVector = tabIcon,
            contentDescription = "",
            tint = iconColor,
            modifier = Modifier
                .padding(start = 20.dp)
                .size(DimensionsCustom.iconSizeMaxi)
        )
        Text(
            text = tabHeader,
            style = StylesCustom.h2,
            color = textColor,
            modifier = Modifier
                .padding(start = 24.dp)
        )
    }
}

@Composable
internal fun ShimmerProfile() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, bottom = 16.dp)
    ) {
        // Картинка профиля
        Box(
            modifier = Modifier
                .size(DimensionsCustom.profilePicSize)
                .shimmer()
                .background(Color.Gray.copy(alpha = 0.3f), shape = CircleShape)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Никнейм
        Box(
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(DimensionsCustom.shimmerTextHeightTitle)
                .shimmer()
                .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(
                    DimensionsCustom.roundedCornersSmall))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Номер телефона
        Box(
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .height(DimensionsCustom.shimmerTextHeightSubTitle)
                .shimmer()
                .background(Color.Gray.copy(alpha = 0.3f), shape = RoundedCornerShape(
                    DimensionsCustom.roundedCornersSmall))
        )
    }
}


@Preview(showBackground = true, apiLevel = 34)
@Composable
internal fun ProfileScreenPreview() {
    BeaconTheme {
        ProfileScreen(
            profilePreviewModel = UserModel(
                id = 1,
                nickname = "Makoto",
                phoneNumber = "89273455641",
                imageUrl = ""
            )
        )
    }
}