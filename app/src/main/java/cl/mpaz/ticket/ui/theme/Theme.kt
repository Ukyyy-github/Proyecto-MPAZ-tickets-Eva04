package cl.mpaz.ticket.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Blue600,
    onPrimary = Color.White,
    primaryContainer = Blue100,
    onPrimaryContainer = Blue900,
    secondary = Blue700,
    background = SoftBlueBackground,
    surface = Color.White,
    outline = Blue300
)

private val DarkColorScheme = darkColorScheme(
    primary = Blue300,
    onPrimary = Blue900,
    primaryContainer = Blue700,
    onPrimaryContainer = Blue100,
    secondary = Blue100,
    outline = Blue500
)

@Composable
fun MPAZTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
