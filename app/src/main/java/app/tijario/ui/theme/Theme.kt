package app.tijario.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val TijarioLightColors: ColorScheme = lightColorScheme(
    primary = TijarioPrimary,
    onPrimary = TijarioSurface,
    secondary = TijarioDeepTeal,
    background = TijarioBackground,
    onBackground = TijarioText,
    surface = TijarioSurface,
    onSurface = TijarioText,
    surfaceVariant = TijarioSoftSurface,
    onSurfaceVariant = Color.Black,
    outline = Color.Black,
    outlineVariant = Color.Black,
    error = TijarioError,
)

private val TijarioDarkColors: ColorScheme = darkColorScheme(
    primary = TijarioPrimary,
    onPrimary = Color.White,
    secondary = TijarioDeepTeal,
    background = Color(0xFF0F172A),
    onBackground = Color.White,
    surface = Color(0xFF111827),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color.White,
    outline = Color.White,
    outlineVariant = Color.White,
    error = TijarioError,
)

@Composable
fun TijarioTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) TijarioDarkColors else TijarioLightColors,
        typography = TijarioTypography,
        content = content,
    )
}
