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
    uniform float refraction;
    uniform float dispersion;

    float sdRoundedBox(float2 p, float2 b, float r) {
        float2 q = abs(p) - b + float2(r);
        return length(max(q, float2(0.0))) + min(max(q.x, q.y), 0.0) - r;
    }

    half4 main(float2 fragCoord) {
        float2 halfSize = resolution * 0.5;
        float safeRadius = min(cornerRadius, min(halfSize.x, halfSize.y));
        float2 p = fragCoord - halfSize;
        float dist = sdRoundedBox(p, halfSize, safeRadius);
        float edgeDepth = clamp(-dist / max(min(safeRadius, 44.0), 1.0), 0.0, 1.0);
        float rimFactor = pow(1.0 - edgeDepth, 2.3);
        float2 dir = p / max(length(p), 0.001);
        float2 baseCoord = clamp(fragCoord - dir * (rimFactor * refraction), float2(0.0), resolution - float2(1.0));
        float2 redCoord = clamp(fragCoord - dir * (rimFactor * (refraction + dispersion)), float2(0.0), resolution - float2(1.0));
        float2 blueCoord = clamp(fragCoord - dir * (rimFactor * max(refraction - dispersion, 0.0)), float2(0.0), resolution - float2(1.0));
        half4 center = contents.eval(baseCoord);
        half rCol = contents.eval(redCoord).r;
        half bCol = contents.eval(blueCoord).b;
        return half4(rCol, center.g, bCol, center.a);
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
    refractionDp: Dp = 11.dp,
    dispersionDp: Dp = 2.dp,
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
        if (isDark) solidColor.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.24f)
    } else {
        if (isDark) solidColor.copy(alpha = 0.58f) else Color.White.copy(alpha = 0.66f)
    }
    val fallbackColor = if (isMaximum) {
        if (isDark) solidColor.copy(alpha = 0.72f) else Color.White.copy(alpha = 0.76f)
    } else {
        if (isDark) solidColor.copy(alpha = 0.90f) else solidColor.copy(alpha = 0.92f)
    }

    val hazeStyle = HazeStyle(
        backgroundColor = Color.Transparent,
        tints = listOf(HazeTint(tintColor)),
        blurRadius = if (isMaximum) 24.dp else 20.dp,
        noiseFactor = if (isMaximum) 0.06f else 0.04f,
        fallbackTint = HazeTint(fallbackColor)
    )

    val borderBrush = if (isMaximum) {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.58f else 0.85f),
                MaterialTheme.colorScheme.primary.copy(alpha = if (isDark) 0.30f else 0.22f),
                Color.White.copy(alpha = if (isDark) 0.14f else 0.35f),
                Color.White.copy(alpha = if (isDark) 0.42f else 0.72f)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = if (isDark) 0.24f else 0.65f),
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            )
        )
    }

    val density = LocalDensity.current
    val cornerRadiusPx = with(density) { cornerRadiusDp.toPx() }
    val refractionPx = with(density) { refractionDp.toPx() }
    val dispersionPx = with(density) { dispersionDp.toPx() }

    val refractionModifier = if (isMaximum && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Modifier.liquidGlassRefraction(
            cornerRadiusPx = cornerRadiusPx,
            refractionPx = refractionPx,
            dispersionPx = dispersionPx
        )
    } else {
        Modifier
    }

    val backdropModifier = if (hazeState != null) {
        Modifier.hazeEffect(state = hazeState, style = hazeStyle)
    } else {
        Modifier.background(fallbackColor)
    }

    val shadowColor = Color.Black.copy(alpha = if (isMaximum) 0.14f else 0.24f)

    Box(
        modifier = modifier
            .shadow(
                elevation = if (isMaximum) 4.dp else 6.dp,
                shape = shape,
                clip = false,
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .clip(shape)
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .then(refractionModifier)
                .clip(shape)
                .then(backdropModifier)
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .border(
                    width = if (isMaximum) 1.2.dp else 1.dp,
                    brush = borderBrush,
                    shape = shape
                )
        )
        content()
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
        refractionDp = 7.dp,
        dispersionDp = 1.5.dp,
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
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun Modifier.liquidGlassRefraction(
    cornerRadiusPx: Float,
    refractionPx: Float,
    dispersionPx: Float
): Modifier {
    val shader = remember { RuntimeShader(LIQUID_GLASS_SHADER_SRC) }
    var lastSize by remember { mutableStateOf(Size.Zero) }
    var lastParams by remember { mutableStateOf(Triple(0f, 0f, 0f)) }
    var cachedEffect by remember { mutableStateOf<ComposeRenderEffect?>(null) }

    return graphicsLayer {
        if (size.width < 4f || size.height < 4f) {
            renderEffect = null
            return@graphicsLayer
        }
        val currentParams = Triple(cornerRadiusPx, refractionPx, dispersionPx)
        if (cachedEffect == null || size != lastSize || currentParams != lastParams) {
            shader.setFloatUniform("resolution", size.width, size.height)
            shader.setFloatUniform("cornerRadius", cornerRadiusPx)
            shader.setFloatUniform("refraction", refractionPx)
            shader.setFloatUniform("dispersion", dispersionPx)
            cachedEffect = RenderEffect.createRuntimeShaderEffect(shader, "contents").asComposeRenderEffect()
            lastSize = size
            lastParams = currentParams
        }
        renderEffect = cachedEffect
    }
}
