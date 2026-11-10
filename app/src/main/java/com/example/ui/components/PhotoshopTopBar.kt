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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AdobeBlue
import com.example.ui.theme.AdobeCyan
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCard
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary

import com.google.firebase.auth.FirebaseUser
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue

@Composable
fun PhotoshopTopBar(
    layerCount: Int,
    canUndo: Boolean,
    canRedo: Boolean,
    zoomScale: Float,
    isLayersPanelOpen: Boolean,
    currentUser: FirebaseUser? = null,
    cloudProjectCount: Int = 0,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onResetZoom: () -> Unit,
    onToggleLayers: () -> Unit,
    onOpenTemplates: () -> Unit,
    onOpenAuth: () -> Unit,
    onOpenCloudProjects: () -> Unit,
    onExportClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("photoshop_top_bar"),
        color = StudioSurface,
        tonalElevation = 6.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, StudioBorder.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand Logo & Project Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenTemplates() }
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(AdobeBlue, AdobeCyan)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ps",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Photocraft",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Studio Project",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            // Undo / Redo & Fit Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("undo_button"),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = TextPrimary,
                        disabledContentColor = TextTertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo Action",
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("redo_button"),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = TextPrimary,
                        disabledContentColor = TextTertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo Action",
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Fit Canvas Button
                FilledTonalIconButton(
                    onClick = onResetZoom,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("fit_canvas_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = StudioCard,
                        contentColor = if (zoomScale != 1.0f) AdobeCyan else TextSecondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.CropFree,
                        contentDescription = "Fit Canvas to Screen",
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Starter Templates Button
                FilledTonalIconButton(
                    onClick = onOpenTemplates,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("templates_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = StudioCard,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.DashboardCustomize,
                        contentDescription = "Templates and Presets",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Cloud Sync, Layers Toggle & Export CTA
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Cloud Projects Storage Button
                FilledTonalIconButton(
                    onClick = {
                        if (currentUser != null) onOpenCloudProjects() else onOpenAuth()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("cloud_projects_topbar_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = StudioCard,
                        contentColor = if (currentUser != null) AdobeCyan else TextSecondary
                    )
                ) {
                    BadgedBox(
                        badge = {
                            if (currentUser != null && cloudProjectCount > 0) {
                                Badge(
                                    containerColor = AdobeCyan,
                                    contentColor = Color.Black
                                ) {
                                    Text(
                                        text = "$cloudProjectCount",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = if (currentUser != null) Icons.Default.CloudDone else Icons.Default.CloudQueue,
                            contentDescription = "Cloud Projects",
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // Google Account / Sign-In Button
                Surface(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenAuth() }
                        .testTag("account_auth_button"),
                    color = if (currentUser != null) StudioCard else Color.White,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (currentUser != null) StudioBorder else Color.Transparent
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (currentUser != null) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(AdobeBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                val initials = currentUser.displayName?.take(1)?.uppercase() ?: "U"
                                Text(
                                    text = initials,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = currentUser.displayName?.split(" ")?.firstOrNull() ?: "Account",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            Text(
                                text = "G",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = AdobeBlue
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Sign In",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Layers Palette Toggle
                FilledTonalIconButton(
                    onClick = onToggleLayers,
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("toggle_layers_panel_button"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (isLayersPanelOpen) AdobeBlue else StudioCard,
                        contentColor = if (isLayersPanelOpen) Color.White else TextPrimary
                    )
                ) {
                    BadgedBox(
                        badge = {
                            if (layerCount > 0) {
                                Badge(
                                    containerColor = if (isLayersPanelOpen) Color.White else AdobeCyan,
                                    contentColor = if (isLayersPanelOpen) AdobeBlue else Color.Black
                                ) {
                                    Text(
                                        text = "$layerCount",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Toggle Layers Palette",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Export Button
                ElevatedButton(
                    onClick = onExportClick,
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("export_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.elevatedButtonColors(
                        containerColor = AdobeBlue,
                        contentColor = Color.White
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Export & Share Design",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Export",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
