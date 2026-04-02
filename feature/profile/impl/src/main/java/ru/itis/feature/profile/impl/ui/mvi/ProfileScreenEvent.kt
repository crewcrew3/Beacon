package ru.itis.feature.profile.impl.ui.mvi

internal sealed interface ProfileScreenEvent {
    data object OnInitProfile : ProfileScreenEvent
    data object OnLogOutTabClick : ProfileScreenEvent
    data object OnLogInBtnClick : ProfileScreenEvent
    data object OnSignUpBtnClick : ProfileScreenEvent
    data object OnMapBottomBarClick : ProfileScreenEvent
}