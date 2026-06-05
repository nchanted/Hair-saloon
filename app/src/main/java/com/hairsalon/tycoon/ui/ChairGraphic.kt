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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hairsalon.tycoon.ui.theme.Pink500
import com.hairsalon.tycoon.ui.theme.PinkDark

/**
 * A stylized salon chair drawn with Canvas (no image assets). When [occupied] is true a
 * caped client is drawn sitting in it, with [faceEmoji] as their head for variety.
 * [capeColor] tints the cape so the player can tell chairs apart at a glance.
 */
@Composable
fun ChairVisual(
    occupied: Boolean,
    faceEmoji: String?,
    capeColor: Color,
    modifier: Modifier = Modifier
) {
    val metal = Color(0xFFB0BEC5)
    val metalDark = Color(0xFF78909C)
    val seatColor = if (occupied) Pink500 else Color(0xFFD7CCC8)
    val seatDark = if (occupied) PinkDark else Color(0xFFA1887F)

    Box(modifier, contentAlignment = Alignment.TopCenter) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f

            // Floor base (5-star style: a flat ellipse) and gas-lift post.
            val baseW = w * 0.52f
            val baseH = h * 0.06f
            drawOval(metalDark, Offset(cx - baseW / 2f, h * 0.90f), Size(baseW, baseH))
            val postW = w * 0.10f
            drawRoundRect(
                metal,
                topLeft = Offset(cx - postW / 2f, h * 0.66f),
                size = Size(postW, h * 0.26f),
                cornerRadius = CornerRadius(postW / 2f, postW / 2f)
            )

            // Backrest (behind the client).
            val brW = w * 0.46f
            drawRoundRect(
                seatColor,
                topLeft = Offset(cx - brW / 2f, h * 0.08f),
                size = Size(brW, h * 0.52f),
                cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
            )
            // Headrest accent band.
            drawRoundRect(
                seatDark,
                topLeft = Offset(cx - brW / 2f, h * 0.08f),
                size = Size(brW, h * 0.10f),
                cornerRadius = CornerRadius(w * 0.12f, w * 0.12f)
            )

            // Seat cushion.
            val seatW = w * 0.62f
            drawRoundRect(
                seatColor,
                topLeft = Offset(cx - seatW / 2f, h * 0.55f),
                size = Size(seatW, h * 0.14f),
                cornerRadius = CornerRadius(w * 0.07f, w * 0.07f)
            )

            // Armrests.
            val armW = w * 0.08f
            val armH = h * 0.13f
            val armY = h * 0.49f
            drawRoundRect(
                metal,
                topLeft = Offset(cx - seatW / 2f - armW * 0.4f, armY),
                size = Size(armW, armH),
                cornerRadius = CornerRadius(armW / 2f, armW / 2f)
            )
            drawRoundRect(
                metal,
                topLeft = Offset(cx + seatW / 2f - armW * 0.6f, armY),
                size = Size(armW, armH),
                cornerRadius = CornerRadius(armW / 2f, armW / 2f)
            )

            if (occupied) {
                // Cape draped over the client: a trapezoid from the neck down to the lap.
                val topY = h * 0.36f
                val botY = h * 0.62f
                val cape = Path().apply {
                    moveTo(cx - w * 0.10f, topY)
                    lineTo(cx + w * 0.10f, topY)
                    lineTo(cx + w * 0.24f, botY)
                    lineTo(cx - w * 0.24f, botY)
                    close()
                }
                drawPath(cape, capeColor)
                // White collar at the neck.
                drawRoundRect(
                    Color.White,
                    topLeft = Offset(cx - w * 0.11f, h * 0.345f),
                    size = Size(w * 0.22f, h * 0.035f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }

        // The client's head sits just above the cape collar.
        if (occupied && faceEmoji != null) {
            Text(faceEmoji, fontSize = 30.sp, modifier = Modifier.padding(top = 14.dp))
        }
    }
}
