package com.example.data.util

import com.example.model.CanvasBlendMode
import com.example.model.DrawPath
import com.example.model.DrawPoint
import com.example.model.DrawingLayer
import com.example.model.FilterPreset
import com.example.model.ImageLayer
import com.example.model.Layer
import com.example.model.ShapeLayer
import com.example.model.ShapeType
import com.example.model.StickerLayer
import com.example.model.TextFontFamily
import com.example.model.TextLayer
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.util.UUID

data class SerializedLayer(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val isVisible: Boolean = true,
    val isLocked: Boolean = false,
    val opacity: Float = 1.0f,
    val blendMode: String = "NORMAL",
    val x: Float = 0f,
    val y: Float = 0f,
    val scale: Float = 1.0f,
    val rotation: Float = 0f,
    val flipH: Boolean = false,
    val flipV: Boolean = false,
    // Text Layer properties
    val text: String = "",
    val fontSize: Float = 32f,
    val textColor: Long = 0xFFFFFFFFL,
    val fontFamily: String = "IMPACT",
    val isBold: Boolean = true,
    val isItalic: Boolean = false,
    val hasBackground: Boolean = false,
    val backgroundColor: Long = 0xCC000000L,
    val letterSpacing: Float = 1.0f,
    val shadowRadius: Float = 4.0f,
    val shadowColor: Long = 0xAA000000L,
    // Shape Layer properties
    val shapeType: String = "ROUNDED_RECT",
    val width: Float = 220f,
    val height: Float = 220f,
    val fillColor: Long = 0xFF007DFEL,
    val strokeColor: Long = 0xFF00C7FFL,
    val strokeWidth: Float = 4f,
    val cornerRadius: Float = 24f,
    // Sticker Layer properties
    val stickerId: String = "badge_verified",
    val title: String = "",
    val tintColor: Long? = null,
    // Image Layer properties
    val resourceId: Int = -1,
    val brightness: Float = 0f,
    val contrast: Float = 1f,
    val saturation: Float = 1f,
    val hue: Float = 0f,
    val warmth: Float = 0f,
    val vignette: Float = 0f,
    val filterPreset: String = "NONE"
)

object LayerSerializer {

    private val moshi: Moshi by lazy {
        Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    private val listType by lazy {
        Types.newParameterizedType(List::class.java, SerializedLayer::class.java)
    }

    private val adapter by lazy {
        moshi.adapter<List<SerializedLayer>>(listType)
    }

    fun serialize(layers: List<Layer>): String {
        val dtos = layers.map { layer ->
            when (layer) {
                is TextLayer -> SerializedLayer(
                    id = layer.id,
                    name = layer.name,
                    type = "text",
                    isVisible = layer.isVisible,
                    isLocked = layer.isLocked,
                    opacity = layer.opacity,
                    blendMode = layer.blendMode.name,
                    x = layer.x,
                    y = layer.y,
                    scale = layer.scale,
                    rotation = layer.rotation,
                    flipH = layer.flipH,
                    flipV = layer.flipV,
                    text = layer.text,
                    fontSize = layer.fontSize,
                    textColor = layer.textColor,
                    fontFamily = layer.fontFamily.name,
                    isBold = layer.isBold,
                    isItalic = layer.isItalic,
                    hasBackground = layer.hasBackground,
                    backgroundColor = layer.backgroundColor,
                    letterSpacing = layer.letterSpacing,
                    shadowRadius = layer.shadowRadius,
                    shadowColor = layer.shadowColor
                )
                is ShapeLayer -> SerializedLayer(
                    id = layer.id,
                    name = layer.name,
                    type = "shape",
                    isVisible = layer.isVisible,
                    isLocked = layer.isLocked,
                    opacity = layer.opacity,
                    blendMode = layer.blendMode.name,
                    x = layer.x,
                    y = layer.y,
                    scale = layer.scale,
                    rotation = layer.rotation,
                    flipH = layer.flipH,
                    flipV = layer.flipV,
                    shapeType = layer.shapeType.name,
                    width = layer.width,
                    height = layer.height,
                    fillColor = layer.fillColor,
                    strokeColor = layer.strokeColor,
                    strokeWidth = layer.strokeWidth,
                    cornerRadius = layer.cornerRadius
                )
                is StickerLayer -> SerializedLayer(
                    id = layer.id,
                    name = layer.name,
                    type = "sticker",
                    isVisible = layer.isVisible,
                    isLocked = layer.isLocked,
                    opacity = layer.opacity,
                    blendMode = layer.blendMode.name,
                    x = layer.x,
                    y = layer.y,
                    scale = layer.scale,
                    rotation = layer.rotation,
                    flipH = layer.flipH,
                    flipV = layer.flipV,
                    stickerId = layer.stickerId,
                    title = layer.title,
                    tintColor = layer.tintColor,
                    width = layer.width,
                    height = layer.height
                )
                is ImageLayer -> SerializedLayer(
                    id = layer.id,
                    name = layer.name,
                    type = "image",
                    isVisible = layer.isVisible,
                    isLocked = layer.isLocked,
                    opacity = layer.opacity,
                    blendMode = layer.blendMode.name,
                    x = layer.x,
                    y = layer.y,
                    scale = layer.scale,
                    rotation = layer.rotation,
                    flipH = layer.flipH,
                    flipV = layer.flipV,
                    resourceId = layer.resourceId ?: -1,
                    width = layer.width,
                    height = layer.height,
                    brightness = layer.brightness,
                    contrast = layer.contrast,
                    saturation = layer.saturation,
                    hue = layer.hue,
                    warmth = layer.warmth,
                    vignette = layer.vignette,
                    filterPreset = layer.filterPreset.name
                )
                is DrawingLayer -> SerializedLayer(
                    id = layer.id,
                    name = layer.name,
                    type = "drawing",
                    isVisible = layer.isVisible,
                    isLocked = layer.isLocked,
                    opacity = layer.opacity,
                    blendMode = layer.blendMode.name,
                    x = layer.x,
                    y = layer.y,
                    scale = layer.scale,
                    rotation = layer.rotation,
                    flipH = layer.flipH,
                    flipV = layer.flipV
                )
            }
        }
        return adapter.toJson(dtos)
    }

    fun deserialize(jsonString: String): List<Layer> {
        val dtos = try {
            adapter.fromJson(jsonString) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
        return dtos.map { dto ->
            val id = dto.id.ifBlank { UUID.randomUUID().toString() }
            val blendMode = try {
                CanvasBlendMode.valueOf(dto.blendMode)
            } catch (e: Exception) {
                CanvasBlendMode.NORMAL
            }
            when (dto.type) {
                "text" -> {
                    val family = try {
                        TextFontFamily.valueOf(dto.fontFamily)
                    } catch (e: Exception) {
                        TextFontFamily.IMPACT
                    }
                    TextLayer(
                        id = id,
                        name = dto.name.ifBlank { "Text Layer" },
                        isVisible = dto.isVisible,
                        isLocked = dto.isLocked,
                        opacity = dto.opacity,
                        blendMode = blendMode,
                        x = dto.x,
                        y = dto.y,
                        scale = dto.scale,
                        rotation = dto.rotation,
                        flipH = dto.flipH,
                        flipV = dto.flipV,
                        text = dto.text,
                        fontSize = dto.fontSize,
                        textColor = dto.textColor,
                        fontFamily = family,
                        isBold = dto.isBold,
                        isItalic = dto.isItalic,
                        hasBackground = dto.hasBackground,
                        backgroundColor = dto.backgroundColor,
                        letterSpacing = dto.letterSpacing,
                        shadowRadius = dto.shadowRadius,
                        shadowColor = dto.shadowColor
                    )
                }
                "shape" -> {
                    val shape = try {
                        ShapeType.valueOf(dto.shapeType)
                    } catch (e: Exception) {
                        ShapeType.ROUNDED_RECT
                    }
                    ShapeLayer(
                        id = id,
                        name = dto.name.ifBlank { "Shape Layer" },
                        isVisible = dto.isVisible,
                        isLocked = dto.isLocked,
                        opacity = dto.opacity,
                        blendMode = blendMode,
                        x = dto.x,
                        y = dto.y,
                        scale = dto.scale,
                        rotation = dto.rotation,
                        flipH = dto.flipH,
                        flipV = dto.flipV,
                        shapeType = shape,
                        width = dto.width,
                        height = dto.height,
                        fillColor = dto.fillColor,
                        strokeColor = dto.strokeColor,
                        strokeWidth = dto.strokeWidth,
                        cornerRadius = dto.cornerRadius
                    )
                }
                "sticker" -> {
                    StickerLayer(
                        id = id,
                        name = dto.name.ifBlank { "Badge Layer" },
                        isVisible = dto.isVisible,
                        isLocked = dto.isLocked,
                        opacity = dto.opacity,
                        blendMode = blendMode,
                        x = dto.x,
                        y = dto.y,
                        scale = dto.scale,
                        rotation = dto.rotation,
                        flipH = dto.flipH,
                        flipV = dto.flipV,
                        stickerId = dto.stickerId,
                        title = dto.title,
                        tintColor = dto.tintColor,
                        width = dto.width,
                        height = dto.height
                    )
                }
                "image" -> {
                    val preset = try {
                        FilterPreset.valueOf(dto.filterPreset)
                    } catch (e: Exception) {
                        FilterPreset.NONE
                    }
                    ImageLayer(
                        id = id,
                        name = dto.name.ifBlank { "Image Layer" },
                        isVisible = dto.isVisible,
                        isLocked = dto.isLocked,
                        opacity = dto.opacity,
                        blendMode = blendMode,
                        x = dto.x,
                        y = dto.y,
                        scale = dto.scale,
                        rotation = dto.rotation,
                        flipH = dto.flipH,
                        flipV = dto.flipV,
                        resourceId = if (dto.resourceId > 0) dto.resourceId else null,
                        width = dto.width,
                        height = dto.height,
                        brightness = dto.brightness,
                        contrast = dto.contrast,
                        saturation = dto.saturation,
                        hue = dto.hue,
                        warmth = dto.warmth,
                        vignette = dto.vignette,
                        filterPreset = preset
                    )
                }
                else -> {
                    DrawingLayer(
                        id = id,
                        name = dto.name.ifBlank { "Drawing Layer" },
                        isVisible = dto.isVisible,
                        isLocked = dto.isLocked,
                        opacity = dto.opacity,
                        blendMode = blendMode,
                        x = dto.x,
                        y = dto.y,
                        scale = dto.scale,
                        rotation = dto.rotation,
                        flipH = dto.flipH,
                        flipV = dto.flipV
                    )
                }
            }
        }
    }
}
