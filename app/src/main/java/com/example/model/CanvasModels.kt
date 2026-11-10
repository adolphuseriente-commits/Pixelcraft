package com.example.model

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode

enum class EditorTool(val label: String, val shortcut: String) {
    MOVE("Move & Transform", "V"),
    BRUSH("Brush", "B"),
    ERASER("Eraser", "E"),
    TEXT("Typography", "T"),
    SHAPES("Vector Shapes", "U"),
    ADJUST("Adjustments", "A"),
    FILTERS("Filters & LUTs", "F"),
    STICKERS("Graphics & Badges", "G"),
    CROP("Canvas & Crop", "C")
}

enum class CanvasBlendMode(val displayName: String, val composeBlendMode: BlendMode) {
    NORMAL("Normal", BlendMode.SrcOver),
    MULTIPLY("Multiply", BlendMode.Multiply),
    SCREEN("Screen", BlendMode.Screen),
    OVERLAY("Overlay", BlendMode.Overlay),
    DARKEN("Darken", BlendMode.Darken),
    LIGHTEN("Lighten", BlendMode.Lighten),
    COLOR_DODGE("Color Dodge", BlendMode.ColorDodge),
    COLOR_BURN("Color Burn", BlendMode.ColorBurn),
    DIFFERENCE("Difference", BlendMode.Difference),
    SOFT_LIGHT("Soft Light", BlendMode.Softlight)
}

enum class FilterPreset(val displayName: String) {
    NONE("Original"),
    CYBERPUNK("Cyberpunk"),
    FILM_NOIR("Film Noir"),
    GOLDEN_HOUR("Golden Hour"),
    PASTEL("Pastel Dream"),
    DUOTONE("Duotone Neon"),
    SEPIA("Antique Sepia"),
    MONOCHROME("B&W Drama"),
    VIVID("Vivid Pop"),
    VINTAGE("70s Vintage")
}

enum class ShapeType(val displayName: String) {
    RECTANGLE("Rectangle"),
    ROUNDED_RECT("Rounded Box"),
    CIRCLE("Circle / Oval"),
    STAR("5-Point Star"),
    TRIANGLE("Triangle"),
    HEART("Heart"),
    HEXAGON("Hexagon"),
    ARROW("Arrow"),
    LINE("Line")
}

enum class TextFontFamily(val displayName: String) {
    DEFAULT("Sans Serif"),
    SERIF("Classic Serif"),
    MONOSPACE("Code Mono"),
    CURSIVE("Script / Cursive"),
    IMPACT("Bold Impact")
}

data class DrawPoint(val x: Float, val y: Float)

data class DrawPath(
    val points: List<DrawPoint>,
    val color: Long,
    val strokeWidth: Float,
    val isEraser: Boolean = false,
    val alpha: Float = 1.0f
)

sealed class Layer {
    abstract val id: String
    abstract val name: String
    abstract val isVisible: Boolean
    abstract val isLocked: Boolean
    abstract val opacity: Float
    abstract val blendMode: CanvasBlendMode
    abstract val x: Float
    abstract val y: Float
    abstract val scale: Float
    abstract val rotation: Float
    abstract val flipH: Boolean
    abstract val flipV: Boolean

    abstract fun copyWithCommon(
        id: String = this.id,
        name: String = this.name,
        isVisible: Boolean = this.isVisible,
        isLocked: Boolean = this.isLocked,
        opacity: Float = this.opacity,
        blendMode: CanvasBlendMode = this.blendMode,
        x: Float = this.x,
        y: Float = this.y,
        scale: Float = this.scale,
        rotation: Float = this.rotation,
        flipH: Boolean = this.flipH,
        flipV: Boolean = this.flipV
    ): Layer
}

data class ImageLayer(
    override val id: String,
    override val name: String = "Image Layer",
    override val isVisible: Boolean = true,
    override val isLocked: Boolean = false,
    override val opacity: Float = 1.0f,
    override val blendMode: CanvasBlendMode = CanvasBlendMode.NORMAL,
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val scale: Float = 1.0f,
    override val rotation: Float = 0f,
    override val flipH: Boolean = false,
    override val flipV: Boolean = false,
    val bitmap: Bitmap? = null,
    val resourceId: Int? = null,
    val uriString: String? = null,
    val width: Float = 360f,
    val height: Float = 360f,
    val brightness: Float = 0f,      // -1f to 1f
    val contrast: Float = 1f,        // 0.2f to 2.5f
    val saturation: Float = 1f,      // 0f to 2.5f
    val hue: Float = 0f,             // -180f to 180f
    val warmth: Float = 0f,          // -1f to 1f
    val vignette: Float = 0f,        // 0f to 1f
    val filterPreset: FilterPreset = FilterPreset.NONE
) : Layer() {
    override fun copyWithCommon(
        id: String,
        name: String,
        isVisible: Boolean,
        isLocked: Boolean,
        opacity: Float,
        blendMode: CanvasBlendMode,
        x: Float,
        y: Float,
        scale: Float,
        rotation: Float,
        flipH: Boolean,
        flipV: Boolean
    ): Layer = copy(
        id = id,
        name = name,
        isVisible = isVisible,
        isLocked = isLocked,
        opacity = opacity,
        blendMode = blendMode,
        x = x,
        y = y,
        scale = scale,
        rotation = rotation,
        flipH = flipH,
        flipV = flipV
    )
}

data class TextLayer(
    override val id: String,
    override val name: String = "Text Layer",
    override val isVisible: Boolean = true,
    override val isLocked: Boolean = false,
    override val opacity: Float = 1.0f,
    override val blendMode: CanvasBlendMode = CanvasBlendMode.NORMAL,
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val scale: Float = 1.0f,
    override val rotation: Float = 0f,
    override val flipH: Boolean = false,
    override val flipV: Boolean = false,
    val text: String = "DESIGN LIKE A PRO",
    val fontSize: Float = 32f,
    val textColor: Long = 0xFFFFFFFF,
    val fontFamily: TextFontFamily = TextFontFamily.IMPACT,
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val hasBackground: Boolean = false,
    val backgroundColor: Long = 0xCC000000,
    val letterSpacing: Float = 1.0f,
    val shadowRadius: Float = 4f,
    val shadowColor: Long = 0xAA000000
) : Layer() {
    override fun copyWithCommon(
        id: String,
        name: String,
        isVisible: Boolean,
        isLocked: Boolean,
        opacity: Float,
        blendMode: CanvasBlendMode,
        x: Float,
        y: Float,
        scale: Float,
        rotation: Float,
        flipH: Boolean,
        flipV: Boolean
    ): Layer = copy(
        id = id,
        name = name,
        isVisible = isVisible,
        isLocked = isLocked,
        opacity = opacity,
        blendMode = blendMode,
        x = x,
        y = y,
        scale = scale,
        rotation = rotation,
        flipH = flipH,
        flipV = flipV
    )
}

data class ShapeLayer(
    override val id: String,
    override val name: String = "Shape Layer",
    override val isVisible: Boolean = true,
    override val isLocked: Boolean = false,
    override val opacity: Float = 1.0f,
    override val blendMode: CanvasBlendMode = CanvasBlendMode.NORMAL,
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val scale: Float = 1.0f,
    override val rotation: Float = 0f,
    override val flipH: Boolean = false,
    override val flipV: Boolean = false,
    val shapeType: ShapeType = ShapeType.ROUNDED_RECT,
    val width: Float = 220f,
    val height: Float = 220f,
    val fillColor: Long = 0xFF007DFE,
    val strokeColor: Long = 0xFF00C7FF,
    val strokeWidth: Float = 4f,
    val cornerRadius: Float = 24f
) : Layer() {
    override fun copyWithCommon(
        id: String,
        name: String,
        isVisible: Boolean,
        isLocked: Boolean,
        opacity: Float,
        blendMode: CanvasBlendMode,
        x: Float,
        y: Float,
        scale: Float,
        rotation: Float,
        flipH: Boolean,
        flipV: Boolean
    ): Layer = copy(
        id = id,
        name = name,
        isVisible = isVisible,
        isLocked = isLocked,
        opacity = opacity,
        blendMode = blendMode,
        x = x,
        y = y,
        scale = scale,
        rotation = rotation,
        flipH = flipH,
        flipV = flipV
    )
}

data class DrawingLayer(
    override val id: String,
    override val name: String = "Brush Layer",
    override val isVisible: Boolean = true,
    override val isLocked: Boolean = false,
    override val opacity: Float = 1.0f,
    override val blendMode: CanvasBlendMode = CanvasBlendMode.NORMAL,
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val scale: Float = 1.0f,
    override val rotation: Float = 0f,
    override val flipH: Boolean = false,
    override val flipV: Boolean = false,
    val paths: List<DrawPath> = emptyList()
) : Layer() {
    override fun copyWithCommon(
        id: String,
        name: String,
        isVisible: Boolean,
        isLocked: Boolean,
        opacity: Float,
        blendMode: CanvasBlendMode,
        x: Float,
        y: Float,
        scale: Float,
        rotation: Float,
        flipH: Boolean,
        flipV: Boolean
    ): Layer = copy(
        id = id,
        name = name,
        isVisible = isVisible,
        isLocked = isLocked,
        opacity = opacity,
        blendMode = blendMode,
        x = x,
        y = y,
        scale = scale,
        rotation = rotation,
        flipH = flipH,
        flipV = flipV
    )
}

data class StickerLayer(
    override val id: String,
    override val name: String = "Graphic Stamp",
    override val isVisible: Boolean = true,
    override val isLocked: Boolean = false,
    override val opacity: Float = 1.0f,
    override val blendMode: CanvasBlendMode = CanvasBlendMode.NORMAL,
    override val x: Float = 0f,
    override val y: Float = 0f,
    override val scale: Float = 1.0f,
    override val rotation: Float = 0f,
    override val flipH: Boolean = false,
    override val flipV: Boolean = false,
    val stickerId: String = "badge_verified",
    val title: String = "VERIFIED",
    val tintColor: Long? = null,
    val width: Float = 160f,
    val height: Float = 160f
) : Layer() {
    override fun copyWithCommon(
        id: String,
        name: String,
        isVisible: Boolean,
        isLocked: Boolean,
        opacity: Float,
        blendMode: CanvasBlendMode,
        x: Float,
        y: Float,
        scale: Float,
        rotation: Float,
        flipH: Boolean,
        flipV: Boolean
    ): Layer = copy(
        id = id,
        name = name,
        isVisible = isVisible,
        isLocked = isLocked,
        opacity = opacity,
        blendMode = blendMode,
        x = x,
        y = y,
        scale = scale,
        rotation = rotation,
        flipH = flipH,
        flipV = flipV
    )
}

data class CanvasPreset(
    val name: String,
    val width: Int,
    val height: Int,
    val label: String
)

val STANDARD_CANVAS_PRESETS = listOf(
    CanvasPreset("Square 1:1", 1080, 1080, "Instagram / Post"),
    CanvasPreset("Portrait 4:5", 1080, 1350, "Feed / Editorial"),
    CanvasPreset("Story 9:16", 1080, 1920, "Reels / TikTok / Story"),
    CanvasPreset("Landscape 16:9", 1920, 1080, "YouTube / Wallpaper"),
    CanvasPreset("Banner 2:1", 1200, 600, "Header / Cover")
)
