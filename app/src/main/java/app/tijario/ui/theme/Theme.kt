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
    onSurfaceVariant = TijarioMutedText,
    error = TijarioError,
)

private val TijarioDarkColors: ColorScheme = darkColorScheme(
    primary = Color(0xFF5EEAD4),
    onPrimary = Color(0xFF042F2E),
    secondary = Color(0xFF2DD4BF),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFE2E8F0),
    surface = Color(0xFF111827),
    onSurface = Color(0xFFE2E8F0),
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Color(0xFFCBD5E1),
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
