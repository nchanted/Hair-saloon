package com.hairsalon.tycoon.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hairsalon.tycoon.ui.theme.Pink500

/**
 * A salon chair drawn with Canvas, shaded with gradients + a ground shadow to read as 3D.
 * When [occupied], a caped client sits in it with [faceEmoji] as their head.
 */
@Composable
fun ChairVisual(
    occupied: Boolean,
    faceEmoji: String?,
    capeColor: Color,
    modifier: Modifier = Modifier
) {
    val metal = Color(0xFFC4CDD3)
    val seatColor = if (occupied) Pink500 else Color(0xFFCFC3BC)

    Box(modifier, contentAlignment = Alignment.TopCenter) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f

            // Ground shadow for grounding / depth.
            drawOval(
                Color.Black.copy(alpha = 0.16f),
                topLeft = Offset(cx - w * 0.30f, h * 0.88f),
                size = Size(w * 0.60f, h * 0.09f)
            )

            // 5-star base (flat ellipse) with a vertical sheen.
            val baseW = w * 0.54f
            val baseH = h * 0.07f
            drawOval(
                Brush.verticalGradient(
                    listOf(tint(metal, 0.3f), shade(metal, 0.35f)),
                    startY = h * 0.86f, endY = h * 0.95f
                ),
                topLeft = Offset(cx - baseW / 2f, h * 0.86f),
                size = Size(baseW, baseH)
            )

            // Gas-lift post with a metallic horizontal gradient.
            val postW = w * 0.11f
            drawRoundRect(
                Brush.horizontalGradient(
                    listOf(shade(metal, 0.3f), tint(metal, 0.45f), shade(metal, 0.3f)),
                    startX = cx - postW / 2f, endX = cx + postW / 2f
                ),
                topLeft = Offset(cx - postW / 2f, h * 0.64f),
                size = Size(postW, h * 0.26f),
                cornerRadius = CornerRadius(postW / 2f, postW / 2f)
            )

            // Backrest, vertical gradient + soft side shadow.
            val brW = w * 0.46f
            drawRoundRect(
                Color.Black.copy(alpha = 0.10f),
                topLeft = Offset(cx - brW / 2f + 4f, h * 0.10f),
                size = Size(brW, h * 0.50f),
                cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
            )
            drawRoundRect(
                Brush.verticalGradient(
                    listOf(tint(seatColor, 0.18f), seatColor, shade(seatColor, 0.22f)),
                    startY = h * 0.08f, endY = h * 0.60f
                ),
                topLeft = Offset(cx - brW / 2f, h * 0.08f),
                size = Size(brW, h * 0.52f),
                cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
            )
            // Headrest highlight band.
            drawRoundRect(
                tint(seatColor, 0.32f),
                topLeft = Offset(cx - brW / 2f + 3f, h * 0.10f),
                size = Size(brW - 6f, h * 0.07f),
                cornerRadius = CornerRadius(w * 0.10f, w * 0.10f)
            )

            // Seat cushion with gradient.
            val seatW = w * 0.62f
            drawRoundRect(
                Brush.verticalGradient(
                    listOf(tint(seatColor, 0.2f), shade(seatColor, 0.2f)),
                    startY = h * 0.55f, endY = h * 0.69f
                ),
                topLeft = Offset(cx - seatW / 2f, h * 0.55f),
                size = Size(seatW, h * 0.14f),
                cornerRadius = CornerRadius(w * 0.07f, w * 0.07f)
            )

            // Armrests.
            val armW = w * 0.08f
            val armH = h * 0.13f
            val armY = h * 0.49f
            val armBrush = Brush.verticalGradient(listOf(tint(metal, 0.3f), shade(metal, 0.3f)))
            drawRoundRect(armBrush, Offset(cx - seatW / 2f - armW * 0.4f, armY), Size(armW, armH), CornerRadius(armW / 2f, armW / 2f))
            drawRoundRect(armBrush, Offset(cx + seatW / 2f - armW * 0.6f, armY), Size(armW, armH), CornerRadius(armW / 2f, armW / 2f))

            if (occupied) {
                // Cape with vertical gradient + highlight.
                val topY = h * 0.36f
                val botY = h * 0.62f
                val cape = Path().apply {
                    moveTo(cx - w * 0.10f, topY)
                    lineTo(cx + w * 0.10f, topY)
                    lineTo(cx + w * 0.24f, botY)
                    lineTo(cx - w * 0.24f, botY)
                    close()
                }
                drawPath(
                    cape,
                    Brush.verticalGradient(
                        listOf(tint(capeColor, 0.22f), capeColor, shade(capeColor, 0.25f)),
                        startY = topY, endY = botY
                    )
                )
                // Collar.
                drawRoundRect(
                    Color.White,
                    topLeft = Offset(cx - w * 0.11f, h * 0.345f),
                    size = Size(w * 0.22f, h * 0.035f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }

        if (occupied && faceEmoji != null) {
            Text(faceEmoji, fontSize = 30.sp, modifier = Modifier.padding(top = 14.dp))
        }
    }
}
