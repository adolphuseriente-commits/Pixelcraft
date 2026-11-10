package com.example.ui

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.credentials.CredentialManager
import com.example.data.auth.GoogleAuthManager
import com.example.model.EditorTool
import com.example.model.ShapeLayer
import com.example.model.ShapeType
import com.example.model.TextLayer
import com.example.ui.components.AuthDialog
import com.example.ui.components.CanvasView
import com.example.ui.components.CloudProjectsDialog
import com.example.ui.components.ColorPickerDialog
import com.example.ui.components.ExportDialog
import com.example.ui.components.InspectorDock
import com.example.ui.components.LayersDrawer
import com.example.ui.components.PhotoshopToolBar
import com.example.ui.components.PhotoshopTopBar
import com.example.ui.components.TemplatesDialog
import com.example.ui.components.TextEditDialog
import com.example.ui.theme.AdobeBlue
import com.example.ui.theme.AdobeCyan
import com.example.ui.theme.StudioBg
import com.example.ui.theme.StudioCard
import com.example.viewmodel.EditorViewModel

@Composable
fun PhotocraftStudioScreen(
    viewModel: EditorViewModel = viewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val credentialManager = remember { CredentialManager.create(context) }

    // Attempt silent auto sign-in on launch
    LaunchedEffect(Unit) {
        if (uiState.currentUser == null) {
            GoogleAuthManager.attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { /* auth state listener automatically handles success */ },
                onUnauthenticated = { /* user will sign in on demand */ },
                scope = scope
            )
        }
    }

    // Android Photo Picker (zero-permission)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.addImageFromUri(it) }
    }

    // Handle Share Intent
    LaunchedEffect(uiState.shareIntentUri) {
        uiState.shareIntentUri?.let { uri ->
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Design"))
            viewModel.clearShareIntent()
        }
    }

    // Status message toast/banner
    LaunchedEffect(uiState.statusMessage) {
        uiState.statusMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearStatusMessage()
        }
    }

    // Back handling
    BackHandler(enabled = uiState.showLayersPanel || uiState.activeTool != EditorTool.MOVE) {
        if (uiState.showLayersPanel) {
            viewModel.toggleLayersPanel()
        } else {
            viewModel.selectTool(EditorTool.MOVE)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(StudioBg)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("photocraft_main_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            PhotoshopTopBar(
                layerCount = uiState.layers.size,
                canUndo = uiState.canUndo,
                canRedo = uiState.canRedo,
                zoomScale = uiState.zoomScale,
                isLayersPanelOpen = uiState.showLayersPanel,
                currentUser = uiState.currentUser,
                cloudProjectCount = uiState.cloudProjects.size,
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onResetZoom = { viewModel.resetViewport() },
                onToggleLayers = { viewModel.toggleLayersPanel() },
                onOpenTemplates = { viewModel.setShowTemplatesDialog(true) },
                onOpenAuth = { viewModel.setShowAuthDialog(true) },
                onOpenCloudProjects = { viewModel.setShowCloudProjectsDialog(true) },
                onExportClick = { viewModel.setShowExportDialog(true) }
            )
        },
        bottomBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Inspector Dock (contextual options)
                InspectorDock(
                    uiState = uiState,
                    selectedLayer = viewModel.selectedLayer,
                    onAlignLayer = { viewModel.alignSelectedLayer(it) },
                    onFlipLayer = { viewModel.flipSelectedLayer(it) },
                    onNudgeLayer = { dx, dy ->
                        viewModel.selectedLayer?.let { layer ->
                            viewModel.updateLayerTransform(layer.id, dx, dy)
                            viewModel.finishTransformGesture()
                        }
                    },
                    onRotateLayer = { delta ->
                        viewModel.selectedLayer?.let { layer ->
                            viewModel.updateLayerTransform(layer.id, 0f, 0f, 1f, delta)
                            viewModel.finishTransformGesture()
                        }
                    },
                    onSetBrushSize = { viewModel.setBrushSize(it) },
                    onSetBrushColor = { viewModel.setBrushColor(it) },
                    onSetBrushAlpha = { viewModel.setBrushAlpha(it) },
                    onOpenColorPicker = { target -> viewModel.setShowColorPickerDialog(true, target) },
                    onOpenTextEditor = { viewModel.setShowTextEditDialog(true) },
                    onUpdateTextFont = { font ->
                        viewModel.updateSelectedLayer({ (it as? TextLayer)?.copy(fontFamily = font) ?: it }, true, "Change Font")
                    },
                    onToggleTextBold = {
                        viewModel.updateSelectedLayer({ (it as? TextLayer)?.copy(isBold = !(it as TextLayer).isBold) ?: it }, true, "Toggle Bold")
                    },
                    onToggleTextItalic = {
                        viewModel.updateSelectedLayer({ (it as? TextLayer)?.copy(isItalic = !(it as TextLayer).isItalic) ?: it }, true, "Toggle Italic")
                    },
                    onToggleTextBackground = {
                        viewModel.updateSelectedLayer({ (it as? TextLayer)?.copy(hasBackground = !(it as TextLayer).hasBackground) ?: it }, true, "Toggle Text Badge")
                    },
                    onSetTextFontSize = { size ->
                        viewModel.updateSelectedLayer({ (it as? TextLayer)?.copy(fontSize = size) ?: it }, true, "Change Font Size")
                    },
                    onSelectShapeType = { shape ->
                        viewModel.updateSelectedLayer({ (it as? ShapeLayer)?.copy(shapeType = shape) ?: it }, true, "Change Shape")
                    },
                    onSetShapeStrokeWidth = { width ->
                        viewModel.updateSelectedLayer({ (it as? ShapeLayer)?.copy(strokeWidth = width) ?: it }, true, "Shape Stroke")
                    },
                    onSetShapeCornerRadius = { radius ->
                        viewModel.updateSelectedLayer({ (it as? ShapeLayer)?.copy(cornerRadius = radius) ?: it }, true, "Corner Radius")
                    },
                    onUpdateImageAdjustments = { b, c, s, h, w, preset ->
                        viewModel.updateSelectedImageAdjustments(b, c, s, h, w, null, preset)
                    },
                    onResetAdjustments = { viewModel.resetAdjustments() },
                    onAddSticker = { id, title -> viewModel.addNewStickerLayer(id, title) },
                    onSetCanvasPreset = { viewModel.setCanvasPreset(it) },
                    onSetCanvasBg = { viewModel.setCanvasBackgroundColor(it) }
                )

                // Photoshop Tools Strip
                PhotoshopToolBar(
                    activeTool = uiState.activeTool,
                    onToolSelected = { viewModel.selectTool(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(StudioBg)
        ) {
            // Interactive Canvas Viewport
            CanvasView(
                uiState = uiState,
                onLayerSelected = { id -> viewModel.selectLayer(id) },
                onLayerTransformed = { id, dx, dy, scale, rot ->
                    viewModel.updateLayerTransform(id, dx, dy, scale, rot)
                },
                onTransformFinished = { viewModel.finishTransformGesture() },
                onDrawingStart = { viewModel.onDrawingStart(it) },
                onDrawingMove = { viewModel.onDrawingMove(it) },
                onDrawingEnd = { viewModel.onDrawingEnd() }
            )

            // Layers Drawer Overlay
            AnimatedVisibility(
                visible = uiState.showLayersPanel,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                LayersDrawer(
                    layers = uiState.layers,
                    selectedLayerId = uiState.selectedLayerId,
                    onSelectLayer = { viewModel.selectLayer(it) },
                    onToggleVisibility = { viewModel.toggleLayerVisibility(it) },
                    onToggleLock = { viewModel.toggleLayerLock(it) },
                    onReorderLayer = { id, up -> viewModel.reorderLayer(id, up) },
                    onDeleteLayer = { viewModel.deleteLayer(it) },
                    onDuplicateLayer = { viewModel.duplicateLayer(it) },
                    onSetOpacity = { id, op -> viewModel.setLayerOpacity(id, op) },
                    onFinishOpacity = { viewModel.finishOpacityEdit() },
                    onSetBlendMode = { id, mode -> viewModel.setLayerBlendMode(id, mode) },
                    onAddText = { viewModel.addNewTextLayer() },
                    onAddShape = { viewModel.addNewShapeLayer(ShapeType.ROUNDED_RECT) },
                    onAddDrawing = {
                        viewModel.selectTool(EditorTool.BRUSH)
                        viewModel.toggleLayersPanel()
                    },
                    onAddSticker = { viewModel.addNewStickerLayer("badge_verified", "VERIFIED") },
                    onPickImage = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onClose = { viewModel.toggleLayersPanel() }
                )
            }
        }
    }

    // Dialogs
    if (uiState.showColorPickerDialog) {
        val initialColor = when (uiState.colorPickerTarget) {
            "textColor" -> (viewModel.selectedLayer as? TextLayer)?.textColor ?: 0xFFFFFFFF
            "shapeFill" -> (viewModel.selectedLayer as? ShapeLayer)?.fillColor ?: 0xFF007DFE
            "shapeStroke" -> (viewModel.selectedLayer as? ShapeLayer)?.strokeColor ?: 0xFF00C7FF
            else -> uiState.brushColor
        }
        ColorPickerDialog(
            initialColor = initialColor,
            onColorSelected = { selectedColor ->
                when (uiState.colorPickerTarget) {
                    "brush" -> viewModel.setBrushColor(selectedColor)
                    "textColor" -> viewModel.updateSelectedLayer({ (it as? TextLayer)?.copy(textColor = selectedColor) ?: it }, true, "Text Color")
                    "shapeFill" -> viewModel.updateSelectedLayer({ (it as? ShapeLayer)?.copy(fillColor = selectedColor) ?: it }, true, "Fill Color")
                    "shapeStroke" -> viewModel.updateSelectedLayer({ (it as? ShapeLayer)?.copy(strokeColor = selectedColor) ?: it }, true, "Stroke Color")
                }
            },
            onDismiss = { viewModel.setShowColorPickerDialog(false) }
        )
    }

    if (uiState.showExportDialog) {
        ExportDialog(
            isExporting = uiState.isExporting,
            canvasWidth = uiState.canvasWidth,
            canvasHeight = uiState.canvasHeight,
            layerCount = uiState.layers.size,
            onExportToGallery = { viewModel.exportComposition(saveToGallery = true, shareDirectly = false) },
            onShareDirectly = { viewModel.exportComposition(saveToGallery = false, shareDirectly = true) },
            onSaveToCloud = {
                viewModel.setShowExportDialog(false)
                viewModel.saveProjectToCloud(uiState.projectTitle)
            },
            onDismiss = { viewModel.setShowExportDialog(false) }
        )
    }

    if (uiState.showTemplatesDialog) {
        TemplatesDialog(
            onLoadCyberpunk = { viewModel.loadCyberpunkTemplate() },
            onLoadFashion = { viewModel.loadFashionTemplate() },
            onLoadBlank = { preset -> viewModel.loadBlankCanvas(preset) },
            onDismiss = { viewModel.setShowTemplatesDialog(false) }
        )
    }

    if (uiState.showTextEditDialog) {
        val selectedText = (viewModel.selectedLayer as? TextLayer)?.text ?: "DESIGN LIKE A PRO"
        TextEditDialog(
            initialText = selectedText,
            onSave = { newText ->
                viewModel.updateSelectedLayer({ (it as? TextLayer)?.copy(text = newText, name = "Text: $newText") ?: it }, true, "Edit Text")
            },
            onDismiss = { viewModel.setShowTextEditDialog(false) }
        )
    }

    // Google Auth Modal Dialog
    if (uiState.showAuthDialog) {
        AuthDialog(
            currentUser = uiState.currentUser,
            onDismiss = { viewModel.setShowAuthDialog(false) }
        )
    }

    // Cloud Firestore Projects Drawer / Dialog
    if (uiState.showCloudProjectsDialog) {
        CloudProjectsDialog(
            projects = uiState.cloudProjects,
            isLoading = uiState.isLoadingCloudProjects,
            isSaving = uiState.isSavingToCloud,
            currentProjectTitle = uiState.projectTitle,
            onSaveCurrent = { title -> viewModel.saveProjectToCloud(title) },
            onLoadProject = { project -> viewModel.loadProjectFromCloud(project) },
            onDeleteProject = { id -> viewModel.deleteCloudProject(id) },
            onRefresh = { viewModel.observeCloudProjects() },
            onDismiss = { viewModel.setShowCloudProjectsDialog(false) }
        )
    }
}
