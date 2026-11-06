package com.tj.expensetracker.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.haze
import dev.chrisbanes.haze.hazeChild

/**
 * Lets GlassSurface find the backdrop it should blur,
 * so you don't have to pass hazeState around manually.
 */
val LocalGlassHazeState = staticCompositionLocalOf<HazeState?> { null }

private data class Blob(
    val x: Float,       // 0..1 of width
    val y: Float,       // 0..1 of height
    val radius: Float,  // fraction of width
    val color: Color
)

@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val darkTheme = LocalIsDarkTheme.current ?: isSystemInDarkTheme()
    val hazeState = androidx.compose.runtime.remember { HazeState() }

    val baseColors = if (darkTheme) {
        listOf(Color(0xFF384D83), Color(0xFF17253D), Color(0xFF1E1B4B))
    } else {
        listOf(Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFFFFFFFF))
    }

    // Glass needs something colorful behind it, otherwise there's nothing to blur/refract.
    val blobs = if (darkTheme) {
        listOf(
            Blob(0.15f, 0.10f, 0.70f, Color(0xFF7C3AED).copy(alpha = 0.60f)), // violet
            Blob(0.95f, 0.35f, 0.60f, Color(0xFF06B6D4).copy(alpha = 0.45f)), // cyan
            Blob(0.20f, 0.72f, 0.75f, Color(0xFFEC4899).copy(alpha = 0.40f)), // pink
            Blob(0.90f, 0.98f, 0.60f, Color(0xFF3B82F6).copy(alpha = 0.50f))  // blue
        )
    } else {
        listOf(
            Blob(0.15f, 0.10f, 0.70f, Color(0xFF000000).copy(alpha = 0.60f)),
            Blob(0.95f, 0.35f, 0.60f, Color(0xFF67E8F9).copy(alpha = 0.55f)),
            Blob(0.20f, 0.72f, 0.75f, Color(0xFFF9A8D4).copy(alpha = 0.55f)),
            Blob(0.90f, 0.98f, 0.60f, Color(0xFF93C5FD).copy(alpha = 0.60f))
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Layer 1: the backdrop. This is what the glass blurs.
        // It must be a SIBLING of the content, not a parent of the glass.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .haze(state = hazeState)
                .background(Brush.verticalGradient(colors = baseColors))
                .drawWithCache {
                    // 1. Pre-calculate brushes here. This block ONLY runs when the size changes!
                    val cachedBlobs = blobs.map { blob ->
                        val center = Offset(size.width * blob.x, size.height * blob.y)
                        val r = size.width * blob.radius
                        val brush = Brush.radialGradient(
                            colors = listOf(blob.color, Color.Transparent),
                            center = center,
                            radius = r
                        )
                        Triple(brush, r, center)
                    }

                    // 2. Do the actual drawing here using the cached instances
                    onDrawBehind {
                        cachedBlobs.forEach { (brush, r, center) ->
                            drawCircle(brush = brush, radius = r, center = center)
                        }
                    }
                }
        )

        // Layer 2: your actual screen content
        CompositionLocalProvider(LocalGlassHazeState provides hazeState) {
            this@Box.content()
        }
    }
}

@Composable
fun GlassSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    content: @Composable BoxScope.() -> Unit
) {
    val darkTheme = LocalIsDarkTheme.current ?: isSystemInDarkTheme()
    val hazeState = LocalGlassHazeState.current

    // Much more transparent than before (0.88 alpha was basically a solid white card)
    val fillBrush = androidx.compose.runtime.remember(darkTheme) {
        if (darkTheme) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.14f),
                    Color.White.copy(alpha = 0.04f)
                )
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.50f),
                    Color.White.copy(alpha = 0.18f)
                )
            )
        }
    }

    // Diagonal specular shine in the top-left + slight shading bottom-right = fake 3D curvature
    val sheenBrush = androidx.compose.runtime.remember(darkTheme) {
        Brush.linearGradient(
            0.00f to Color.White.copy(alpha = if (darkTheme) 0.18f else 0.45f),
            0.35f to Color.White.copy(alpha = 0.04f),
            0.60f to Color.Transparent,
            1.00f to Color.Black.copy(alpha = if (darkTheme) 0.14f else 0.05f)
        )
    }

    // Bright rim on the light-facing edge, fading out on the far edge
    val borderBrush = androidx.compose.runtime.remember(darkTheme) {
        Brush.linearGradient(
            0.0f to Color.White.copy(alpha = if (darkTheme) 0.60f else 0.95f),
            0.5f to Color.White.copy(alpha = 0.12f),
            1.0f to Color.White.copy(alpha = if (darkTheme) 0.25f else 0.50f)
        )
    }

    Box(
        modifier = modifier
            .shadow(
                elevation = if (darkTheme) 20.dp else 10.dp,
                shape = shape,
                ambientColor = Color.Black.copy(alpha = if (darkTheme) 0.25f else 0.08f),
                spotColor = Color.Black.copy(alpha = if (darkTheme) 0.35f else 0.14f)
            )
            .clip(shape)
            // The actual frosted-glass part: blurs whatever is behind this surface.
            .then(
                if (hazeState != null) {
                    Modifier.hazeChild(
                        state = hazeState,
                        style = HazeStyle(
                            blurRadius = 28.dp,
                            noiseFactor = 0.04f,
                            backgroundColor = Color.Transparent,
                            tint = HazeTint(Color.Transparent)
                        )
                    )
                } else Modifier
            )
            .background(fillBrush)
            .background(sheenBrush)
            .border(width = 1.dp, brush = borderBrush, shape = shape),
        content = content
    )
}
