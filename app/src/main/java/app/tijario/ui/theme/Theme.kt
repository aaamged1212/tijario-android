package app.tijario.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TijarioLightColors: ColorScheme = lightColorScheme(
    primary = TijarioPrimary,
    onPrimary = TijarioSurface,
    secondary = TijarioDeepTeal,
    background = TijarioBackground,
    onBackground = TijarioText,
    surface = TijarioSurface,
    onSurface = TijarioText,
    surfaceVariant = TijarioSoftSurface,
    onSurfaceVariant = TijarioMutedText,
    error = TijarioError,
)

@Composable
fun TijarioTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TijarioLightColors,
        typography = TijarioTypography,
        content = content,
    )
}
