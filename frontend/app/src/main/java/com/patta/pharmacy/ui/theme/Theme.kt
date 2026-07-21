package com.patta.pharmacy.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val PattaColors = lightColorScheme(
    primary = PattaTeal,
    onPrimary = SurfaceCard,
    primaryContainer = PattaTealContainer,
    onPrimaryContainer = PattaTealDark,
    secondary = MoneyIn,
    onSecondary = SurfaceCard,
    secondaryContainer = MoneyInContainer,
    tertiary = ExpiryAmber,
    tertiaryContainer = ExpiryAmberContainer,
    error = MoneyOut,
    onError = SurfaceCard,
    errorContainer = MoneyOutContainer,
    background = Surface,
    onBackground = OnSurface,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceContainer,
    onSurfaceVariant = OnSurfaceVariant,
    surfaceContainerLowest = SurfaceCard,
    outline = OutlineSoft,
)

private val PattaShapes = Shapes(
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),   // buttons
    large = RoundedCornerShape(16.dp),    // cards
)

@Composable
fun PattaTheme(content: @Composable () -> Unit) {
    // Light-only for MVP: the design is a bright, low-glare counter UI.
    MaterialTheme(
        colorScheme = PattaColors,
        typography = PattaTypography,
        shapes = PattaShapes,
        content = content
    )
}
