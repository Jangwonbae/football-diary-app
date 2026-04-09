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

private val DarkColorScheme = darkColorScheme(
    primary              = SkyBlue,
    onPrimary            = SkyBlueDark,
    primaryContainer     = SkyBlueDarkContainer,
    onPrimaryContainer   = SkyBlueLight,

    secondary            = Blue80,
    onSecondary          = Blue20,
    secondaryContainer   = Blue30,
    onSecondaryContainer = Blue90,

    error                = Red80,
    onError              = Red10,
    errorContainer       = Red40,
    onErrorContainer     = Red90,

    background           = MidnightBlue,
    onBackground         = DarkOnSurface,
    surface              = MidnightBlueLight,
    onSurface            = DarkOnSurface,

    surfaceVariant       = MidnightBlueVariant,
    onSurfaceVariant     = DarkOnSurfaceVariant,
    outline              = DarkOutline,
    outlineVariant       = DarkOutlineVariant,
    surfaceContainer     = MidnightBlueSurface,
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
