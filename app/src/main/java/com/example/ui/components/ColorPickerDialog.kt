package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AdobeBlue
import com.example.ui.theme.AdobeCyan
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioInput
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private val STUDIO_PALETTE = listOf(
    0xFFFFFFFF, 0xFFE0E0E0, 0xFF9E9E9E, 0xFF424242, 0xFF141518, 0xFF000000,
    0xFF00C7FF, 0xFF007DFE, 0xFF3F51B5, 0xFF8A58FF, 0xFFE040FB, 0xFFFF4081,
    0xFFFF3B30, 0xFFFF5722, 0xFFFF9100, 0xFFFFB300, 0xFFFFEB3B, 0xFF00E676,
    0xFF00B0FF, 0xFF00E5FF, 0xFF1DE9B6, 0xFF76FF03, 0xFFC6FF00, 0xFFFF6E40
)

@Composable
fun ColorPickerDialog(
    initialColor: Long,
    onColorSelected: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    var red by remember { mutableFloatStateOf(((initialColor ushr 16) and 0xFF) / 255f) }
    var green by remember { mutableFloatStateOf(((initialColor ushr 8) and 0xFF) / 255f) }
    var blue by remember { mutableFloatStateOf((initialColor and 0xFF) / 255f) }
    var alpha by remember { mutableFloatStateOf(((initialColor ushr 24) and 0xFF) / 255f) }

    val currentColorLong = remember(red, green, blue, alpha) {
        val a = (alpha * 255).toInt().coerceIn(0, 255)
        val r = (red * 255).toInt().coerceIn(0, 255)
        val g = (green * 255).toInt().coerceIn(0, 255)
        val b = (blue * 255).toInt().coerceIn(0, 255)
        ((a.toLong() and 0xFFL) shl 24) or
                ((r.toLong() and 0xFFL) shl 16) or
                ((g.toLong() and 0xFFL) shl 8) or
                (b.toLong() and 0xFFL)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .testTag("color_picker_dialog"),
            color = StudioSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Photoshop Color Picker",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Preview Swatch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(currentColorLong))
                            .border(1.5.dp, StudioBorder, RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "HEX: #${(currentColorLong and 0xFFFFFFFFL).toString(16).uppercase().padStart(8, '0')}",
                            color = AdobeCyan,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "R: ${(red * 255).toInt()}  G: ${(green * 255).toInt()}  B: ${(blue * 255).toInt()}",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "Alpha: ${(alpha * 100).toInt()}%",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // RGB + Alpha Sliders
                SliderRow(label = "R", value = red, onValueChange = { red = it }, trackColor = Color(0xFFFF3B30))
                SliderRow(label = "G", value = green, onValueChange = { green = it }, trackColor = Color(0xFF00E676))
                SliderRow(label = "B", value = blue, onValueChange = { blue = it }, trackColor = Color(0xFF007DFE))
                SliderRow(label = "A", value = alpha, onValueChange = { alpha = it }, trackColor = Color.White)

                Spacer(modifier = Modifier.height(10.dp))

                // Palette Swatches
                Text("Swatches:", color = TextSecondary, fontSize = 11.sp)
                Spacer(modifier = Modifier.height(6.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.height(72.dp)
                ) {
                    items(STUDIO_PALETTE) { colorVal ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(1.dp, StudioBorder, CircleShape)
                                .clickable {
                                    red = ((colorVal ushr 16) and 0xFF) / 255f
                                    green = ((colorVal ushr 8) and 0xFF) / 255f
                                    blue = (colorVal and 0xFF) / 255f
                                }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    ElevatedButton(
                        onClick = {
                            onColorSelected(currentColorLong)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.elevatedButtonColors(containerColor = AdobeBlue, contentColor = Color.White)
                    ) {
                        Text("Apply Color", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SliderRow(label: String, value: Float, onValueChange: (Float) -> Unit, trackColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(20.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            modifier = Modifier
                .weight(1f)
                .height(24.dp),
            colors = SliderDefaults.colors(thumbColor = trackColor, activeTrackColor = trackColor)
        )
        Text(text = "${(value * 255).toInt()}", color = TextSecondary, fontSize = 10.sp, modifier = Modifier.width(28.dp))
    }
}
