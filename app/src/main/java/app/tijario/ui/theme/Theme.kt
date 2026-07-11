package app.tijario.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import app.tijario.config.AppLanguage

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
    background = Color(0xFF0F0F10),
    onBackground = Color.White,
    surface = Color(0xFF1A1A1C),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF262629),
    onSurfaceVariant = Color.White,
    outline = Color.White,
    outlineVariant = Color.White,
    error = TijarioError,
)

@Composable
fun TijarioTheme(
    darkTheme: Boolean = false,
    language: AppLanguage = AppLanguage.AR,
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) TijarioDarkColors else TijarioLightColors,
        typography = tijarioTypography(language),
        content = content,
    )
}
