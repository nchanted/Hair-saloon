package com.hairsalon.tycoon.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/** Darken a color toward black by [f] (0..1) — used for gradient shadows. */
internal fun shade(c: Color, f: Float): Color = lerp(c, Color.Black, f.coerceIn(0f, 1f))

/** Lighten a color toward white by [f] (0..1) — used for gradient highlights. */
internal fun tint(c: Color, f: Float): Color = lerp(c, Color.White, f.coerceIn(0f, 1f))
