package com.example

import com.example.model.CanvasBlendMode
import com.example.model.CanvasPreset
import com.example.model.EditorTool
import com.example.model.FilterPreset
import com.example.model.ImageLayer
import com.example.model.STANDARD_CANVAS_PRESETS
import com.example.model.ShapeLayer
import com.example.model.ShapeType
import com.example.model.StickerLayer
import com.example.model.TextFontFamily
import com.example.model.TextLayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testCanvasPresets() {
        assertTrue(STANDARD_CANVAS_PRESETS.isNotEmpty())
        val square = STANDARD_CANVAS_PRESETS.first { it.name.startsWith("Square") }
        assertEquals(1080, square.width)
        assertEquals(1080, square.height)

        val story = STANDARD_CANVAS_PRESETS.first { it.name.startsWith("Story") }
        assertEquals(1080, story.width)
        assertEquals(1920, story.height)
    }

    @Test
    fun testTextLayerPropertiesAndCopy() {
        val textLayer = TextLayer(
            id = "text_1",
            name = "Title",
            text = "NEON REVOLUTION",
            fontSize = 42f,
            fontFamily = TextFontFamily.IMPACT,
            isBold = true,
            hasBackground = true,
            backgroundColor = 0xAA000000
        )

        assertEquals("NEON REVOLUTION", textLayer.text)
        assertTrue(textLayer.isBold)
        assertTrue(textLayer.hasBackground)

        val transformed = textLayer.copyWithCommon(
            x = 100f,
            y = 200f,
            scale = 1.5f,
            rotation = 45f,
            blendMode = CanvasBlendMode.MULTIPLY
        )

        assertEquals(100f, transformed.x, 0.01f)
        assertEquals(200f, transformed.y, 0.01f)
        assertEquals(1.5f, transformed.scale, 0.01f)
        assertEquals(45f, transformed.rotation, 0.01f)
        assertEquals(CanvasBlendMode.MULTIPLY, transformed.blendMode)
    }

    @Test
    fun testShapeLayerCreation() {
        val shape = ShapeLayer(
            id = "shape_1",
            shapeType = ShapeType.ROUNDED_RECT,
            width = 300f,
            height = 300f,
            fillColor = 0xFF007DFE,
            strokeColor = 0xFF00C7FF,
            strokeWidth = 6f,
            cornerRadius = 32f
        )

        assertEquals(ShapeType.ROUNDED_RECT, shape.shapeType)
        assertEquals(6f, shape.strokeWidth, 0.01f)
        assertEquals(32f, shape.cornerRadius, 0.01f)
        assertFalse(shape.isLocked)
        assertTrue(shape.isVisible)
    }

    @Test
    fun testImageLayerAdjustments() {
        val imageLayer = ImageLayer(
            id = "img_1",
            name = "Background",
            brightness = 0.2f,
            contrast = 1.3f,
            saturation = 1.5f,
            hue = 45f,
            warmth = 0.1f,
            filterPreset = FilterPreset.CYBERPUNK
        )

        assertEquals(0.2f, imageLayer.brightness, 0.01f)
        assertEquals(1.3f, imageLayer.contrast, 0.01f)
        assertEquals(1.5f, imageLayer.saturation, 0.01f)
        assertEquals(FilterPreset.CYBERPUNK, imageLayer.filterPreset)
    }

    @Test
    fun testBlendModesMapping() {
        val modes = CanvasBlendMode.values()
        assertEquals(10, modes.size)
        assertTrue(modes.contains(CanvasBlendMode.NORMAL))
        assertTrue(modes.contains(CanvasBlendMode.MULTIPLY))
        assertTrue(modes.contains(CanvasBlendMode.SCREEN))
        assertTrue(modes.contains(CanvasBlendMode.OVERLAY))
        assertTrue(modes.contains(CanvasBlendMode.COLOR_DODGE))
    }

    @Test
    fun testEditorTools() {
        val tools = EditorTool.values()
        assertEquals(9, tools.size)
        assertEquals("V", EditorTool.MOVE.shortcut)
        assertEquals("B", EditorTool.BRUSH.shortcut)
        assertEquals("E", EditorTool.ERASER.shortcut)
        assertEquals("T", EditorTool.TEXT.shortcut)
        assertEquals("U", EditorTool.SHAPES.shortcut)
        assertEquals("A", EditorTool.ADJUST.shortcut)
        assertEquals("F", EditorTool.FILTERS.shortcut)
    }
}
