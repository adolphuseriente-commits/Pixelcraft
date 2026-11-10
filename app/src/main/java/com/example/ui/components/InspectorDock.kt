package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlignHorizontalCenter
import androidx.compose.material.icons.filled.AlignHorizontalLeft
import androidx.compose.material.icons.filled.AlignHorizontalRight
import androidx.compose.material.icons.filled.AlignVerticalBottom
import androidx.compose.material.icons.filled.AlignVerticalCenter
import androidx.compose.material.icons.filled.AlignVerticalTop
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RotateRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CanvasPreset
import com.example.model.EditorTool
import com.example.model.FilterPreset
import com.example.model.ImageLayer
import com.example.model.Layer
import com.example.model.STANDARD_CANVAS_PRESETS
import com.example.model.ShapeLayer
import com.example.model.ShapeType
import com.example.model.StickerLayer
import com.example.model.TextFontFamily
import com.example.model.TextLayer
import com.example.ui.theme.AdobeBlue
import com.example.ui.theme.AdobeCyan
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioInput
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import com.example.viewmodel.CanvasAlignment
import com.example.viewmodel.EditorUiState

private val PRESET_SWATCHES = listOf(
    0xFFFFFFFF, // White
    0xFF12141A, // Deep Studio Black
    0xFF00C7FF, // Adobe Cyan
    0xFF007DFE, // Adobe Blue
    0xFF00E676, // Neon Green
    0xFFFFB300, // Amber Yellow
    0xFFFF3B30, // Red
    0xFFFF4081, // Hot Pink
    0xFF8A58FF, // Purple
    0xFFFF9100  // Orange
)

@Composable
fun InspectorDock(
    uiState: EditorUiState,
    selectedLayer: Layer?,
    onAlignLayer: (CanvasAlignment) -> Unit,
    onFlipLayer: (Boolean) -> Unit,
    onNudgeLayer: (Float, Float) -> Unit,
    onRotateLayer: (Float) -> Unit,
    onSetBrushSize: (Float) -> Unit,
    onSetBrushColor: (Long) -> Unit,
    onSetBrushAlpha: (Float) -> Unit,
    onOpenColorPicker: (String) -> Unit,
    onOpenTextEditor: () -> Unit,
    onUpdateTextFont: (TextFontFamily) -> Unit,
    onToggleTextBold: () -> Unit,
    onToggleTextItalic: () -> Unit,
    onToggleTextBackground: () -> Unit,
    onSetTextFontSize: (Float) -> Unit,
    onSelectShapeType: (ShapeType) -> Unit,
    onSetShapeStrokeWidth: (Float) -> Unit,
    onSetShapeCornerRadius: (Float) -> Unit,
    onUpdateImageAdjustments: (Float?, Float?, Float?, Float?, Float?, FilterPreset?) -> Unit,
    onResetAdjustments: () -> Unit,
    onAddSticker: (String, String) -> Unit,
    onSetCanvasPreset: (CanvasPreset) -> Unit,
    onSetCanvasBg: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("inspector_dock"),
        color = StudioSurface,
        tonalElevation = 10.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            when (uiState.activeTool) {
                EditorTool.MOVE -> {
                    MoveInspector(
                        selectedLayer = selectedLayer,
                        onAlign = onAlignLayer,
                        onFlip = onFlipLayer,
                        onNudge = onNudgeLayer,
                        onRotate = onRotateLayer
                    )
                }
                EditorTool.BRUSH -> {
                    BrushInspector(
                        brushSize = uiState.brushSize,
                        brushColor = uiState.brushColor,
                        brushAlpha = uiState.brushAlpha,
                        onSizeChange = onSetBrushSize,
                        onColorChange = onSetBrushColor,
                        onAlphaChange = onSetBrushAlpha,
                        onOpenColorPicker = { onOpenColorPicker("brush") }
                    )
                }
                EditorTool.ERASER -> {
                    EraserInspector(
                        eraserSize = uiState.brushSize,
                        onSizeChange = onSetBrushSize
                    )
                }
                EditorTool.TEXT -> {
                    TextInspector(
                        selectedTextLayer = selectedLayer as? TextLayer,
                        onOpenEditor = onOpenTextEditor,
                        onFontChange = onUpdateTextFont,
                        onToggleBold = onToggleTextBold,
                        onToggleItalic = onToggleTextItalic,
                        onToggleBg = onToggleTextBackground,
                        onFontSizeChange = onSetTextFontSize,
                        onOpenColorPicker = { onOpenColorPicker("textColor") }
                    )
                }
                EditorTool.SHAPES -> {
                    ShapesInspector(
                        selectedShapeLayer = selectedLayer as? ShapeLayer,
                        onSelectShape = onSelectShapeType,
                        onStrokeWidthChange = onSetShapeStrokeWidth,
                        onCornerRadiusChange = onSetShapeCornerRadius,
                        onOpenFillPicker = { onOpenColorPicker("shapeFill") },
                        onOpenStrokePicker = { onOpenColorPicker("shapeStroke") }
                    )
                }
                EditorTool.ADJUST -> {
                    AdjustmentsInspector(
                        imageLayer = selectedLayer as? ImageLayer,
                        onAdjust = onUpdateImageAdjustments,
                        onReset = onResetAdjustments
                    )
                }
                EditorTool.FILTERS -> {
                    FiltersInspector(
                        imageLayer = selectedLayer as? ImageLayer,
                        onSelectFilter = { preset ->
                            onUpdateImageAdjustments(null, null, null, null, null, preset)
                        }
                    )
                }
                EditorTool.STICKERS -> {
                    StickersInspector(
                        onAddSticker = onAddSticker
                    )
                }
                EditorTool.CROP -> {
                    CanvasCropInspector(
                        currentPreset = uiState.canvasPreset,
                        currentBg = uiState.backgroundColor,
                        onSelectPreset = onSetCanvasPreset,
                        onSelectBg = onSetCanvasBg
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TOOL SPECIFIC INSPECTOR PANELS
// -------------------------------------------------------------

@Composable
private fun MoveInspector(
    selectedLayer: Layer?,
    onAlign: (CanvasAlignment) -> Unit,
    onFlip: (Boolean) -> Unit,
    onNudge: (Float, Float) -> Unit,
    onRotate: (Float) -> Unit
) {
    if (selectedLayer == null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Tap any layer on canvas to select and transform",
                color = TextSecondary,
                fontSize = 13.sp
            )
        }
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Selected: ${selectedLayer.name}",
                color = AdobeCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalIconButton(
                    onClick = { onFlip(true) },
                    modifier = Modifier.size(30.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = StudioCard)
                ) {
                    Icon(Icons.Default.Flip, contentDescription = "Flip Horizontal", modifier = Modifier.size(16.dp), tint = TextPrimary)
                }
                FilledTonalIconButton(
                    onClick = { onRotate(90f) },
                    modifier = Modifier.size(30.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = StudioCard)
                ) {
                    Icon(Icons.Default.RotateRight, contentDescription = "Rotate 90 Degrees", modifier = Modifier.size(16.dp), tint = TextPrimary)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Alignment & Nudge Strip
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Align:", color = TextTertiary, fontSize = 11.sp)

            IconButton(onClick = { onAlign(CanvasAlignment.LEFT) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.AlignHorizontalLeft, "Align Left", modifier = Modifier.size(18.dp), tint = TextSecondary)
            }
            IconButton(onClick = { onAlign(CanvasAlignment.CENTER_X) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.AlignHorizontalCenter, "Center X", modifier = Modifier.size(18.dp), tint = AdobeCyan)
            }
            IconButton(onClick = { onAlign(CanvasAlignment.RIGHT) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.AlignHorizontalRight, "Align Right", modifier = Modifier.size(18.dp), tint = TextSecondary)
            }
            IconButton(onClick = { onAlign(CanvasAlignment.TOP) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.AlignVerticalTop, "Align Top", modifier = Modifier.size(18.dp), tint = TextSecondary)
            }
            IconButton(onClick = { onAlign(CanvasAlignment.CENTER_Y) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.AlignVerticalCenter, "Center Y", modifier = Modifier.size(18.dp), tint = AdobeCyan)
            }
            IconButton(onClick = { onAlign(CanvasAlignment.BOTTOM) }, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.AlignVerticalBottom, "Align Bottom", modifier = Modifier.size(18.dp), tint = TextSecondary)
            }

            Spacer(modifier = Modifier.width(6.dp))
            Text("Nudge:", color = TextTertiary, fontSize = 11.sp)

            IconButton(onClick = { onNudge(-10f, 0f) }, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.KeyboardArrowLeft, "Nudge Left", modifier = Modifier.size(18.dp), tint = TextPrimary)
            }
            IconButton(onClick = { onNudge(10f, 0f) }, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.KeyboardArrowRight, "Nudge Right", modifier = Modifier.size(18.dp), tint = TextPrimary)
            }
            IconButton(onClick = { onNudge(0f, -10f) }, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.KeyboardArrowUp, "Nudge Up", modifier = Modifier.size(18.dp), tint = TextPrimary)
            }
            IconButton(onClick = { onNudge(0f, 10f) }, modifier = Modifier.size(30.dp)) {
                Icon(Icons.Default.KeyboardArrowDown, "Nudge Down", modifier = Modifier.size(18.dp), tint = TextPrimary)
            }
        }
    }
}

@Composable
private fun BrushInspector(
    brushSize: Float,
    brushColor: Long,
    brushAlpha: Float,
    onSizeChange: (Float) -> Unit,
    onColorChange: (Long) -> Unit,
    onAlphaChange: (Float) -> Unit,
    onOpenColorPicker: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Size: ${brushSize.toInt()}px", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(70.dp))
            Slider(
                value = brushSize,
                onValueChange = onSizeChange,
                valueRange = 2f..80f,
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp),
                colors = SliderDefaults.colors(thumbColor = AdobeCyan, activeTrackColor = AdobeBlue)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Opacity: ${(brushAlpha * 100).toInt()}%", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(76.dp))
            Slider(
                value = brushAlpha,
                onValueChange = onAlphaChange,
                valueRange = 0.1f..1f,
                modifier = Modifier
                    .weight(1f)
                    .height(26.dp),
                colors = SliderDefaults.colors(thumbColor = AdobeCyan, activeTrackColor = AdobeBlue)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Color Swatches
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenColorPicker,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(Icons.Default.ColorLens, "Custom Color", tint = AdobeCyan, modifier = Modifier.size(20.dp))
            }

            PRESET_SWATCHES.forEach { colorVal ->
                val isSelected = (brushColor == colorVal)
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(colorVal))
                        .border(
                            width = if (isSelected) 2.5.dp else 1.dp,
                            color = if (isSelected) AdobeCyan else StudioBorder,
                            shape = CircleShape
                        )
                        .clickable { onColorChange(colorVal) }
                )
            }
        }
    }
}

@Composable
private fun EraserInspector(
    eraserSize: Float,
    onSizeChange: (Float) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Eraser Radius: ${eraserSize.toInt()}px", color = TextSecondary, fontSize = 12.sp, modifier = Modifier.width(130.dp))
        Slider(
            value = eraserSize,
            onValueChange = onSizeChange,
            valueRange = 6f..120f,
            modifier = Modifier
                .weight(1f)
                .height(28.dp),
            colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = TextSecondary)
        )
    }
}

@Composable
private fun TextInspector(
    selectedTextLayer: TextLayer?,
    onOpenEditor: () -> Unit,
    onFontChange: (TextFontFamily) -> Unit,
    onToggleBold: () -> Unit,
    onToggleItalic: () -> Unit,
    onToggleBg: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onOpenColorPicker: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ElevatedButton(
                onClick = onOpenEditor,
                shape = RoundedCornerShape(6.dp),
                modifier = Modifier.height(32.dp),
                colors = ButtonDefaults.elevatedButtonColors(containerColor = AdobeBlue, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 10.dp)
            ) {
                Icon(Icons.Default.Edit, "Edit Text", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Edit Content", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }

            // Quick Format Toggles
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                FilledTonalIconButton(
                    onClick = onToggleBold,
                    modifier = Modifier.size(30.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (selectedTextLayer?.isBold == true) AdobeBlue else StudioCard
                    )
                ) {
                    Icon(Icons.Default.FormatBold, "Bold", modifier = Modifier.size(16.dp), tint = TextPrimary)
                }
                FilledTonalIconButton(
                    onClick = onToggleItalic,
                    modifier = Modifier.size(30.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (selectedTextLayer?.isItalic == true) AdobeBlue else StudioCard
                    )
                ) {
                    Icon(Icons.Default.FormatItalic, "Italic", modifier = Modifier.size(16.dp), tint = TextPrimary)
                }
                FilledTonalButton(
                    onClick = onToggleBg,
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (selectedTextLayer?.hasBackground == true) AdobeCyan else StudioCard
                    )
                ) {
                    Text("Badge", fontSize = 10.sp, color = if (selectedTextLayer?.hasBackground == true) Color.Black else TextPrimary)
                }
                FilledTonalIconButton(
                    onClick = onOpenColorPicker,
                    modifier = Modifier.size(30.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = StudioCard)
                ) {
                    Icon(Icons.Default.ColorLens, "Text Color", modifier = Modifier.size(16.dp), tint = AdobeCyan)
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Font Families chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Font:", color = TextTertiary, fontSize = 11.sp)
            TextFontFamily.values().forEach { family ->
                val isSelected = selectedTextLayer?.fontFamily == family
                Surface(
                    color = if (isSelected) AdobeBlue else StudioCard,
                    shape = RoundedCornerShape(6.dp),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AdobeCyan) else null,
                    modifier = Modifier
                        .clickable { onFontChange(family) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = family.displayName,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}

@Composable
private fun ShapesInspector(
    selectedShapeLayer: ShapeLayer?,
    onSelectShape: (ShapeType) -> Unit,
    onStrokeWidthChange: (Float) -> Unit,
    onCornerRadiusChange: (Float) -> Unit,
    onOpenFillPicker: () -> Unit,
    onOpenStrokePicker: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ShapeType.values().forEach { shape ->
                val isSelected = selectedShapeLayer?.shapeType == shape
                Surface(
                    color = if (isSelected) AdobeBlue else StudioCard,
                    shape = RoundedCornerShape(6.dp),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, AdobeCyan) else null,
                    modifier = Modifier
                        .clickable { onSelectShape(shape) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = shape.displayName,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else TextSecondary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalButton(
                    onClick = onOpenFillPicker,
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = StudioCard)
                ) {
                    Text("Fill Color", fontSize = 11.sp, color = TextPrimary)
                }
                FilledTonalButton(
                    onClick = onOpenStrokePicker,
                    modifier = Modifier.height(30.dp),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = StudioCard)
                ) {
                    Text("Stroke Color", fontSize = 11.sp, color = AdobeCyan)
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Stroke: ${(selectedShapeLayer?.strokeWidth ?: 4f).toInt()}px", color = TextSecondary, fontSize = 11.sp)
                Slider(
                    value = selectedShapeLayer?.strokeWidth ?: 4f,
                    onValueChange = onStrokeWidthChange,
                    valueRange = 0f..24f,
                    modifier = Modifier
                        .width(110.dp)
                        .height(26.dp),
                    colors = SliderDefaults.colors(thumbColor = AdobeCyan, activeTrackColor = AdobeBlue)
                )
            }
        }
    }
}

@Composable
private fun AdjustmentsInspector(
    imageLayer: ImageLayer?,
    onAdjust: (Float?, Float?, Float?, Float?, Float?, FilterPreset?) -> Unit,
    onReset: () -> Unit
) {
    if (imageLayer == null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Select an Image Layer to adjust Brightness, Contrast & Tone", color = TextSecondary, fontSize = 12.sp)
        }
        return
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Photoshop Color Adjustments", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            IconButton(onClick = onReset, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Refresh, "Reset Adjustments", modifier = Modifier.size(16.dp), tint = TextSecondary)
            }
        }

        // Brightness & Contrast
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Bright: ${(imageLayer.brightness * 100).toInt()}%", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(76.dp))
            Slider(
                value = imageLayer.brightness,
                onValueChange = { onAdjust(it, null, null, null, null, null) },
                valueRange = -1f..1f,
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp),
                colors = SliderDefaults.colors(thumbColor = AdobeCyan, activeTrackColor = AdobeBlue)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contrast: ${(imageLayer.contrast * 100).toInt()}%", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(80.dp))
            Slider(
                value = imageLayer.contrast,
                onValueChange = { onAdjust(null, it, null, null, null, null) },
                valueRange = 0.2f..2.5f,
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp),
                colors = SliderDefaults.colors(thumbColor = AdobeCyan, activeTrackColor = AdobeBlue)
            )
        }

        // Saturation & Hue
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Sat: ${(imageLayer.saturation * 100).toInt()}%", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(76.dp))
            Slider(
                value = imageLayer.saturation,
                onValueChange = { onAdjust(null, null, it, null, null, null) },
                valueRange = 0f..2.5f,
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp),
                colors = SliderDefaults.colors(thumbColor = AdobeCyan, activeTrackColor = AdobeBlue)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Hue: ${imageLayer.hue.toInt()}°", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.width(80.dp))
            Slider(
                value = imageLayer.hue,
                onValueChange = { onAdjust(null, null, null, it, null, null) },
                valueRange = -180f..180f,
                modifier = Modifier
                    .weight(1f)
                    .height(24.dp),
                colors = SliderDefaults.colors(thumbColor = Color(0xFFFF4081), activeTrackColor = AdobeBlue)
            )
        }
    }
}

@Composable
private fun FiltersInspector(
    imageLayer: ImageLayer?,
    onSelectFilter: (FilterPreset) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Creative Filter LUTs", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPreset.values().forEach { preset ->
                val isSelected = imageLayer?.filterPreset == preset
                Surface(
                    color = if (isSelected) AdobeBlue else StudioCard,
                    shape = RoundedCornerShape(8.dp),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AdobeCyan) else androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                    modifier = Modifier
                        .clickable { onSelectFilter(preset) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset.displayName,
                        fontSize = 11.sp,
                        color = if (isSelected) Color.White else TextPrimary,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun StickersInspector(
    onAddSticker: (String, String) -> Unit
) {
    val stamps = listOf(
        Pair("badge_verified", "Verified Badge"),
        Pair("badge_sale", "50% Off Sale"),
        Pair("badge_new", "New Arrival"),
        Pair("badge_pro", "Pro Studio"),
        Pair("badge_limited", "Limited Edition")
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Graphic Stamps & Badges", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            stamps.forEach { (id, title) ->
                FilledTonalButton(
                    onClick = { onAddSticker(id, title) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = StudioCard),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text("+ $title", fontSize = 11.sp, color = AdobeCyan, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun CanvasCropInspector(
    currentPreset: CanvasPreset,
    currentBg: Long?,
    onSelectPreset: (CanvasPreset) -> Unit,
    onSelectBg: (Long?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Canvas Size & Aspect Ratio", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            STANDARD_CANVAS_PRESETS.forEach { preset ->
                val isSelected = currentPreset.name == preset.name
                Surface(
                    color = if (isSelected) AdobeBlue else StudioCard,
                    shape = RoundedCornerShape(8.dp),
                    border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AdobeCyan) else androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                    modifier = Modifier
                        .clickable { onSelectPreset(preset) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = preset.name,
                            fontSize = 11.sp,
                            color = if (isSelected) Color.White else TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = preset.label,
                            fontSize = 9.sp,
                            color = if (isSelected) Color.White.copy(alpha = 0.8f) else TextTertiary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Background Color Selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Canvas Backdrop:", color = TextSecondary, fontSize = 11.sp)

            // Transparent
            Surface(
                color = StudioCard,
                shape = RoundedCornerShape(6.dp),
                border = if (currentBg == null) androidx.compose.foundation.BorderStroke(1.5.dp, AdobeCyan) else null,
                modifier = Modifier
                    .clickable { onSelectBg(null) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Transparent Grid", fontSize = 10.sp, color = if (currentBg == null) AdobeCyan else TextSecondary)
            }

            // Dark
            Surface(
                color = StudioCard,
                shape = RoundedCornerShape(6.dp),
                border = if (currentBg == 0xFF12141AL) androidx.compose.foundation.BorderStroke(1.5.dp, AdobeCyan) else null,
                modifier = Modifier
                    .clickable { onSelectBg(0xFF12141A) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Dark Studio", fontSize = 10.sp, color = if (currentBg == 0xFF12141AL) AdobeCyan else TextSecondary)
            }

            // White
            Surface(
                color = StudioCard,
                shape = RoundedCornerShape(6.dp),
                border = if (currentBg == 0xFFFFFFFFL) androidx.compose.foundation.BorderStroke(1.5.dp, AdobeCyan) else null,
                modifier = Modifier
                    .clickable { onSelectBg(0xFFFFFFFF) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Clean White", fontSize = 10.sp, color = if (currentBg == 0xFFFFFFFFL) AdobeCyan else TextSecondary)
            }
        }
    }
}
