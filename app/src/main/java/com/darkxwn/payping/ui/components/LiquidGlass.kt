package com.darkxwn.payping.ui.components

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect as ComposeRenderEffect
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.darkxwn.payping.domain.model.LiquidGlassLevel
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect

val LocalHazeState = compositionLocalOf<HazeState?> { null }

private const val LIQUID_GLASS_SHADER_SRC = """
    uniform shader contents;
    uniform float2 resolution;
    uniform float cornerRadius;
    uniform float bezelWidth;
    uniform float refraction;
    uniform float dispersion;
    uniform float edgeHighlight;

    float sdRoundedBox(float2 p, float2 b, float r) {
        float2 q = abs(p) - b + float2(r);
        return length(max(q, float2(0.0))) + min(max(q.x, q.y), 0.0) - r;
    }

    half4 main(float2 fragCoord) {
        float2 halfSize = resolution * 0.5;
        float safeRadius = min(cornerRadius, min(halfSize.x, halfSize.y));
        float2 p = fragCoord - halfSize;
        float dist = sdRoundedBox(p, halfSize, safeRadius);

        float bezel = clamp(-dist / max(bezelWidth, 1.0), 0.0, 1.0);
        float rimCurve = pow(1.0 - bezel, 2.0);
        float centerFactor = smoothstep(0.0, 1.0, 1.0 - bezel);

        float2 dir = p / max(length(p), 0.001);
        float2 offset = dir * (rimCurve * refraction + centerFactor * 1.5);
        float2 baseCoord = clamp(fragCoord - offset, float2(0.0), resolution - float2(1.0));
        float2 redCoord = clamp(fragCoord - dir * (rimCurve * (refraction + dispersion)), float2(0.0), resolution - float2(1.0));
        float2 blueCoord = clamp(fragCoord - dir * (rimCurve * max(refraction - dispersion, 0.0)), float2(0.0), resolution - float2(1.0));

        half4 center = contents.eval(baseCoord);
        half rCol = contents.eval(redCoord).r;
        half bCol = contents.eval(blueCoord).b;

        // Soft directional specular sheen inside the beveled glass edge (top-left brighter, bottom-right dimmer).
        float lightAngle = clamp((-dir.x * 0.65 - dir.y * 0.75) * 0.5 + 0.5, 0.0, 1.0);
        float sheen = rimCurve * edgeHighlight * (0.35 + 0.65 * lightAngle);

        half3 lit = clamp(half3(rCol, center.g, bCol) + half3(sheen), half3(0.0), half3(1.0));
        return half4(lit, center.a);
    }
"""

@Composable
fun LiquidGlassContainer(
    liquidGlassLevel: LiquidGlassLevel,
    shape: Shape,
    cornerRadiusDp: Dp,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = LocalHazeState.current,
    solidColor: Color = MaterialTheme.colorScheme.surfaceContainer,
    refractionDp: Dp = 18.dp,
    dispersionDp: Dp = 3.2.dp,
    bezelWidthDp: Dp = 13.dp,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    if (liquidGlassLevel == LiquidGlassLevel.NONE) {
        Surface(
            modifier = modifier,
            shape = shape,
            color = solidColor,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            ),
            tonalElevation = 3.dp,
            shadowElevation = 6.dp
        ) {
            Box(content = content)
        }
        return
    }

    val isMaximum = liquidGlassLevel == LiquidGlassLevel.MAXIMUM
    val tintColor = if (isMaximum) {
        if (isDark) solidColor.copy(alpha = 0.03f) else Color.White.copy(alpha = 0.03f)
    } else {
        if (isDark) solidColor.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.30f)
    }
    val fallbackColor = if (isMaximum) {
        if (isDark) solidColor.copy(alpha = 0.28f) else Color.White.copy(alpha = 0.30f)
    } else {
        if (isDark) solidColor.copy(alpha = 0.68f) else solidColor.copy(alpha = 0.72f)
    }

    val hazeStyle = HazeStyle(
        backgroundColor = Color.Transparent,
        tints = listOf(HazeTint(tintColor)),
        blurRadius = if (isMaximum) 5.dp else 15.dp,
        noiseFactor = if (isMaximum) 0.01f else 0.03f,
        fallbackTint = HazeTint(fallbackColor)
    )

    val outerRimBrush = if (isMaximum) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.22f else 0.36f),
                MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.16f else 0.14f),
                MaterialTheme.colorScheme.onSurface.copy(alpha = if (isDark) 0.08f else 0.10f),
                Color.White.copy(alpha = if (isDark) 0.14f else 0.24f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.20f else 0.45f),
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.38f)
            )
        )
    }

    val innerBevelBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = if (isDark) 0.14f else 0.24f),
            MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.07f else 0.06f),
            Color.Transparent,
            Color.White.copy(alpha = if (isDark) 0.08f else 0.14f)
        )
    )

    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { cornerRadiusDp.toPx() }
    val bezelWidthPx = with(density) { bezelWidthDp.toPx() }
    val refractionPx = with(density) { refractionDp.toPx() }
    val dispersionPx = with(density) { dispersionDp.toPx() }
    val edgeHighlight = if (isDark) 0.14f else 0.20f

    val refractionModifier = if (isMaximum && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Modifier.liquidGlassRefraction(
            cornerRadiusPx = cornerRadiusPx,
            bezelWidthPx = bezelWidthPx,
            refractionPx = refractionPx,
            dispersionPx = dispersionPx,
            edgeHighlight = edgeHighlight
        )
    } else {
        Modifier
    }

    val backdropModifier = if (hazeState != null) {
        Modifier.hazeEffect(state = hazeState, style = hazeStyle)
    } else {
        Modifier.background(fallbackColor)
    }

    // Never apply Compose shadow() in MAXIMUM mode: shadow draws an opaque-assumption dark fill behind the pill.
    val containerModifier = if (isMaximum) {
        modifier.clip(shape)
    } else {
        val shadowColor = Color.Black.copy(alpha = 0.22f)
        modifier
            .shadow(
                elevation = 5.dp,
                shape = shape,
                clip = false,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
    }

    Box(modifier = containerModifier) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(refractionModifier)
                .clip(shape)
                .then(backdropModifier)
        )
        if (isMaximum) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .border(
                        width = 3.dp,
                        brush = innerBevelBrush,
                        shape = shape
                    )
            )
        }
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = if (isMaximum) 0.75.dp else 1.dp,
                    brush = outerRimBrush,
                    shape = shape
                )
        )
        content()
    }
}

@Composable
fun CircularGlassIconButton(
    icon: ImageVector,
    liquidGlassLevel: LiquidGlassLevel,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = LocalHazeState.current,
    tint: Color = MaterialTheme.colorScheme.onSurface,
    iconSize: Dp = 20.dp
) {
    val effectiveGlass = if (liquidGlassLevel == LiquidGlassLevel.MAXIMUM) {
        LiquidGlassLevel.MAXIMUM
    } else {
        LiquidGlassLevel.NONE
    }

    LiquidGlassContainer(
        liquidGlassLevel = effectiveGlass,
        shape = CircleShape,
        cornerRadiusDp = 20.dp,
        hazeState = hazeState,
        solidColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        refractionDp = 12.dp,
        dispersionDp = 2.4.dp,
        bezelWidthDp = 10.dp,
        modifier = modifier.size(40.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable(role = Role.Button, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
fun CircularBackButton(
    liquidGlassLevel: LiquidGlassLevel,
    onClick: () -> Unit,
    contentDescription: String,
    modifier: Modifier = Modifier,
    hazeState: HazeState? = LocalHazeState.current,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    CircularGlassIconButton(
        icon = Icons.AutoMirrored.Rounded.ArrowBack,
        liquidGlassLevel = liquidGlassLevel,
        onClick = onClick,
        contentDescription = contentDescription,
        modifier = modifier,
        hazeState = hazeState,
        tint = tint
    )
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun Modifier.liquidGlassRefraction(
    cornerRadiusPx: Float,
    bezelWidthPx: Float,
    refractionPx: Float,
    dispersionPx: Float,
    edgeHighlight: Float
): Modifier {
    val shader = remember { RuntimeShader(LIQUID_GLASS_SHADER_SRC) }
    var lastSize by remember { mutableStateOf(Size.Zero) }
    var lastBezel by remember { mutableStateOf(0f) }
    var lastParams by remember { mutableStateOf(Triple(0f, 0f, 0f)) }
    var cachedEffect by remember { mutableStateOf<ComposeRenderEffect?>(null) }

    return graphicsLayer {
        if (size.width < 4f || size.height < 4f) {
            renderEffect = null
            return@graphicsLayer
        }
        val currentParams = Triple(cornerRadiusPx, refractionPx, dispersionPx)
        if (cachedEffect == null || size != lastSize || currentParams != lastParams || bezelWidthPx != lastBezel) {
            shader.setFloatUniform("resolution", size.width, size.height)
            shader.setFloatUniform("cornerRadius", cornerRadiusPx)
            shader.setFloatUniform("bezelWidth", bezelWidthPx)
            shader.setFloatUniform("refraction", refractionPx)
            shader.setFloatUniform("dispersion", dispersionPx)
            shader.setFloatUniform("edgeHighlight", edgeHighlight)
            cachedEffect = RenderEffect.createRuntimeShaderEffect(shader, "contents").asComposeRenderEffect()
            lastSize = size
            lastBezel = bezelWidthPx
            lastParams = currentParams
        }
        renderEffect = cachedEffect
    }
}
