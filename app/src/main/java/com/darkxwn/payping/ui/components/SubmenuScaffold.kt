package com.darkxwn.payping.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.darkxwn.payping.R
import com.darkxwn.payping.domain.model.LiquidGlassLevel
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState

/**
 * Shared layout for submenus with a circular back button that
 * stays inside a circle at all glass levels and refracts scrolling content in MAXIMUM mode.
 */
@Composable
fun PayPingSubmenuScreen(
    title: String,
    liquidGlassLevel: LiquidGlassLevel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
    bottomBar: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val hazeState = rememberHazeState()
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val navBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val backgroundColor = MaterialTheme.colorScheme.background

    CompositionLocalProvider(
        LocalHazeState provides hazeState,
        LocalContentColor provides MaterialTheme.colorScheme.onBackground
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(backgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(state = hazeState)
                    .background(backgroundColor)
                    .verticalScroll(rememberScrollState())
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        top = statusTop + 68.dp,
                        bottom = navBottom + if (bottomBar != null) 96.dp else 24.dp
                    )
                    .let { base -> if (bottomBar == null) base.imePadding() else base },
                content = content
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                backgroundColor.copy(alpha = if (liquidGlassLevel == LiquidGlassLevel.MAXIMUM) 0.48f else 0.96f),
                                backgroundColor.copy(alpha = if (liquidGlassLevel == LiquidGlassLevel.MAXIMUM) 0.16f else 0.78f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(
                        top = statusTop + 10.dp,
                        bottom = 12.dp,
                        start = 16.dp,
                        end = 16.dp
                    )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularBackButton(
                        liquidGlassLevel = liquidGlassLevel,
                        onClick = onNavigateBack,
                        contentDescription = stringResource(R.string.action_back),
                        hazeState = hazeState
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    actions()
                }
            }

            if (bottomBar != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    backgroundColor.copy(alpha = if (liquidGlassLevel == LiquidGlassLevel.MAXIMUM) 0.72f else 0.94f),
                                    backgroundColor
                                )
                            )
                        )
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp)
                ) {
                    bottomBar()
                }
            }
        }
    }
}
