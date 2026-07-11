package app.tijario.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun GoogleSignInButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String = "تسجيل الدخول باستخدام Google"
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF1F2937)
        ),
        border = ButtonDefaults.outlinedButtonBorder(enabled).copy(
            width = 1.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            GoogleLogoIcon()
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = MaterialTheme.typography.bodyMedium.fontFamily
            )
        }
    }
}

@Composable
fun GoogleLogoIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val width = size.width
        val height = size.height

        val strokeWidth = width * 0.22f
        val rect = androidx.compose.ui.geometry.Rect(
            strokeWidth / 2f,
            strokeWidth / 2f,
            width - strokeWidth / 2f,
            height - strokeWidth / 2f
        )
        val center = Offset(width / 2f, height / 2f)

        // Google G Colors
        val red = Color(0xFFEA4335)
        val yellow = Color(0xFFFBBC05)
        val green = Color(0xFF34A853)
        val blue = Color(0xFF4285F4)

        // 1. Yellow (Left segment)
        drawArc(
            color = yellow,
            startAngle = 140f,
            sweepAngle = 80f,
            useCenter = false,
            size = rect.size,
            topLeft = rect.topLeft,
            style = Stroke(width = strokeWidth)
        )

        // 2. Red (Top segment)
        drawArc(
            color = red,
            startAngle = 220f,
            sweepAngle = 95f,
            useCenter = false,
            size = rect.size,
            topLeft = rect.topLeft,
            style = Stroke(width = strokeWidth)
        )

        // 3. Blue (Right segment)
        drawArc(
            color = blue,
            startAngle = 0f,
            sweepAngle = 45f,
            useCenter = false,
            size = rect.size,
            topLeft = rect.topLeft,
            style = Stroke(width = strokeWidth)
        )

        // 4. Green (Bottom segment)
        drawArc(
            color = green,
            startAngle = 45f,
            sweepAngle = 95f,
            useCenter = false,
            size = rect.size,
            topLeft = rect.topLeft,
            style = Stroke(width = strokeWidth)
        )

        // Horizontal bar of G
        val radius = rect.width / 2f
        drawLine(
            color = blue,
            start = Offset(center.x - strokeWidth / 4f, center.y),
            end = Offset(center.x + radius, center.y),
            strokeWidth = strokeWidth
        )
    }
}
