package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.engine.ImageProcessor
import com.example.model.CanvasBlendMode
import com.example.model.DrawPoint
import com.example.model.DrawingLayer
import com.example.model.EditorTool
import com.example.model.ImageLayer
import com.example.model.Layer
import com.example.model.ShapeLayer
import com.example.model.ShapeType
import com.example.model.StickerLayer
import com.example.model.TextFontFamily
import com.example.model.TextLayer
import com.example.ui.theme.AdobeBlue
import com.example.ui.theme.AdobeCyan
import com.example.ui.theme.CheckerboardDark
import com.example.ui.theme.CheckerboardLight
import com.example.viewmodel.EditorUiState
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun CanvasView(
    uiState: EditorUiState,
    onLayerSelected: (String?) -> Unit,
    onLayerTransformed: (String, Float, Float, Float, Float) -> Unit,
    onTransformFinished: () -> Unit,
    onDrawingStart: (DrawPoint) -> Unit,
    onDrawingMove: (DrawPoint) -> Unit,
    onDrawingEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("canvas_viewport_container"),
        contentAlignment = Alignment.Center
    ) {
        val containerWidth = constraints.maxWidth.toFloat()
        val containerHeight = constraints.maxHeight.toFloat()

        // Calculate fitting scale so canvas fits comfortably within viewport
        val canvasAspect = uiState.canvasWidth.toFloat() / uiState.canvasHeight.toFloat()
        val containerAspect = containerWidth / containerHeight

        val baseFitScale = if (canvasAspect > containerAspect) {
            (containerWidth * 0.90f) / uiState.canvasWidth.toFloat()
        } else {
            (containerHeight * 0.90f) / uiState.canvasHeight.toFloat()
        }

        val totalScale = baseFitScale * uiState.zoomScale
        val renderedCanvasW = uiState.canvasWidth.toFloat() * totalScale
        val renderedCanvasH = uiState.canvasHeight.toFloat() * totalScale

        val canvasOffsetX = (containerWidth - renderedCanvasW) / 2f + uiState.panX
        val canvasOffsetY = (containerHeight - renderedCanvasH) / 2f + uiState.panY

        val densityVal = density.density

        // Canvas Surface
        Canvas(
            modifier = Modifier
                .offset { IntOffset(canvasOffsetX.roundToInt(), canvasOffsetY.roundToInt()) }
                .size(
                    width = with(density) { renderedCanvasW.toDp() },
                    height = with(density) { renderedCanvasH.toDp() }
                )
                .testTag("interactive_canvas")
                .pointerInput(uiState.activeTool, uiState.selectedLayerId, totalScale) {
                    when (uiState.activeTool) {
                        EditorTool.BRUSH, EditorTool.ERASER -> {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val canvasX = offset.x / totalScale
                                    val canvasY = offset.y / totalScale
                                    onDrawingStart(DrawPoint(canvasX, canvasY))
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val canvasX = change.position.x / totalScale
                                    val canvasY = change.position.y / totalScale
                                    onDrawingMove(DrawPoint(canvasX, canvasY))
                                },
                                onDragEnd = { onDrawingEnd() },
                                onDragCancel = { onDrawingEnd() }
                            )
                        }
                        EditorTool.MOVE -> {
                            detectTransformGestures { _, pan, zoom, rotation ->
                                val selectedId = uiState.selectedLayerId
                                if (selectedId != null) {
                                    val dx = pan.x / totalScale
                                    val dy = pan.y / totalScale
                                    onLayerTransformed(selectedId, dx, dy, zoom, rotation)
                                }
                            }
                        }
                        else -> {
                            // Default tap to select
                        }
                    }
                }
                .pointerInput(uiState.activeTool, uiState.layers, totalScale) {
                    if (uiState.activeTool == EditorTool.MOVE) {
                        detectTapGestures(
                            onTap = { tapOffset ->
                                val touchX = tapOffset.x / totalScale
                                val touchY = tapOffset.y / totalScale

                                // Hit test from top layer to bottom layer
                                var hitId: String? = null
                                for (i in uiState.layers.indices.reversed()) {
                                    val layer = uiState.layers[i]
                                    if (!layer.isVisible || layer.isLocked) continue

                                    val halfW = when (layer) {
                                        is ImageLayer -> layer.width / 2f * layer.scale
                                        is TextLayer -> 220f * layer.scale
                                        is ShapeLayer -> layer.width / 2f * layer.scale
                                        is StickerLayer -> layer.width / 2f * layer.scale
                                        is DrawingLayer -> 300f * layer.scale
                                    }
                                    val halfH = when (layer) {
                                        is ImageLayer -> layer.height / 2f * layer.scale
                                        is TextLayer -> 60f * layer.scale
                                        is ShapeLayer -> layer.height / 2f * layer.scale
                                        is StickerLayer -> layer.height / 2f * layer.scale
                                        is DrawingLayer -> 300f * layer.scale
                                    }

                                    if (touchX >= layer.x - halfW && touchX <= layer.x + halfW &&
                                        touchY >= layer.y - halfH && touchY <= layer.y + halfH
                                    ) {
                                        hitId = layer.id
                                        break
                                    }
                                }
                                onLayerSelected(hitId)
                            }
                        )
                    }
                }
        ) {
            // Scale scope to virtual canvas coordinate system (e.g. 1080x1080)
            scale(scaleX = totalScale, scaleY = totalScale, pivot = Offset.Zero) {
                // 1. Draw Transparency Checkerboard
                val squareSize = 32f
                val cols = (uiState.canvasWidth / squareSize).toInt() + 1
                val rows = (uiState.canvasHeight / squareSize).toInt() + 1
                for (r in 0 until rows) {
                    for (c in 0 until cols) {
                        val isEven = (r + c) % 2 == 0
                        val color = if (isEven) CheckerboardLight else CheckerboardDark
                        drawRect(
                            color = color,
                            topLeft = Offset(c * squareSize, r * squareSize),
                            size = Size(squareSize, squareSize)
                        )
                    }
                }

                // 2. Draw Solid Background if defined
                uiState.backgroundColor?.let { bg ->
                    drawRect(
                        color = Color(bg),
                        topLeft = Offset.Zero,
                        size = Size(uiState.canvasWidth.toFloat(), uiState.canvasHeight.toFloat())
                    )
                }

                // 3. Render Layers in Order
                for (layer in uiState.layers) {
                    if (!layer.isVisible) continue

                    translate(left = layer.x, top = layer.y) {
                        rotate(degrees = layer.rotation) {
                            val sx = if (layer.flipH) -layer.scale else layer.scale
                            val sy = if (layer.flipV) -layer.scale else layer.scale
                            scale(scaleX = sx, scaleY = sy, pivot = Offset.Zero) {
                                when (layer) {
                                    is ImageLayer -> drawImageLayer(context, layer)
                                    is TextLayer -> drawTextLayer(layer)
                                    is ShapeLayer -> drawShapeLayer(layer)
                                    is DrawingLayer -> drawDrawingLayer(layer)
                                    is StickerLayer -> drawStickerLayer(layer)
                                }
                            }
                        }
                    }
                }

                // 4. Render Active In-Progress Brush Stroke
                if (uiState.currentDrawingPath.isNotEmpty()) {
                    val path = Path()
                    path.moveTo(uiState.currentDrawingPath[0].x, uiState.currentDrawingPath[0].y)
                    for (pt in uiState.currentDrawingPath) {
                        path.lineTo(pt.x, pt.y)
                    }
                    drawPath(
                        path = path,
                        color = if (uiState.isEraser) Color.Black else Color(uiState.brushColor).copy(alpha = uiState.brushAlpha),
                        style = Stroke(
                            width = uiState.brushSize,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // 5. Draw Transform Bounding Box for Selected Layer (Photoshop Style!)
                val selected = uiState.layers.find { it.id == uiState.selectedLayerId }
                if (selected != null && uiState.activeTool == EditorTool.MOVE) {
                    drawSelectionOverlay(selected)
                }

                // 6. Crop / Rule-of-Thirds Grid if CROP Tool active
                if (uiState.activeTool == EditorTool.CROP) {
                    val w = uiState.canvasWidth.toFloat()
                    val h = uiState.canvasHeight.toFloat()
                    val gridColor = AdobeCyan.copy(alpha = 0.5f)
                    val gridStroke = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f)))

                    drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 2f)
                    drawLine(gridColor, Offset(w * 2f / 3f, 0f), Offset(w * 2f / 3f, h), strokeWidth = 2f)
                    drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 2f)
                    drawLine(gridColor, Offset(0f, h * 2f / 3f), Offset(w, h * 2f / 3f), strokeWidth = 2f)

                    drawRect(AdobeCyan, Offset.Zero, Size(w, h), style = Stroke(width = 4f))
                }
            }
        }
    }
}

private fun DrawScope.drawImageLayer(context: android.content.Context, layer: ImageLayer) {
    val bmp = when {
        layer.bitmap != null -> layer.bitmap
        layer.resourceId != null -> BitmapFactory.decodeResource(context.resources, layer.resourceId)
        else -> null
    }

    if (bmp != null) {
        val halfW = layer.width / 2f
        val halfH = layer.height / 2f

        drawIntoCanvas { canvas ->
            val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                alpha = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()
                xfermode = android.graphics.PorterDuffXfermode(ImageProcessor.toPorterDuff(layer.blendMode))
                colorFilter = ImageProcessor.getColorFilter(
                    layer.brightness,
                    layer.contrast,
                    layer.saturation,
                    layer.hue,
                    layer.warmth,
                    layer.filterPreset
                )
            }
            val dst = android.graphics.RectF(-halfW, -halfH, halfW, halfH)
            canvas.nativeCanvas.drawBitmap(bmp, null, dst, paint)
        }
    }
}

private fun DrawScope.drawTextLayer(layer: TextLayer) {
    drawIntoCanvas { canvas ->
        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = layer.textColor.toInt()
            alpha = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()
            textSize = layer.fontSize * 1.5f
            textAlign = android.graphics.Paint.Align.CENTER
            xfermode = android.graphics.PorterDuffXfermode(ImageProcessor.toPorterDuff(layer.blendMode))

            val style = when {
                layer.isBold && layer.isItalic -> Typeface.BOLD_ITALIC
                layer.isBold -> Typeface.BOLD
                layer.isItalic -> Typeface.ITALIC
                else -> Typeface.NORMAL
            }

            val family = when (layer.fontFamily) {
                TextFontFamily.DEFAULT -> Typeface.SANS_SERIF
                TextFontFamily.SERIF -> Typeface.SERIF
                TextFontFamily.MONOSPACE -> Typeface.MONOSPACE
                TextFontFamily.CURSIVE -> Typeface.create("cursive", style)
                TextFontFamily.IMPACT -> Typeface.DEFAULT_BOLD
            }
            typeface = Typeface.create(family, style)

            if (layer.shadowRadius > 0f) {
                setShadowLayer(layer.shadowRadius * 2f, 2f, 4f, layer.shadowColor.toInt())
            }
        }

        val bounds = android.graphics.Rect()
        textPaint.getTextBounds(layer.text, 0, layer.text.length, bounds)

        if (layer.hasBackground) {
            val bgPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.backgroundColor.toInt()
                alpha = ((layer.backgroundColor ushr 24) * layer.opacity).toInt()
                style = android.graphics.Paint.Style.FILL
            }
            val rect = android.graphics.RectF(
                -bounds.width() / 2f - 30f,
                -bounds.height() / 2f - 18f,
                bounds.width() / 2f + 30f,
                bounds.height() / 2f + 18f
            )
            canvas.nativeCanvas.drawRoundRect(rect, 16f, 16f, bgPaint)
        }

        val yOff = (bounds.height() / 2f) - bounds.bottom
        canvas.nativeCanvas.drawText(layer.text, 0f, yOff, textPaint)
    }
}

private fun DrawScope.drawShapeLayer(layer: ShapeLayer) {
    val halfW = layer.width / 2f
    val halfH = layer.height / 2f
    val rect = android.graphics.RectF(-halfW, -halfH, halfW, halfH)

    drawIntoCanvas { canvas ->
        val fillPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = layer.fillColor.toInt()
            alpha = ((layer.fillColor ushr 24) * layer.opacity).toInt().coerceIn(0, 255)
            style = android.graphics.Paint.Style.FILL
            xfermode = android.graphics.PorterDuffXfermode(ImageProcessor.toPorterDuff(layer.blendMode))
        }

        val strokePaint = if (layer.strokeWidth > 0f) {
            android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.strokeColor.toInt()
                alpha = ((layer.strokeColor ushr 24) * layer.opacity).toInt().coerceIn(0, 255)
                style = android.graphics.Paint.Style.STROKE
                strokeWidth = layer.strokeWidth
                xfermode = android.graphics.PorterDuffXfermode(ImageProcessor.toPorterDuff(layer.blendMode))
            }
        } else null

        val path = createShapeAndroidPath(layer.shapeType, rect, layer.cornerRadius)
        canvas.nativeCanvas.drawPath(path, fillPaint)
        strokePaint?.let { canvas.nativeCanvas.drawPath(path, it) }
    }
}

private fun createShapeAndroidPath(shapeType: ShapeType, rect: android.graphics.RectF, cornerRadius: Float): android.graphics.Path {
    val path = android.graphics.Path()
    when (shapeType) {
        ShapeType.RECTANGLE -> path.addRect(rect, android.graphics.Path.Direction.CW)
        ShapeType.ROUNDED_RECT -> path.addRoundRect(rect, cornerRadius, cornerRadius, android.graphics.Path.Direction.CW)
        ShapeType.CIRCLE -> path.addOval(rect, android.graphics.Path.Direction.CW)
        ShapeType.STAR -> {
            val cx = rect.centerX()
            val cy = rect.centerY()
            val rOuter = minOf(rect.width(), rect.height()) / 2f
            val rInner = rOuter * 0.45f
            val numPoints = 5
            val angleStep = Math.PI / numPoints
            var angle = -Math.PI / 2.0
            path.moveTo((cx + rOuter * cos(angle)).toFloat(), (cy + rOuter * sin(angle)).toFloat())
            for (i in 0 until numPoints * 2) {
                val r = if (i % 2 == 0) rInner else rOuter
                angle += angleStep
                path.lineTo((cx + r * cos(angle)).toFloat(), (cy + r * sin(angle)).toFloat())
            }
            path.close()
        }
        ShapeType.TRIANGLE -> {
            path.moveTo(rect.centerX(), rect.top)
            path.lineTo(rect.right, rect.bottom)
            path.lineTo(rect.left, rect.bottom)
            path.close()
        }
        ShapeType.HEART -> {
            val w = rect.width()
            val h = rect.height()
            val x = rect.left
            val y = rect.top
            path.moveTo(x + w / 2f, y + h / 4f)
            path.cubicTo(x + w / 2f, y, x, y, x, y + h / 3f)
            path.cubicTo(x, y + h * 0.6f, x + w / 2f, y + h * 0.85f, x + w / 2f, y + h)
            path.cubicTo(x + w / 2f, y + h * 0.85f, x + w, y + h * 0.6f, x + w, y + h / 3f)
            path.cubicTo(x + w, y, x + w / 2f, y, x + w / 2f, y + h / 4f)
            path.close()
        }
        ShapeType.HEXAGON -> {
            val cx = rect.centerX()
            val cy = rect.centerY()
            val r = minOf(rect.width(), rect.height()) / 2f
            for (i in 0 until 6) {
                val rad = Math.toRadians((60 * i - 30).toDouble())
                val px = (cx + r * cos(rad)).toFloat()
                val py = (cy + r * sin(rad)).toFloat()
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
        }
        ShapeType.ARROW -> {
            val w = rect.width()
            val h = rect.height()
            val left = rect.left
            val top = rect.top
            path.moveTo(left, top + h * 0.35f)
            path.lineTo(left + w * 0.55f, top + h * 0.35f)
            path.lineTo(left + w * 0.55f, top + h * 0.15f)
            path.lineTo(left + w, top + h * 0.5f)
            path.lineTo(left + w * 0.55f, top + h * 0.85f)
            path.lineTo(left + w * 0.55f, top + h * 0.65f)
            path.lineTo(left, top + h * 0.65f)
            path.close()
        }
        ShapeType.LINE -> {
            path.moveTo(rect.left, rect.centerY())
            path.lineTo(rect.right, rect.centerY())
        }
    }
    return path
}

private fun DrawScope.drawDrawingLayer(layer: DrawingLayer) {
    for (dp in layer.paths) {
        if (dp.points.isEmpty()) continue
        val path = Path()
        path.moveTo(dp.points[0].x, dp.points[0].y)
        for (i in 1 until dp.points.size) {
            path.lineTo(dp.points[i].x, dp.points[i].y)
        }
        drawPath(
            path = path,
            color = Color(dp.color).copy(alpha = layer.opacity * dp.alpha),
            style = Stroke(
                width = dp.strokeWidth,
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
    }
}

private fun DrawScope.drawStickerLayer(layer: StickerLayer) {
    val halfW = layer.width / 2f
    val halfH = layer.height / 2f
    val rect = android.graphics.RectF(-halfW, -halfH, halfW, halfH)

    drawIntoCanvas { canvas ->
        val badgePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            style = android.graphics.Paint.Style.FILL
            alpha = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()
        }
        val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 24f
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            alpha = badgePaint.alpha
        }

        when (layer.stickerId) {
            "badge_verified" -> {
                badgePaint.color = 0xFF007DFE.toInt()
                canvas.nativeCanvas.drawRoundRect(rect, 30f, 30f, badgePaint)
                canvas.nativeCanvas.drawText("✓ VERIFIED", 0f, 8f, textPaint)
            }
            "badge_sale" -> {
                badgePaint.color = 0xFFFF3B30.toInt()
                canvas.nativeCanvas.drawRoundRect(rect, 16f, 16f, badgePaint)
                canvas.nativeCanvas.drawText("🔥 50% OFF", 0f, 8f, textPaint)
            }
            "badge_new" -> {
                badgePaint.color = 0xFF00E676.toInt()
                canvas.nativeCanvas.drawRoundRect(rect, 24f, 24f, badgePaint)
                textPaint.color = android.graphics.Color.BLACK
                canvas.nativeCanvas.drawText("★ NEW ARRIVAL", 0f, 8f, textPaint)
            }
            "badge_limited" -> {
                badgePaint.color = 0xFF8A58FF.toInt()
                canvas.nativeCanvas.drawRoundRect(rect, 24f, 24f, badgePaint)
                canvas.nativeCanvas.drawText("LIMITED EDITION", 0f, 8f, textPaint)
            }
            else -> {
                badgePaint.color = 0xFF00C7FF.toInt()
                canvas.nativeCanvas.drawRoundRect(rect, 20f, 20f, badgePaint)
                canvas.nativeCanvas.drawText(layer.title, 0f, 8f, textPaint)
            }
        }
    }
}

private fun DrawScope.drawSelectionOverlay(layer: Layer) {
    translate(left = layer.x, top = layer.y) {
        rotate(degrees = layer.rotation) {
            val halfW = when (layer) {
                is ImageLayer -> layer.width / 2f * layer.scale
                is TextLayer -> 220f * layer.scale
                is ShapeLayer -> layer.width / 2f * layer.scale
                is StickerLayer -> layer.width / 2f * layer.scale
                is DrawingLayer -> 300f * layer.scale
            } + 12f

            val halfH = when (layer) {
                is ImageLayer -> layer.height / 2f * layer.scale
                is TextLayer -> 60f * layer.scale
                is ShapeLayer -> layer.height / 2f * layer.scale
                is StickerLayer -> layer.height / 2f * layer.scale
                is DrawingLayer -> 300f * layer.scale
            } + 12f

            // Dashed cyan boundary box
            drawRect(
                color = AdobeCyan,
                topLeft = Offset(-halfW, -halfH),
                size = Size(halfW * 2f, halfH * 2f),
                style = Stroke(
                    width = 2.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f))
                )
            )

            // Corner anchor handles
            val handleSize = 14f
            val cornerCoords = listOf(
                Offset(-halfW, -halfH),
                Offset(halfW, -halfH),
                Offset(halfW, halfH),
                Offset(-halfW, halfH),
                Offset(0f, -halfH),
                Offset(0f, halfH),
                Offset(-halfW, 0f),
                Offset(halfW, 0f)
            )

            for (c in cornerCoords) {
                drawCircle(color = Color.White, radius = handleSize / 2f, center = c)
                drawCircle(color = AdobeBlue, radius = handleSize / 2f, center = c, style = Stroke(width = 2f))
            }

            // Top rotation pivot stem
            val rotStemY = -halfH - 30f
            drawLine(color = AdobeCyan, start = Offset(0f, -halfH), end = Offset(0f, rotStemY), strokeWidth = 2f)
            drawCircle(color = Color.White, radius = 9f, center = Offset(0f, rotStemY))
            drawCircle(color = AdobeCyan, radius = 9f, center = Offset(0f, rotStemY), style = Stroke(width = 2.5f))
        }
    }
}
