package com.darkxwn.payping.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.darkxwn.payping.R
import com.darkxwn.payping.domain.model.BaseAccentColor

@Composable
fun ColorPickerDialog(
    initialColorArgb: Long,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit,
    confirmTextRes: Int = R.string.color_picker_add
) {
    val initialHsv = remember(initialColorArgb) {
        FloatArray(3).apply {
            android.graphics.Color.colorToHSV(
                (BaseAccentColor.normalizeOpaqueArgb(initialColorArgb) and 0xFFFFFFFFL).toInt(),
                this
            )
        }
    }

    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember { mutableFloatStateOf(initialHsv[2]) }

    fun currentArgb(): Long =
        BaseAccentColor.normalizeOpaqueArgb(
            android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, brightness)).toLong()
        )

    var hexInput by remember { mutableStateOf(BaseAccentColor.formatHex(currentArgb())) }

    val parsedHexArgb = remember(hexInput) { BaseAccentColor.parseHex(hexInput) }
    val previewArgb = parsedHexArgb ?: currentArgb()
    val previewColor = Color(previewArgb)

    fun syncHexFromHsv() {
        hexInput = BaseAccentColor.formatHex(currentArgb())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.color_picker_title),
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(previewColor)
                            .border(
                                width = 1.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant,
                                shape = CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = BaseAccentColor.formatHex(previewArgb),
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Monospace),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                SaturationValuePanel(
                    hue = hue,
                    saturation = saturation,
                    value = brightness,
                    onChanged = { newSat, newVal ->
                        saturation = newSat
                        brightness = newVal
                        syncHexFromHsv()
                    }
                )

                HueSliderBar(
                    hue = hue,
                    onHueChanged = { newHue ->
                        hue = newHue
                        syncHexFromHsv()
                    }
                )

                OutlinedTextField(
                    value = hexInput,
                    onValueChange = { raw ->
                        val filtered = raw.uppercase()
                            .filterIndexed { index, ch ->
                                (index == 0 && ch == '#') || ch in '0'..'9' || ch in 'A'..'F'
                            }
                            .take(7)
                        hexInput = filtered
                        BaseAccentColor.parseHex(filtered)?.let { argb ->
                            val hsv = FloatArray(3)
                            android.graphics.Color.colorToHSV((argb and 0xFFFFFFFFL).toInt(), hsv)
                            hue = hsv[0]
                            saturation = hsv[1]
                            brightness = hsv[2]
                        }
                    },
                    label = { Text(stringResource(R.string.color_picker_hex_label)) },
                    singleLine = true,
                    isError = parsedHexArgb == null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontFamily = FontFamily.Monospace),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { parsedHexArgb?.let(onConfirm) },
                enabled = parsedHexArgb != null
            ) {
                Text(stringResource(confirmTextRes))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.action_cancel))
            }
        }
    )
}

@Composable
private fun SaturationValuePanel(
    hue: Float,
    saturation: Float,
    value: Float,
    onChanged: (Float, Float) -> Unit
) {
    val hueColor = Color(android.graphics.Color.HSVToColor(floatArrayOf(hue, 1f, 1f)))

    fun updateFromOffset(offset: Offset, width: Float, height: Float) {
        if (width <= 0f || height <= 0f) return
        val newSat = (offset.x / width).coerceIn(0f, 1f)
        val newVal = (1f - offset.y / height).coerceIn(0f, 1f)
        onChanged(newSat, newVal)
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(14.dp))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    updateFromOffset(offset, size.width.toFloat(), size.height.toFloat())
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    updateFromOffset(change.position, size.width.toFloat(), size.height.toFloat())
                }
            }
    ) {
        drawRoundRect(
            brush = Brush.horizontalGradient(listOf(Color.White, hueColor)),
            cornerRadius = CornerRadius(14.dp.toPx())
        )
        drawRoundRect(
            brush = Brush.verticalGradient(listOf(Color.Transparent, Color.Black)),
            cornerRadius = CornerRadius(14.dp.toPx())
        )
        val center = Offset(
            x = saturation.coerceIn(0f, 1f) * size.width,
            y = (1f - brightnessClamp(value)) * size.height
        )
        drawCircle(
            color = Color.Black.copy(alpha = 0.45f),
            radius = 10.dp.toPx(),
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
        drawCircle(
            color = Color.White,
            radius = 9.dp.toPx(),
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}

private fun brightnessClamp(value: Float): Float = value.coerceIn(0f, 1f)

@Composable
private fun HueSliderBar(
    hue: Float,
    onHueChanged: (Float) -> Unit
) {
    val hueColors = remember {
        listOf(
            Color(0xFFFF0000),
            Color(0xFFFFFF00),
            Color(0xFF00FF00),
            Color(0xFF00FFFF),
            Color(0xFF0000FF),
            Color(0xFFFF00FF),
            Color(0xFFFF0000)
        )
    }

    fun updateHue(x: Float, width: Float) {
        if (width <= 0f) return
        onHueChanged(((x / width).coerceIn(0f, 1f)) * 360f)
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(26.dp)
            .clip(RoundedCornerShape(13.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(13.dp))
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    updateHue(offset.x, size.width.toFloat())
                }
            }
            .pointerInput(Unit) {
                detectDragGestures { change, _ ->
                    change.consume()
                    updateHue(change.position.x, size.width.toFloat())
                }
            }
    ) {
        drawRoundRect(
            brush = Brush.horizontalGradient(hueColors),
            cornerRadius = CornerRadius(13.dp.toPx())
        )
        val x = (hue.coerceIn(0f, 360f) / 360f) * size.width
        val center = Offset(x = x.coerceIn(10.dp.toPx(), size.width - 10.dp.toPx()), y = size.height / 2f)
        drawCircle(
            color = Color.Black.copy(alpha = 0.45f),
            radius = 10.dp.toPx(),
            center = center,
            style = Stroke(width = 3.dp.toPx())
        )
        drawCircle(
            color = Color.White,
            radius = 9.dp.toPx(),
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )
    }
}
