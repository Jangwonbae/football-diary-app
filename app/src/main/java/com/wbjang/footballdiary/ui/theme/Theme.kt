package com.wbjang.footballdiary.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary          = Blue40,
    onPrimary        = Color.White,
    primaryContainer = Blue90,
    onPrimaryContainer = Blue10,

    secondary          = BlueGrey40,
    onSecondary        = Color.White,
    secondaryContainer = BlueGrey90,
    onSecondaryContainer = BlueGrey30,

    tertiary          = Indigo40,
    onTertiary        = Color.White,
    tertiaryContainer = Indigo90,
    onTertiaryContainer = Indigo30,

    error          = Red40,
    onError        = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red10,

    background = Grey99,
    onBackground = Grey10,
    surface    = Grey99,
    onSurface  = Grey10,

    surfaceVariant    = GreyVariant90,
    onSurfaceVariant  = GreyVariant30,
    outline           = GreyVariant50,
    outlineVariant    = GreyVariant80,
    surfaceContainer  = Grey95,
)

// 밤 테마는 추후 설정
private val DarkColorScheme = darkColorScheme(
    primary          = Blue80,
    onPrimary        = Blue20,
    primaryContainer = Blue30,
    onPrimaryContainer = Blue90,
    background = DarkSurface,
    surface    = DarkSurface,
)

@Composable
fun FootballDiaryTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
