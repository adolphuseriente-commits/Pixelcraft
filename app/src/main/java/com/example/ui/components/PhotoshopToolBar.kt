package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.OpenWith
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.model.EditorTool
import com.example.ui.theme.AdobeBlue
import com.example.ui.theme.AdobeCyan
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

@Composable
fun PhotoshopToolBar(
    activeTool: EditorTool,
    onToolSelected: (EditorTool) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("photoshop_tool_bar"),
        color = StudioSurface,
        tonalElevation = 8.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ToolItem(
                tool = EditorTool.MOVE,
                icon = Icons.Default.OpenWith,
                label = "Move",
                shortcut = "V",
                isActive = activeTool == EditorTool.MOVE,
                onClick = { onToolSelected(EditorTool.MOVE) }
            )
            ToolItem(
                tool = EditorTool.BRUSH,
                icon = Icons.Default.Brush,
                label = "Brush",
                shortcut = "B",
                isActive = activeTool == EditorTool.BRUSH,
                onClick = { onToolSelected(EditorTool.BRUSH) }
            )
            ToolItem(
                tool = EditorTool.ERASER,
                icon = Icons.Default.FormatPaint,
                label = "Eraser",
                shortcut = "E",
                isActive = activeTool == EditorTool.ERASER,
                onClick = { onToolSelected(EditorTool.ERASER) }
            )
            ToolItem(
                tool = EditorTool.TEXT,
                icon = Icons.Default.TextFields,
                label = "Text",
                shortcut = "T",
                isActive = activeTool == EditorTool.TEXT,
                onClick = { onToolSelected(EditorTool.TEXT) }
            )
            ToolItem(
                tool = EditorTool.SHAPES,
                icon = Icons.Default.Category,
                label = "Shapes",
                shortcut = "U",
                isActive = activeTool == EditorTool.SHAPES,
                onClick = { onToolSelected(EditorTool.SHAPES) }
            )
            ToolItem(
                tool = EditorTool.ADJUST,
                icon = Icons.Default.Tune,
                label = "Adjust",
                shortcut = "A",
                isActive = activeTool == EditorTool.ADJUST,
                onClick = { onToolSelected(EditorTool.ADJUST) }
            )
            ToolItem(
                tool = EditorTool.FILTERS,
                icon = Icons.Default.Grain,
                label = "Filters",
                shortcut = "F",
                isActive = activeTool == EditorTool.FILTERS,
                onClick = { onToolSelected(EditorTool.FILTERS) }
            )
            ToolItem(
                tool = EditorTool.STICKERS,
                icon = Icons.Default.LocalOffer,
                label = "Stickers",
                shortcut = "G",
                isActive = activeTool == EditorTool.STICKERS,
                onClick = { onToolSelected(EditorTool.STICKERS) }
            )
            ToolItem(
                tool = EditorTool.CROP,
                icon = Icons.Default.Crop,
                label = "Canvas",
                shortcut = "C",
                isActive = activeTool == EditorTool.CROP,
                onClick = { onToolSelected(EditorTool.CROP) }
            )
        }
    }
}

@Composable
private fun ToolItem(
    tool: EditorTool,
    icon: ImageVector,
    label: String,
    shortcut: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isActive) AdobeBlue else Color.Transparent
    val contentColor = if (isActive) Color.White else TextSecondary
    val borderStroke = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, AdobeCyan.copy(alpha = 0.8f)) else null

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bg)
            .then(
                if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(8.dp)) else Modifier
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag("tool_${tool.name.lowercase()}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = tool.label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                color = contentColor,
                fontSize = 11.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = "($shortcut)",
                color = if (isActive) Color.White.copy(alpha = 0.8f) else TextTertiary,
                fontSize = 9.sp
            )
        }
    }
}
