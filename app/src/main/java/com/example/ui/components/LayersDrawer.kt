package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CanvasBlendMode
import com.example.model.DrawingLayer
import com.example.model.ImageLayer
import com.example.model.Layer
import com.example.model.ShapeLayer
import com.example.model.StickerLayer
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

@Composable
fun LayersDrawer(
    layers: List<Layer>,
    selectedLayerId: String?,
    onSelectLayer: (String) -> Unit,
    onToggleVisibility: (String) -> Unit,
    onToggleLock: (String) -> Unit,
    onReorderLayer: (String, Boolean) -> Unit,
    onDeleteLayer: (String) -> Unit,
    onDuplicateLayer: (String) -> Unit,
    onSetOpacity: (String, Float) -> Unit,
    onFinishOpacity: () -> Unit,
    onSetBlendMode: (String, CanvasBlendMode) -> Unit,
    onAddText: () -> Unit,
    onAddShape: () -> Unit,
    onAddDrawing: () -> Unit,
    onAddSticker: () -> Unit,
    onPickImage: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedLayer = layers.find { it.id == selectedLayerId }
    var blendMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .width(310.dp)
            .testTag("layers_palette_drawer"),
        color = StudioSurface,
        tonalElevation = 12.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Layers",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${layers.size})",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Layers Drawer",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Selected Layer Properties (Opacity + Blend Mode)
            if (selectedLayer != null) {
                Surface(
                    color = StudioCard,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Opacity",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Text(
                                text = "${(selectedLayer.opacity * 100).toInt()}%",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Slider(
                            value = selectedLayer.opacity,
                            onValueChange = { onSetOpacity(selectedLayer.id, it) },
                            onValueChangeFinished = onFinishOpacity,
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = AdobeCyan,
                                activeTrackColor = AdobeBlue,
                                inactiveTrackColor = StudioInput
                            ),
                            modifier = Modifier.height(26.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Blend Mode",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            Box {
                                Surface(
                                    color = StudioInput,
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder),
                                    modifier = Modifier
                                        .clickable { blendMenuExpanded = true }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = selectedLayer.blendMode.displayName,
                                        color = AdobeCyan,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                DropdownMenu(
                                    expanded = blendMenuExpanded,
                                    onDismissRequest = { blendMenuExpanded = false }
                                ) {
                                    CanvasBlendMode.values().forEach { mode ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = mode.displayName,
                                                    color = if (mode == selectedLayer.blendMode) AdobeCyan else TextPrimary,
                                                    fontSize = 13.sp
                                                )
                                            },
                                            onClick = {
                                                onSetBlendMode(selectedLayer.id, mode)
                                                blendMenuExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = StudioBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Layers List (reversed so top layers show at top!)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // We show layers in reverse order (stack order)
                val reversedLayers = layers.asReversed()
                itemsIndexed(reversedLayers, key = { _, layer -> layer.id }) { index, layer ->
                    val isSelected = layer.id == selectedLayerId
                    LayerRowItem(
                        layer = layer,
                        isSelected = isSelected,
                        canMoveUp = index > 0, // In reversed list, moving up in stack means moving towards index 0
                        canMoveDown = index < reversedLayers.size - 1,
                        onSelect = { onSelectLayer(layer.id) },
                        onToggleVisibility = { onToggleVisibility(layer.id) },
                        onToggleLock = { onToggleLock(layer.id) },
                        onMoveUp = { onReorderLayer(layer.id, true) },
                        onMoveDown = { onReorderLayer(layer.id, false) },
                        onDelete = { onDeleteLayer(layer.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = StudioBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Add Layer Action Strip
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Add Layer",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = onPickImage,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("add_photo_layer_button"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = StudioCard)
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add Photo", modifier = Modifier.size(16.dp), tint = AdobeCyan)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Photo", fontSize = 11.sp, color = TextPrimary)
                    }

                    FilledTonalButton(
                        onClick = onAddText,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("add_text_layer_button"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = StudioCard)
                    ) {
                        Icon(Icons.Default.TextFields, contentDescription = "Add Text", modifier = Modifier.size(16.dp), tint = AdobeBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Text", fontSize = 11.sp, color = TextPrimary)
                    }

                    FilledTonalButton(
                        onClick = onAddShape,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("add_shape_layer_button"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = StudioCard)
                    ) {
                        Icon(Icons.Default.Category, contentDescription = "Add Shape", modifier = Modifier.size(16.dp), tint = Color(0xFFFFB300))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Shape", fontSize = 11.sp, color = TextPrimary)
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilledTonalButton(
                        onClick = onAddDrawing,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("add_brush_layer_button"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = StudioCard)
                    ) {
                        Icon(Icons.Default.Brush, contentDescription = "Add Brush Layer", modifier = Modifier.size(16.dp), tint = Color(0xFF00E676))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Brush", fontSize = 11.sp, color = TextPrimary)
                    }

                    FilledTonalButton(
                        onClick = onAddSticker,
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("add_sticker_layer_button"),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp),
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = StudioCard)
                    ) {
                        Icon(Icons.Default.LocalOffer, contentDescription = "Add Graphic Stamp", modifier = Modifier.size(16.dp), tint = Color(0xFFFF4081))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stamp", fontSize = 11.sp, color = TextPrimary)
                    }

                    if (selectedLayer != null) {
                        OutlinedButton(
                            onClick = { onDuplicateLayer(selectedLayer.id) },
                            modifier = Modifier
                                .weight(1f)
                                .height(34.dp)
                                .testTag("duplicate_layer_button"),
                            shape = RoundedCornerShape(6.dp),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate Layer", modifier = Modifier.size(14.dp), tint = TextSecondary)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontSize = 11.sp, color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LayerRowItem(
    layer: Layer,
    isSelected: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onSelect: () -> Unit,
    onToggleVisibility: () -> Unit,
    onToggleLock: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit
) {
    val bgColor = if (isSelected) StudioCard else StudioInput
    val borderStroke = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, AdobeCyan) else androidx.compose.foundation.BorderStroke(1.dp, StudioBorder.copy(alpha = 0.5f))

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
        border = borderStroke,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .testTag("layer_row_${layer.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Visibility Eye Toggle
            IconButton(
                onClick = onToggleVisibility,
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = if (layer.isVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                    contentDescription = if (layer.isVisible) "Hide Layer" else "Show Layer",
                    tint = if (layer.isVisible) AdobeCyan else TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }

            // Layer Icon
            val layerIcon = when (layer) {
                is ImageLayer -> Icons.Default.Image
                is TextLayer -> Icons.Default.TextFields
                is ShapeLayer -> Icons.Default.Category
                is DrawingLayer -> Icons.Default.Brush
                is StickerLayer -> Icons.Default.LocalOffer
            }
            Icon(
                imageVector = layerIcon,
                contentDescription = null,
                tint = if (isSelected) AdobeBlue else TextSecondary,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Layer Name
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = layer.name,
                    color = if (layer.isVisible) TextPrimary else TextTertiary,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${layer.blendMode.displayName} • ${(layer.opacity * 100).toInt()}%",
                    color = TextTertiary,
                    fontSize = 10.sp
                )
            }

            // Up / Down stack buttons
            IconButton(
                onClick = onMoveUp,
                enabled = canMoveUp,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = "Bring Forward",
                    tint = if (canMoveUp) TextSecondary else TextTertiary.copy(alpha = 0.3f),
                    modifier = Modifier.size(14.dp)
                )
            }

            IconButton(
                onClick = onMoveDown,
                enabled = canMoveDown,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = "Send Backward",
                    tint = if (canMoveDown) TextSecondary else TextTertiary.copy(alpha = 0.3f),
                    modifier = Modifier.size(14.dp)
                )
            }

            // Lock toggle
            IconButton(
                onClick = onToggleLock,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = if (layer.isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = if (layer.isLocked) "Unlock Layer" else "Lock Layer",
                    tint = if (layer.isLocked) Color(0xFFFFB300) else TextTertiary,
                    modifier = Modifier.size(14.dp)
                )
            }

            // Delete
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Layer",
                    tint = TextTertiary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
