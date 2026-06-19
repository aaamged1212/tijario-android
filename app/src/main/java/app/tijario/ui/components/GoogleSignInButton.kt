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

        // Draw Google 'G' shape segments
        val strokeWidth = width * 0.22f
        val radius = (width - strokeWidth) / 2f
        val center = Offset(width / 2f, height / 2f)

        // Colors
        val blue = Color(0xFF4285F4)
        val green = Color(0xFF34A853)
        val yellow = Color(0xFFFBBC05)
        val red = Color(0xFFEA4335)

        // 1. Blue segment (horizontal bar and right arc)
        val pathBlue = Path().apply {
            moveTo(center.x + radius, center.y)
            lineTo(center.x, center.y)
            // Move inside and draw G middle line
            moveTo(center.x + radius, center.y)
            arcTo(
                rect = Size(radius * 2, radius * 2).let {
                    androidx.compose.ui.geometry.Rect(
                        center.x - radius,
                        center.y - radius,
                        center.x + radius,
                        center.y + radius
                    )
                },
                startAngleDegrees = 0f,
                sweepAngleDegrees = 45f,
                forceMoveTo = false
            )
        }
        drawPath(path = pathBlue, color = blue, style = Stroke(width = strokeWidth))

        // Since custom path drawing can sometimes look complex, we draw 4 solid arcs for the G circle:
        val rect = androidx.compose.ui.geometry.Rect(
            strokeWidth / 2f,
            strokeWidth / 2f,
            width - strokeWidth / 2f,
            height - strokeWidth / 2f
        )

        // Red (Top arc) - from 180 to 300 approx
        drawArc(
            color = red,
            startAngle = 180f,
            sweepAngle = 140f,
            useCenter = false,
            size = rect.size,
            topLeft = rect.topLeft,
            style = Stroke(width = strokeWidth)
        )

        // Yellow (Left-top arc) - from 120 to 180
        drawArc(
            color = yellow,
            startAngle = 120f,
            sweepAngle = 65f,
            useCenter = false,
            size = rect.size,
            topLeft = rect.topLeft,
            style = Stroke(width = strokeWidth)
        )

        // Green (Bottom arc) - from 45 to 120
        drawArc(
            color = green,
            startAngle = 35f,
            sweepAngle = 90f,
            useCenter = false,
            size = rect.size,
            topLeft = rect.topLeft,
            style = Stroke(width = strokeWidth)
        )

        // Blue (Right arc + horizontal bar)
        drawArc(
            color = blue,
            startAngle = -40f,
            sweepAngle = 80f,
            useCenter = false,
            size = rect.size,
            topLeft = rect.topLeft,
            style = Stroke(width = strokeWidth)
        )

        // Horizontal bar of G
        val barLength = radius * 0.9f
        drawLine(
            color = blue,
            start = Offset(center.x, center.y),
            end = Offset(center.x + barLength + strokeWidth/2f, center.y),
            strokeWidth = strokeWidth
        )
    }
}
