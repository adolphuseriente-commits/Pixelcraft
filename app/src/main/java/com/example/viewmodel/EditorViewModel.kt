package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.CloudDesignProject
import com.example.data.repository.FirestoreRepository
import com.example.data.util.LayerSerializer
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.example.engine.ImageProcessor
import com.example.model.CanvasBlendMode
import com.example.model.CanvasPreset
import com.example.model.DrawPath
import com.example.model.DrawPoint
import com.example.model.DrawingLayer
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class CanvasAlignment {
    LEFT, CENTER_X, RIGHT, TOP, CENTER_Y, BOTTOM
}

data class HistoryItem(
    val description: String,
    val layers: List<Layer>,
    val canvasWidth: Int,
    val canvasHeight: Int,
    val backgroundColor: Long?
)

data class EditorUiState(
    val canvasWidth: Int = 1080,
    val canvasHeight: Int = 1080,
    val canvasPreset: CanvasPreset = STANDARD_CANVAS_PRESETS[0],
    val backgroundColor: Long? = 0xFF12141A,
    val layers: List<Layer> = emptyList(),
    val selectedLayerId: String? = null,
    val activeTool: EditorTool = EditorTool.MOVE,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val historyCount: Int = 0,
    val currentHistoryIndex: Int = 0,

    // Brush Settings
    val brushSize: Float = 16f,
    val brushColor: Long = 0xFF00C7FF,
    val brushAlpha: Float = 1.0f,
    val isEraser: Boolean = false,
    val currentDrawingPath: List<DrawPoint> = emptyList(),

    // Viewport
    val zoomScale: Float = 1.0f,
    val panX: Float = 0f,
    val panY: Float = 0f,

    // UI Panel Toggles
    val showLayersPanel: Boolean = false,
    val showExportDialog: Boolean = false,
    val showTemplatesDialog: Boolean = false,
    val showColorPickerDialog: Boolean = false,
    val showTextEditDialog: Boolean = false,
    val colorPickerTarget: String = "brush", // "brush", "textColor", "textBg", "shapeFill", "shapeStroke", "canvasBg"

    // Export Feedback
    val isExporting: Boolean = false,
    val exportedUri: Uri? = null,
    val shareIntentUri: Uri? = null,
    val statusMessage: String? = null,

    // Firebase Auth & Cloud Firestore
    val currentUser: com.google.firebase.auth.FirebaseUser? = null,
    val isSigningIn: Boolean = false,
    val authError: String? = null,
    val showAuthDialog: Boolean = false,
    val cloudProjects: List<com.example.data.model.CloudDesignProject> = emptyList(),
    val isLoadingCloudProjects: Boolean = false,
    val showCloudProjectsDialog: Boolean = false,
    val currentCloudProjectId: String? = null,
    val isSavingToCloud: Boolean = false,
    val projectTitle: String = "Neon Revolution"
)

class EditorViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    private val history = mutableListOf<HistoryItem>()
    private var historyIndex = -1
    private val maxHistorySteps = 30

    private val firestoreRepository by lazy { FirestoreRepository(getApplication<Application>()) }

    init {
        loadCyberpunkTemplate()
        Firebase.auth.addAuthStateListener { auth ->
            val user = auth.currentUser
            _uiState.update { it.copy(currentUser = user) }
            if (user != null) {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        firestoreRepository.syncUserProfile(user)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                observeCloudProjects()
            } else {
                _uiState.update { it.copy(cloudProjects = emptyList()) }
            }
        }
    }

    // ==========================================
    // HISTORY SYSTEM (Undo / Redo like Photoshop)
    // ==========================================
    private fun recordHistory(actionDescription: String) {
        val state = _uiState.value
        // Truncate any redo steps ahead
        while (history.size > historyIndex + 1) {
            history.removeAt(history.size - 1)
        }

        val item = HistoryItem(
            description = actionDescription,
            layers = state.layers.map { it },
            canvasWidth = state.canvasWidth,
            canvasHeight = state.canvasHeight,
            backgroundColor = state.backgroundColor
        )
        history.add(item)
        if (history.size > maxHistorySteps) {
            history.removeAt(0)
        } else {
            historyIndex++
        }

        updateHistoryFlags()
    }

    private fun updateHistoryFlags() {
        _uiState.update {
            it.copy(
                canUndo = historyIndex > 0,
                canRedo = historyIndex < history.size - 1,
                historyCount = history.size,
                currentHistoryIndex = historyIndex
            )
        }
    }

    fun undo() {
        if (historyIndex > 0) {
            historyIndex--
            val item = history[historyIndex]
            _uiState.update {
                it.copy(
                    layers = item.layers,
                    canvasWidth = item.canvasWidth,
                    canvasHeight = item.canvasHeight,
                    backgroundColor = item.backgroundColor,
                    statusMessage = "Undo: ${item.description}"
                )
            }
            updateHistoryFlags()
        }
    }

    fun redo() {
        if (historyIndex < history.size - 1) {
            historyIndex++
            val item = history[historyIndex]
            _uiState.update {
                it.copy(
                    layers = item.layers,
                    canvasWidth = item.canvasWidth,
                    canvasHeight = item.canvasHeight,
                    backgroundColor = item.backgroundColor,
                    statusMessage = "Redo: ${item.description}"
                )
            }
            updateHistoryFlags()
        }
    }

    // ==========================================
    // TOOL SELECTION & UI PANELS
    // ==========================================
    fun selectTool(tool: EditorTool) {
        _uiState.update {
            it.copy(
                activeTool = tool,
                isEraser = (tool == EditorTool.ERASER)
            )
        }
    }

    fun toggleLayersPanel() {
        _uiState.update { it.copy(showLayersPanel = !it.showLayersPanel) }
    }

    fun setShowExportDialog(show: Boolean) {
        _uiState.update { it.copy(showExportDialog = show) }
    }

    fun setShowTemplatesDialog(show: Boolean) {
        _uiState.update { it.copy(showTemplatesDialog = show) }
    }

    fun setShowColorPickerDialog(show: Boolean, target: String = "brush") {
        _uiState.update {
            it.copy(
                showColorPickerDialog = show,
                colorPickerTarget = target
            )
        }
    }

    fun setShowTextEditDialog(show: Boolean) {
        _uiState.update { it.copy(showTextEditDialog = show) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    // ==========================================
    // LAYER MANAGEMENT
    // ==========================================
    val selectedLayer: Layer?
        get() = _uiState.value.layers.find { it.id == _uiState.value.selectedLayerId }

    fun selectLayer(id: String?) {
        _uiState.update { it.copy(selectedLayerId = id) }
    }

    fun addLayer(layer: Layer, description: String = "Add Layer") {
        _uiState.update { current ->
            current.copy(
                layers = current.layers + layer,
                selectedLayerId = layer.id
            )
        }
        recordHistory(description)
    }

    fun updateSelectedLayer(update: (Layer) -> Layer, recordHistory: Boolean = true, description: String = "Edit Layer") {
        val targetId = _uiState.value.selectedLayerId ?: return
        _uiState.update { current ->
            val updated = current.layers.map { layer ->
                if (layer.id == targetId && !layer.isLocked) update(layer) else layer
            }
            current.copy(layers = updated)
        }
        if (recordHistory) {
            recordHistory(description)
        }
    }

    fun updateLayerTransform(
        id: String,
        dx: Float,
        dy: Float,
        scaleFactor: Float = 1.0f,
        rotationDelta: Float = 0f
    ) {
        _uiState.update { current ->
            val updated = current.layers.map { layer ->
                if (layer.id == id && !layer.isLocked) {
                    val newScale = (layer.scale * scaleFactor).coerceIn(0.1f, 8.0f)
                    val newRot = (layer.rotation + rotationDelta) % 360f
                    layer.copyWithCommon(
                        x = layer.x + dx,
                        y = layer.y + dy,
                        scale = newScale,
                        rotation = newRot
                    )
                } else layer
            }
            current.copy(layers = updated)
        }
    }

    fun finishTransformGesture() {
        recordHistory("Transform Layer")
    }

    fun reorderLayer(id: String, moveUp: Boolean) {
        val currentLayers = _uiState.value.layers.toMutableList()
        val index = currentLayers.indexOfFirst { it.id == id }
        if (index == -1) return

        if (moveUp && index < currentLayers.size - 1) {
            val item = currentLayers.removeAt(index)
            currentLayers.add(index + 1, item)
            _uiState.update { it.copy(layers = currentLayers) }
            recordHistory("Bring Forward")
        } else if (!moveUp && index > 0) {
            val item = currentLayers.removeAt(index)
            currentLayers.add(index - 1, item)
            _uiState.update { it.copy(layers = currentLayers) }
            recordHistory("Send Backward")
        }
    }

    fun bringToFront(id: String) {
        val currentLayers = _uiState.value.layers.toMutableList()
        val index = currentLayers.indexOfFirst { it.id == id }
        if (index in 0 until currentLayers.size - 1) {
            val item = currentLayers.removeAt(index)
            currentLayers.add(item)
            _uiState.update { it.copy(layers = currentLayers) }
            recordHistory("Bring to Front")
        }
    }

    fun sendToBack(id: String) {
        val currentLayers = _uiState.value.layers.toMutableList()
        val index = currentLayers.indexOfFirst { it.id == id }
        if (index > 0) {
            val item = currentLayers.removeAt(index)
            currentLayers.add(0, item)
            _uiState.update { it.copy(layers = currentLayers) }
            recordHistory("Send to Back")
        }
    }

    fun toggleLayerVisibility(id: String) {
        _uiState.update { current ->
            val updated = current.layers.map {
                if (it.id == id) it.copyWithCommon(isVisible = !it.isVisible) else it
            }
            current.copy(layers = updated)
        }
        recordHistory("Toggle Visibility")
    }

    fun toggleLayerLock(id: String) {
        _uiState.update { current ->
            val updated = current.layers.map {
                if (it.id == id) it.copyWithCommon(isLocked = !it.isLocked) else it
            }
            current.copy(layers = updated)
        }
        recordHistory("Toggle Lock")
    }

    fun setLayerOpacity(id: String, opacity: Float) {
        _uiState.update { current ->
            val updated = current.layers.map {
                if (it.id == id) it.copyWithCommon(opacity = opacity.coerceIn(0f, 1f)) else it
            }
            current.copy(layers = updated)
        }
    }

    fun finishOpacityEdit() {
        recordHistory("Change Opacity")
    }

    fun setLayerBlendMode(id: String, blendMode: CanvasBlendMode) {
        _uiState.update { current ->
            val updated = current.layers.map {
                if (it.id == id) it.copyWithCommon(blendMode = blendMode) else it
            }
            current.copy(layers = updated)
        }
        recordHistory("Blend Mode: ${blendMode.displayName}")
    }

    fun duplicateLayer(id: String) {
        val layer = _uiState.value.layers.find { it.id == id } ?: return
        val newId = UUID.randomUUID().toString()
        val duplicated = when (layer) {
            is ImageLayer -> layer.copy(id = newId, name = "${layer.name} Copy", x = layer.x + 30f, y = layer.y + 30f)
            is TextLayer -> layer.copy(id = newId, name = "${layer.name} Copy", x = layer.x + 30f, y = layer.y + 30f)
            is ShapeLayer -> layer.copy(id = newId, name = "${layer.name} Copy", x = layer.x + 30f, y = layer.y + 30f)
            is DrawingLayer -> layer.copy(id = newId, name = "${layer.name} Copy", x = layer.x + 30f, y = layer.y + 30f)
            is StickerLayer -> layer.copy(id = newId, name = "${layer.name} Copy", x = layer.x + 30f, y = layer.y + 30f)
        }
        addLayer(duplicated, "Duplicate Layer")
    }

    fun deleteLayer(id: String) {
        _uiState.update { current ->
            val filtered = current.layers.filterNot { it.id == id }
            val nextSelected = if (current.selectedLayerId == id) filtered.lastOrNull()?.id else current.selectedLayerId
            current.copy(layers = filtered, selectedLayerId = nextSelected)
        }
        recordHistory("Delete Layer")
    }

    fun alignSelectedLayer(alignment: CanvasAlignment) {
        val layer = selectedLayer ?: return
        val canvasW = _uiState.value.canvasWidth.toFloat()
        val canvasH = _uiState.value.canvasHeight.toFloat()
        val newX = when (alignment) {
            CanvasAlignment.LEFT -> 150f
            CanvasAlignment.CENTER_X -> canvasW / 2f
            CanvasAlignment.RIGHT -> canvasW - 150f
            else -> layer.x
        }
        val newY = when (alignment) {
            CanvasAlignment.TOP -> 150f
            CanvasAlignment.CENTER_Y -> canvasH / 2f
            CanvasAlignment.BOTTOM -> canvasH - 150f
            else -> layer.y
        }
        updateSelectedLayer({ it.copyWithCommon(x = newX, y = newY) }, true, "Align Layer")
    }

    fun flipSelectedLayer(horizontal: Boolean) {
        updateSelectedLayer({
            if (horizontal) it.copyWithCommon(flipH = !it.flipH) else it.copyWithCommon(flipV = !it.flipV)
        }, true, if (horizontal) "Flip Horizontal" else "Flip Vertical")
    }

    // ==========================================
    // BRUSH & DRAWING ENGINE
    // ==========================================
    fun setBrushSize(size: Float) {
        _uiState.update { it.copy(brushSize = size) }
    }

    fun setBrushColor(color: Long) {
        _uiState.update { it.copy(brushColor = color) }
    }

    fun setBrushAlpha(alpha: Float) {
        _uiState.update { it.copy(brushAlpha = alpha) }
    }

    fun onDrawingStart(point: DrawPoint) {
        _uiState.update { it.copy(currentDrawingPath = listOf(point)) }
    }

    fun onDrawingMove(point: DrawPoint) {
        _uiState.update { it.copy(currentDrawingPath = it.currentDrawingPath + point) }
    }

    fun onDrawingEnd() {
        val points = _uiState.value.currentDrawingPath
        if (points.isEmpty()) return

        val state = _uiState.value
        val newPath = DrawPath(
            points = points,
            color = state.brushColor,
            strokeWidth = state.brushSize,
            isEraser = state.isEraser,
            alpha = state.brushAlpha
        )

        // Find existing DrawingLayer or create a new one
        val layers = state.layers.toMutableList()
        val drawingIndex = layers.indexOfLast { it is DrawingLayer && !it.isLocked }

        if (drawingIndex != -1) {
            val drawing = layers[drawingIndex] as DrawingLayer
            layers[drawingIndex] = drawing.copy(paths = drawing.paths + newPath)
        } else {
            val newDrawing = DrawingLayer(
                id = UUID.randomUUID().toString(),
                name = if (state.isEraser) "Eraser Stroke" else "Drawing Layer",
                paths = listOf(newPath)
            )
            layers.add(newDrawing)
        }

        _uiState.update {
            it.copy(
                layers = layers,
                currentDrawingPath = emptyList()
            )
        }
        recordHistory(if (state.isEraser) "Eraser Stroke" else "Brush Stroke")
    }

    // ==========================================
    // CREATING NEW LAYERS
    // ==========================================
    fun addNewTextLayer(text: String = "YOUR TITLE HERE") {
        val canvasW = _uiState.value.canvasWidth.toFloat()
        val canvasH = _uiState.value.canvasHeight.toFloat()
        val layer = TextLayer(
            id = UUID.randomUUID().toString(),
            name = "Text: $text",
            x = canvasW / 2f,
            y = canvasH / 2f,
            text = text,
            fontSize = 36f,
            textColor = 0xFFFFFFFF,
            fontFamily = TextFontFamily.IMPACT
        )
        addLayer(layer, "Add Text Layer")
    }

    fun addNewShapeLayer(shapeType: ShapeType) {
        val canvasW = _uiState.value.canvasWidth.toFloat()
        val canvasH = _uiState.value.canvasHeight.toFloat()
        val layer = ShapeLayer(
            id = UUID.randomUUID().toString(),
            name = "${shapeType.displayName} Shape",
            shapeType = shapeType,
            x = canvasW / 2f,
            y = canvasH / 2f,
            width = 240f,
            height = 240f,
            fillColor = 0xFF007DFE,
            strokeColor = 0xFF00C7FF,
            strokeWidth = 4f
        )
        addLayer(layer, "Add Shape: ${shapeType.displayName}")
    }

    fun addNewStickerLayer(stickerId: String, title: String) {
        val canvasW = _uiState.value.canvasWidth.toFloat()
        val canvasH = _uiState.value.canvasHeight.toFloat()
        val layer = StickerLayer(
            id = UUID.randomUUID().toString(),
            name = "Badge: $title",
            stickerId = stickerId,
            title = title,
            x = canvasW / 2f,
            y = canvasH / 2f
        )
        addLayer(layer, "Add Graphic: $title")
    }

    fun addImageFromUri(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val bitmap = context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
                if (bitmap != null) {
                    val canvasW = _uiState.value.canvasWidth.toFloat()
                    val canvasH = _uiState.value.canvasHeight.toFloat()
                    val layer = ImageLayer(
                        id = UUID.randomUUID().toString(),
                        name = "Photo Layer",
                        bitmap = bitmap,
                        uriString = uri.toString(),
                        x = canvasW / 2f,
                        y = canvasH / 2f,
                        width = minOf(canvasW * 0.8f, bitmap.width.toFloat()),
                        height = minOf(canvasH * 0.8f, bitmap.height.toFloat())
                    )
                    addLayer(layer, "Import Photo")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // ==========================================
    // IMAGE ADJUSTMENTS & FILTERS
    // ==========================================
    fun updateSelectedImageAdjustments(
        brightness: Float? = null,
        contrast: Float? = null,
        saturation: Float? = null,
        hue: Float? = null,
        warmth: Float? = null,
        vignette: Float? = null,
        preset: FilterPreset? = null
    ) {
        val target = selectedLayer as? ImageLayer ?: return
        val updated = target.copy(
            brightness = brightness ?: target.brightness,
            contrast = contrast ?: target.contrast,
            saturation = saturation ?: target.saturation,
            hue = hue ?: target.hue,
            warmth = warmth ?: target.warmth,
            vignette = vignette ?: target.vignette,
            filterPreset = preset ?: target.filterPreset
        )
        updateSelectedLayer({ updated }, false)
    }

    fun finishAdjustmentEdit(label: String = "Adjust Image") {
        recordHistory(label)
    }

    fun resetAdjustments() {
        val target = selectedLayer as? ImageLayer ?: return
        val updated = target.copy(
            brightness = 0f,
            contrast = 1f,
            saturation = 1f,
            hue = 0f,
            warmth = 0f,
            vignette = 0f,
            filterPreset = FilterPreset.NONE
        )
        updateSelectedLayer({ updated }, true, "Reset Adjustments")
    }

    // ==========================================
    // CANVAS & CROP
    // ==========================================
    fun setCanvasPreset(preset: CanvasPreset) {
        _uiState.update {
            it.copy(
                canvasPreset = preset,
                canvasWidth = preset.width,
                canvasHeight = preset.height
            )
        }
        recordHistory("Resize Canvas: ${preset.name}")
    }

    fun setCanvasBackgroundColor(color: Long?) {
        _uiState.update { it.copy(backgroundColor = color) }
        recordHistory("Canvas Background Color")
    }

    // ==========================================
    // VIEWPORT (Zoom & Pan)
    // ==========================================
    fun updateViewport(zoomDelta: Float, panDx: Float, panDy: Float) {
        _uiState.update {
            val newZoom = (it.zoomScale * zoomDelta).coerceIn(0.2f, 5.0f)
            it.copy(
                zoomScale = newZoom,
                panX = it.panX + panDx,
                panY = it.panY + panDy
            )
        }
    }

    fun resetViewport() {
        _uiState.update { it.copy(zoomScale = 1.0f, panX = 0f, panY = 0f) }
    }

    // ==========================================
    // TEMPLATES / STARTER DESIGNS
    // ==========================================
    fun loadCyberpunkTemplate() {
        val canvasW = 1080
        val canvasH = 1080
        val context = getApplication<Application>()
        val neonBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sample_neon)

        val layers = listOf<Layer>(
            // 1. Cyberpunk Neon Background Image
            ImageLayer(
                id = UUID.randomUUID().toString(),
                name = "Cyberpunk Nightscape",
                bitmap = neonBitmap,
                resourceId = R.drawable.sample_neon,
                width = 1080f,
                height = 1080f,
                x = 540f,
                y = 540f,
                contrast = 1.25f,
                saturation = 1.35f,
                filterPreset = FilterPreset.CYBERPUNK
            ),
            // 2. Cyan Glowing Geometric Frame
            ShapeLayer(
                id = UUID.randomUUID().toString(),
                name = "Neon Border Frame",
                shapeType = ShapeType.ROUNDED_RECT,
                x = 540f,
                y = 540f,
                width = 940f,
                height = 940f,
                fillColor = 0x00000000,
                strokeColor = 0xFF00C7FF,
                strokeWidth = 8f,
                cornerRadius = 32f,
                blendMode = CanvasBlendMode.SCREEN
            ),
            // 3. Bold Main Title
            TextLayer(
                id = UUID.randomUUID().toString(),
                name = "Headline Title",
                text = "NEON REVOLUTION",
                fontSize = 42f,
                textColor = 0xFFFFFFFF,
                fontFamily = TextFontFamily.IMPACT,
                x = 540f,
                y = 480f,
                shadowRadius = 8f,
                shadowColor = 0xFF00C7FF
            ),
            // 4. Subtitle
            TextLayer(
                id = UUID.randomUUID().toString(),
                name = "Subtitle",
                text = "ELECTRIC DREAMS • 2026 EDITION",
                fontSize = 20f,
                textColor = 0xFF00E676,
                fontFamily = TextFontFamily.MONOSPACE,
                x = 540f,
                y = 560f,
                hasBackground = true,
                backgroundColor = 0xCC12141A
            ),
            // 5. Pro Badge
            StickerLayer(
                id = UUID.randomUUID().toString(),
                name = "Studio Badge",
                stickerId = "badge_verified",
                title = "VERIFIED MASTER",
                x = 540f,
                y = 660f,
                width = 220f,
                height = 50f
            )
        )

        _uiState.update {
            it.copy(
                canvasWidth = canvasW,
                canvasHeight = canvasH,
                canvasPreset = STANDARD_CANVAS_PRESETS[0],
                layers = layers,
                selectedLayerId = layers[2].id,
                activeTool = EditorTool.MOVE,
                statusMessage = "Loaded Neon Cyberpunk Project"
            )
        }
        history.clear()
        historyIndex = -1
        recordHistory("New Project: Cyberpunk Poster")
    }

    fun loadFashionTemplate() {
        val canvasW = 1080
        val canvasH = 1350
        val context = getApplication<Application>()
        val portraitBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.sample_portrait)

        val layers = listOf<Layer>(
            // 1. Portrait Base
            ImageLayer(
                id = UUID.randomUUID().toString(),
                name = "Editorial Portrait",
                bitmap = portraitBitmap,
                resourceId = R.drawable.sample_portrait,
                width = 1080f,
                height = 1350f,
                x = 540f,
                y = 675f,
                contrast = 1.15f,
                filterPreset = FilterPreset.GOLDEN_HOUR
            ),
            // 2. High Fashion Headline
            TextLayer(
                id = UUID.randomUUID().toString(),
                name = "Magazine Masthead",
                text = "AVANT GARDE",
                fontSize = 54f,
                textColor = 0xFFFFFFFF,
                fontFamily = TextFontFamily.SERIF,
                isBold = true,
                x = 540f,
                y = 240f,
                shadowRadius = 6f,
                shadowColor = 0x99000000
            ),
            // 3. Issue Subhead
            TextLayer(
                id = UUID.randomUUID().toString(),
                name = "Edition Text",
                text = "THE CREATIVE ISSUE • VOLUME IV",
                fontSize = 18f,
                textColor = 0xFFFFD700,
                fontFamily = TextFontFamily.DEFAULT,
                x = 540f,
                y = 320f
            ),
            // 4. Star Graphic
            ShapeLayer(
                id = UUID.randomUUID().toString(),
                name = "Gold Star Accent",
                shapeType = ShapeType.STAR,
                x = 880f,
                y = 240f,
                width = 80f,
                height = 80f,
                fillColor = 0xFFFFD700,
                strokeColor = 0xFFFFFFFF,
                strokeWidth = 2f
            ),
            // 5. Limited Stamp
            StickerLayer(
                id = UUID.randomUUID().toString(),
                name = "Collector Stamp",
                stickerId = "badge_limited",
                title = "SPECIAL EDITION",
                x = 540f,
                y = 1200f,
                width = 240f,
                height = 60f
            )
        )

        _uiState.update {
            it.copy(
                canvasWidth = canvasW,
                canvasHeight = canvasH,
                canvasPreset = STANDARD_CANVAS_PRESETS[1],
                layers = layers,
                selectedLayerId = layers[1].id,
                activeTool = EditorTool.MOVE,
                statusMessage = "Loaded Avant Garde Fashion Magazine"
            )
        }
        history.clear()
        historyIndex = -1
        recordHistory("New Project: Fashion Magazine")
    }

    fun loadBlankCanvas(preset: CanvasPreset, bgColor: Long = 0xFF1E2026) {
        _uiState.update {
            it.copy(
                canvasWidth = preset.width,
                canvasHeight = preset.height,
                canvasPreset = preset,
                backgroundColor = bgColor,
                layers = emptyList(),
                selectedLayerId = null,
                activeTool = EditorTool.MOVE,
                statusMessage = "Created Blank Canvas (${preset.width}x${preset.height})"
            )
        }
        history.clear()
        historyIndex = -1
        recordHistory("Blank Canvas (${preset.name})")
    }

    // ==========================================
    // EXPORT & SHARE
    // ==========================================
    fun exportComposition(saveToGallery: Boolean, shareDirectly: Boolean) {
        _uiState.update { it.copy(isExporting = true) }
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val state = _uiState.value
            val renderedBitmap = ImageProcessor.renderComposition(
                context = context,
                layers = state.layers,
                canvasWidth = state.canvasWidth,
                canvasHeight = state.canvasHeight,
                backgroundColor = state.backgroundColor
            )

            var galleryUri: Uri? = null
            var shareUri: Uri? = null

            if (saveToGallery) {
                galleryUri = ImageProcessor.saveToGallery(context, renderedBitmap, "Photocraft_Design")
            }
            if (shareDirectly) {
                shareUri = ImageProcessor.createShareableUri(context, renderedBitmap, "Photocraft_Design")
            }

            _uiState.update {
                it.copy(
                    isExporting = false,
                    showExportDialog = false,
                    exportedUri = galleryUri,
                    shareIntentUri = shareUri,
                    statusMessage = when {
                        galleryUri != null && shareUri != null -> "Saved to Gallery & Ready to Share!"
                        galleryUri != null -> "Exported successfully to Pictures/Photocraft!"
                        shareUri != null -> "Design ready to share!"
                        else -> "Export completed"
                    }
                )
            }
        }
    }

    fun clearShareIntent() {
        _uiState.update { it.copy(shareIntentUri = null) }
    }

    // ==========================================
    // CLOUD FIRESTORE PROJECTS & SYNC
    // ==========================================
    fun observeCloudProjects() {
        val user = Firebase.auth.currentUser ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingCloudProjects = true) }
            try {
                firestoreRepository.observeProjects().collect { projects ->
                    _uiState.update {
                        it.copy(
                            cloudProjects = projects,
                            isLoadingCloudProjects = false
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingCloudProjects = false) }
            }
        }
    }

    fun saveProjectToCloud(title: String) {
        val user = Firebase.auth.currentUser
        if (user == null) {
            _uiState.update { it.copy(showAuthDialog = true, statusMessage = "Sign in with Google to save to Cloud") }
            return
        }

        val state = _uiState.value
        _uiState.update { it.copy(isSavingToCloud = true) }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val layersJson = LayerSerializer.serialize(state.layers)
                val finalTitle = title.ifBlank { state.projectTitle }
                val docId = firestoreRepository.saveProject(
                    title = finalTitle,
                    canvasWidth = state.canvasWidth,
                    canvasHeight = state.canvasHeight,
                    presetName = state.canvasPreset.name,
                    backgroundColor = state.backgroundColor,
                    layerCount = state.layers.size,
                    layersJson = layersJson,
                    existingId = state.currentCloudProjectId
                )
                _uiState.update {
                    it.copy(
                        isSavingToCloud = false,
                        currentCloudProjectId = docId,
                        projectTitle = finalTitle,
                        statusMessage = "Project '$finalTitle' saved to Google Cloud!"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSavingToCloud = false,
                        statusMessage = "Failed to save: ${e.localizedMessage ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    fun loadProjectFromCloud(project: CloudDesignProject) {
        viewModelScope.launch {
            try {
                val restoredLayers = LayerSerializer.deserialize(project.layersJson)
                val preset = STANDARD_CANVAS_PRESETS.find { it.name == project.presetName }
                    ?: STANDARD_CANVAS_PRESETS[0]

                _uiState.update {
                    it.copy(
                        canvasWidth = project.canvasWidth.toInt(),
                        canvasHeight = project.canvasHeight.toInt(),
                        canvasPreset = preset,
                        backgroundColor = project.backgroundColor,
                        layers = restoredLayers,
                        selectedLayerId = restoredLayers.firstOrNull()?.id,
                        currentCloudProjectId = project.id,
                        projectTitle = project.title,
                        showCloudProjectsDialog = false,
                        statusMessage = "Loaded '${project.title}' from Cloud"
                    )
                }
                history.clear()
                historyIndex = -1
                recordHistory("Load Cloud Project: ${project.title}")
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "Failed to load project: ${e.message}") }
            }
        }
    }

    fun deleteCloudProject(projectId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                firestoreRepository.deleteProject(projectId)
                _uiState.update {
                    it.copy(
                        currentCloudProjectId = if (it.currentCloudProjectId == projectId) null else it.currentCloudProjectId,
                        statusMessage = "Project deleted from Cloud"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(statusMessage = "Delete failed: ${e.message}") }
            }
        }
    }

    fun setShowAuthDialog(show: Boolean) {
        _uiState.update { it.copy(showAuthDialog = show) }
    }

    fun setShowCloudProjectsDialog(show: Boolean) {
        _uiState.update { it.copy(showCloudProjectsDialog = show) }
        if (show && Firebase.auth.currentUser != null) {
            observeCloudProjects()
        }
    }

    fun setProjectTitle(title: String) {
        _uiState.update { it.copy(projectTitle = title) }
    }
}
