package com.hairsalon.tycoon.ui

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * The salon "room" drawn behind the gameplay: a wall, a floor, and decor that grows
 * richer as the player renovates to higher tiers (1 Corner Shop -> 4 Luxury Spa).
 * Pure Canvas, no image assets.
 */
@Composable
fun SalonBackground(tier: Int, modifier: Modifier = Modifier) {
    val t = tier.coerceIn(1, 4)
    val wall = when (t) {
        1 -> Color(0xFFF3E9E2)
        2 -> Color(0xFFE3F1EE)
        3 -> Color(0xFFF7E4EE)
        else -> Color(0xFFECE7F6)
    }
    val floor = when (t) {
        1 -> Color(0xFFC9A77F)
        2 -> Color(0xFFCBBAA6)
        3 -> Color(0xFFD7CCC8)
        else -> Color(0xFFE7ECEF)
    }
    val accent = when (t) {
        1 -> Color(0xFF8D6E63)
        2 -> Color(0xFF26A69A)
        3 -> Color(0xFFAD1457)
        else -> Color(0xFFC9A227) // gold
    }

    Canvas(modifier) {
        val w = size.width
        val h = size.height
        val floorY = h * 0.60f

        // Wall: vertical gradient (lighter near ceiling, shaded toward the floor).
        drawRect(
            Brush.verticalGradient(
                listOf(tint(wall, 0.12f), wall, shade(wall, 0.10f)),
                startY = 0f, endY = floorY
            ),
            size = Size(w, floorY)
        )
        // A touch more shade just above the skirting for grounding.
        drawRect(shade(wall, 0.06f), topLeft = Offset(0f, floorY * 0.78f), size = Size(w, floorY * 0.22f))

        // Floor: gradient darker near the wall (far) to lighter near the viewer (near) = depth.
        drawRect(
            Brush.verticalGradient(
                listOf(shade(floor, 0.20f), tint(floor, 0.05f)),
                startY = floorY, endY = h
            ),
            topLeft = Offset(0f, floorY),
            size = Size(w, h - floorY)
        )
        // Skirting board.
        drawRect(accent.copy(alpha = 0.55f), topLeft = Offset(0f, floorY - h * 0.012f), size = Size(w, h * 0.014f))
        // Subtle floorboards / tiles.
        val lines = if (t >= 3) 6 else 4
        for (i in 1 until lines) {
            val x = w * i / lines
            drawLine(Color.Black.copy(alpha = 0.05f), Offset(x, floorY), Offset(x, h), strokeWidth = 2f)
        }

        // --- Decor, cumulative by tier ---

        // Central styling mirror (all tiers).
        mirror(0.5f, 0.10f, 0.20f, 0.34f, accent)

        if (t >= 2) {
            // A potted plant in the corner + wall sconces.
            plant(0.92f, 0.58f, Color(0xFF2E7D32))
            sconce(0.18f, 0.14f, accent)
            sconce(0.82f, 0.14f, accent)
        }
        if (t >= 3) {
            // Extra mirrors flanking the center + pendant lights.
            mirror(0.18f, 0.14f, 0.15f, 0.26f, accent)
            mirror(0.82f, 0.14f, 0.15f, 0.26f, accent)
            pendant(0.35f, 0.12f, Color(0xFFFFE082))
            pendant(0.65f, 0.12f, Color(0xFFFFE082))
        }
        if (t >= 4) {
            // Gold trim line and a bright window: the luxury spa.
            drawRect(accent, topLeft = Offset(0f, floorY - h * 0.004f), size = Size(w, h * 0.006f))
            window(0.5f, 0.07f, 0.26f, 0.30f, accent)
            plant(0.06f, 0.58f, Color(0xFF388E3C))
        }
    }
}

private fun DrawScope.mirror(cxFrac: Float, topFrac: Float, wFrac: Float, hFrac: Float, frame: Color) {
    val mw = size.width * wFrac
    val mh = size.height * hFrac
    val mx = size.width * cxFrac - mw / 2f
    val my = size.height * topFrac
    drawRoundRect(frame, Offset(mx - 5f, my - 5f), Size(mw + 10f, mh + 10f), CornerRadius(14f, 14f))
    drawRoundRect(Color(0xFFDCEAEC), Offset(mx, my), Size(mw, mh), CornerRadius(10f, 10f))
    // Diagonal glare.
    drawLine(
        Color.White.copy(alpha = 0.5f),
        Offset(mx + mw * 0.2f, my + mh * 0.8f),
        Offset(mx + mw * 0.7f, my + mh * 0.15f),
        strokeWidth = mw * 0.06f
    )
}

private fun DrawScope.plant(cxFrac: Float, baseFrac: Float, leaf: Color) {
    val cx = size.width * cxFrac
    val baseY = size.height * baseFrac
    val potW = size.width * 0.07f
    val potH = size.height * 0.06f
    // Pot.
    drawRoundRect(
        Color(0xFFB0623A),
        Offset(cx - potW / 2f, baseY),
        Size(potW, potH),
        CornerRadius(6f, 6f)
    )
    // Foliage.
    val r = size.width * 0.045f
    drawCircle(leaf, r, Offset(cx, baseY - r * 0.6f))
    drawCircle(leaf, r * 0.8f, Offset(cx - r * 0.7f, baseY - r * 1.2f))
    drawCircle(leaf, r * 0.8f, Offset(cx + r * 0.7f, baseY - r * 1.2f))
    drawCircle(leaf, r * 0.7f, Offset(cx, baseY - r * 1.9f))
}

private fun DrawScope.sconce(cxFrac: Float, topFrac: Float, frame: Color) {
    val cx = size.width * cxFrac
    val cy = size.height * topFrac
    drawCircle(Color(0xFFFFF3C4).copy(alpha = 0.85f), size.width * 0.03f, Offset(cx, cy))
    drawCircle(frame, size.width * 0.012f, Offset(cx, cy))
}

private fun DrawScope.pendant(cxFrac: Float, lengthFrac: Float, bulb: Color) {
    val x = size.width * cxFrac
    val len = size.height * lengthFrac
    drawLine(Color.Black.copy(alpha = 0.3f), Offset(x, 0f), Offset(x, len), strokeWidth = 2.5f)
    drawCircle(bulb.copy(alpha = 0.35f), size.width * 0.032f, Offset(x, len))
    drawCircle(bulb, size.width * 0.018f, Offset(x, len))
}

private fun DrawScope.window(cxFrac: Float, topFrac: Float, wFrac: Float, hFrac: Float, frame: Color) {
    val ww = size.width * wFrac
    val wh = size.height * hFrac
    val wx = size.width * cxFrac - ww / 2f
    val wy = size.height * topFrac
    drawRoundRect(frame, Offset(wx - 5f, wy - 5f), Size(ww + 10f, wh + 10f), CornerRadius(10f, 10f))
    drawRoundRect(Color(0xFFCFEAFB), Offset(wx, wy), Size(ww, wh), CornerRadius(6f, 6f))
    // Cross bars.
    drawLine(frame, Offset(wx + ww / 2f, wy), Offset(wx + ww / 2f, wy + wh), strokeWidth = 4f)
    drawLine(frame, Offset(wx, wy + wh / 2f), Offset(wx + ww, wy + wh / 2f), strokeWidth = 4f)
}
