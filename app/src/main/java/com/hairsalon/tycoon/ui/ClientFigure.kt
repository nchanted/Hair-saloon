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

/**
 * A full-body standing client drawn with Canvas (shirt, arms, legs, shoes, ground shadow),
 * with the client's [faceEmoji] as the head. [shirt] and [pants] vary per client for variety.
 */
@Composable
fun ClientFigure(
    faceEmoji: String,
    shirt: Color,
    pants: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier, contentAlignment = Alignment.TopCenter) {
        Canvas(Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f

            // Ground shadow.
            drawOval(
                Color.Black.copy(alpha = 0.14f),
                topLeft = Offset(cx - w * 0.24f, h * 0.90f),
                size = Size(w * 0.48f, h * 0.06f)
            )

            // Legs (pants).
            val legW = w * 0.15f
            val legTop = h * 0.57f
            val legBot = h * 0.87f
            val legBrush = Brush.verticalGradient(listOf(tint(pants, 0.12f), shade(pants, 0.22f)), startY = legTop, endY = legBot)
            drawRoundRect(legBrush, Offset(cx - legW - w * 0.015f, legTop), Size(legW, legBot - legTop), CornerRadius(legW * 0.4f, legW * 0.4f))
            drawRoundRect(legBrush, Offset(cx + w * 0.015f, legTop), Size(legW, legBot - legTop), CornerRadius(legW * 0.4f, legW * 0.4f))
            // Shoes.
            val shoe = Color(0xFF3E2723)
            val shoeH = h * 0.04f
            drawRoundRect(shoe, Offset(cx - legW - w * 0.03f, legBot - shoeH), Size(legW + w * 0.03f, shoeH), CornerRadius(6f, 6f))
            drawRoundRect(shoe, Offset(cx + w * 0.015f, legBot - shoeH), Size(legW + w * 0.03f, shoeH), CornerRadius(6f, 6f))

            // Arms (behind torso), shaded shirt color.
            val armBrush = Brush.verticalGradient(listOf(shade(shirt, 0.08f), shade(shirt, 0.30f)))
            val armW = w * 0.11f
            drawRoundRect(armBrush, Offset(cx - w * 0.26f, h * 0.33f), Size(armW, h * 0.23f), CornerRadius(armW / 2f, armW / 2f))
            drawRoundRect(armBrush, Offset(cx + w * 0.26f - armW, h * 0.33f), Size(armW, h * 0.23f), CornerRadius(armW / 2f, armW / 2f))

            // Torso (shirt) as a tapered body with a vertical gradient.
            val torso = Path().apply {
                moveTo(cx - w * 0.22f, h * 0.34f)
                lineTo(cx + w * 0.22f, h * 0.34f)
                lineTo(cx + w * 0.17f, h * 0.58f)
                lineTo(cx - w * 0.17f, h * 0.58f)
                close()
            }
            drawPath(
                torso,
                Brush.verticalGradient(
                    listOf(tint(shirt, 0.22f), shirt, shade(shirt, 0.20f)),
                    startY = h * 0.30f, endY = h * 0.58f
                )
            )
            // Collar.
            val collar = Path().apply {
                moveTo(cx - w * 0.07f, h * 0.34f)
                lineTo(cx + w * 0.07f, h * 0.34f)
                lineTo(cx, h * 0.40f)
                close()
            }
            drawPath(collar, tint(shirt, 0.35f))
        }

        // Head.
        Text(faceEmoji, fontSize = 26.sp, modifier = Modifier.padding(top = 2.dp))
    }
}
