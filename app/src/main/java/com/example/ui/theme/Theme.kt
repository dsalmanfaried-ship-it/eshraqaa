package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

@Immutable
data class IlmExtendedColors(
    val gold: Color,
    val goldDeep: Color,
    val onGold: Color,
    val cardElevated: Color,
    val hairline: Color,
    val watched: Color,
    val onWatched: Color,
    val heart: Color,
    val mutedText: Color,
    val isDark: Boolean
)

val LocalIlmColors = staticCompositionLocalOf {
    IlmExtendedColors(
        gold = GoldPrimary,
        goldDeep = GoldDeep,
        onGold = GoldOnPrimary,
        cardElevated = EmeraldCardElevated,
        hairline = EmeraldBorder,
        watched = EmeraldMint,
        onWatched = EmeraldMintDark,
        heart = CoralHeart,
        mutedText = SageMuted,
        isDark = true
    )
}

private val IlmDarkColorScheme = darkColorScheme(
    primary = GoldPrimary,
    onPrimary = GoldOnPrimary,
    primaryContainer = EmeraldCardElevated,
    onPrimaryContainer = GoldPrimary,
    secondary = EmeraldMint,
    onSecondary = EmeraldMintDark,
    secondaryContainer = EmeraldBorder,
    onSecondaryContainer = IvoryText,
    tertiary = CoralHeart,
    onTertiary = Color.White,
    background = ObsidianBg,
    onBackground = IvoryText,
    surface = EmeraldSurface,
    onSurface = IvoryText,
    surfaceVariant = EmeraldCardElevated,
    onSurfaceVariant = SageMuted,
    outline = EmeraldBorder,
    outlineVariant = EmeraldBorder,
    error = CoralHeart,
    onError = Color.White
)

private val IlmLightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = Color.White,
    primaryContainer = ParchmentSurface,
    onPrimaryContainer = ForestPrimary,
    secondary = AntiqueGold,
    onSecondary = Color.White,
    secondaryContainer = ParchmentBorder,
    onSecondaryContainer = InkText,
    tertiary = CoralHeart,
    onTertiary = Color.White,
    background = ParchmentBg,
    onBackground = InkText,
    surface = ParchmentElevated,
    onSurface = InkText,
    surfaceVariant = ParchmentSurface,
    onSurfaceVariant = MossMuted,
    outline = ParchmentBorder,
    outlineVariant = ParchmentBorder,
    error = CoralHeart,
    onError = Color.White
)

private val IlmShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun IlmTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) IlmDarkColorScheme else IlmLightColorScheme
    val extendedColors = if (darkTheme) {
        IlmExtendedColors(
            gold = GoldPrimary,
            goldDeep = GoldDeep,
            onGold = GoldOnPrimary,
            cardElevated = EmeraldCardElevated,
            hairline = EmeraldBorder,
            watched = EmeraldMint,
            onWatched = EmeraldMintDark,
            heart = CoralHeart,
            mutedText = SageMuted,
            isDark = true
        )
    } else {
        IlmExtendedColors(
            gold = AntiqueGold,
            goldDeep = ForestPrimary,
            onGold = Color.White,
            cardElevated = ParchmentElevated,
            hairline = ParchmentBorder,
            watched = EmeraldMintLightTheme,
            onWatched = Color.White,
            heart = CoralHeart,
            mutedText = MossMuted,
            isDark = false
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                WindowCompat.getInsetsController(window, view).apply {
                    isAppearanceLightStatusBars = !darkTheme
                    isAppearanceLightNavigationBars = !darkTheme
                }
            }
        }
    }

    CompositionLocalProvider(LocalIlmColors provides extendedColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = IlmTypography,
            shapes = IlmShapes,
            content = content
        )
    }
}
