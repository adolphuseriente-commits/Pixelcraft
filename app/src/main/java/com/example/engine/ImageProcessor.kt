package com.example.engine

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.model.CanvasBlendMode
import com.example.model.DrawingLayer
import com.example.model.FilterPreset
import com.example.model.ImageLayer
import com.example.model.Layer
import com.example.model.ShapeLayer
import com.example.model.ShapeType
import com.example.model.StickerLayer
import com.example.model.TextFontFamily
import com.example.model.TextLayer
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.cos
import kotlin.math.sin

object ImageProcessor {

    fun createAdjustmentMatrix(
        brightness: Float,  // -1 to 1
        contrast: Float,    // 0.2 to 2.5
        saturation: Float,  // 0 to 2.5
        hue: Float,         // -180 to 180
        warmth: Float,      // -1 to 1
        preset: FilterPreset
    ): ColorMatrix {
        val matrix = ColorMatrix()

        // 1. Filter Preset base
        when (preset) {
            FilterPreset.NONE -> matrix.reset()
            FilterPreset.CYBERPUNK -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.2f, 0.0f, 0.2f, 0.0f, 20f,
                        0.0f, 1.1f, 0.3f, 0.0f, 10f,
                        0.2f, 0.3f, 1.6f, 0.0f, 40f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                matrix.postConcat(cm)
            }
            FilterPreset.FILM_NOIR -> {
                val sat = ColorMatrix()
                sat.setSaturation(0f)
                val cont = ColorMatrix(
                    floatArrayOf(
                        1.5f, 0f, 0f, 0f, -30f,
                        0f, 1.5f, 0f, 0f, -30f,
                        0f, 0f, 1.5f, 0f, -30f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(sat)
                matrix.postConcat(cont)
            }
            FilterPreset.GOLDEN_HOUR -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.25f, 0.1f, 0.0f, 0.0f, 30f,
                        0.05f, 1.15f, 0.0f, 0.0f, 15f,
                        0.0f, 0.0f, 0.85f, 0.0f, -20f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                matrix.postConcat(cm)
            }
            FilterPreset.PASTEL -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        0.9f, 0.1f, 0.1f, 0.0f, 40f,
                        0.1f, 0.9f, 0.1f, 0.0f, 40f,
                        0.1f, 0.1f, 0.9f, 0.0f, 50f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                val sat = ColorMatrix()
                sat.setSaturation(0.75f)
                matrix.postConcat(cm)
                matrix.postConcat(sat)
            }
            FilterPreset.DUOTONE -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        1.3f, 0.0f, 0.0f, 0.0f, 45f,
                        0.0f, 0.6f, 0.4f, 0.0f, 0f,
                        0.1f, 0.4f, 1.5f, 0.0f, 40f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                matrix.postConcat(cm)
            }
            FilterPreset.SEPIA -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        0.393f, 0.769f, 0.189f, 0f, 0f,
                        0.349f, 0.686f, 0.168f, 0f, 0f,
                        0.272f, 0.534f, 0.131f, 0f, 0f,
                        0.0f, 0.0f, 0.0f, 1f, 0f
                    )
                )
                matrix.postConcat(cm)
            }
            FilterPreset.MONOCHROME -> {
                val sat = ColorMatrix()
                sat.setSaturation(0f)
                matrix.postConcat(sat)
            }
            FilterPreset.VIVID -> {
                val sat = ColorMatrix()
                sat.setSaturation(1.5f)
                val cont = ColorMatrix(
                    floatArrayOf(
                        1.2f, 0f, 0f, 0f, -10f,
                        0f, 1.2f, 0f, 0f, -10f,
                        0f, 0f, 1.2f, 0f, -10f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                matrix.postConcat(sat)
                matrix.postConcat(cont)
            }
            FilterPreset.VINTAGE -> {
                val cm = ColorMatrix(
                    floatArrayOf(
                        0.9f, 0.0f, 0.0f, 0.0f, 25f,
                        0.0f, 0.85f, 0.0f, 0.0f, 10f,
                        0.0f, 0.0f, 0.75f, 0.0f, 5f,
                        0.0f, 0.0f, 0.0f, 1.0f, 0f
                    )
                )
                matrix.postConcat(cm)
            }
        }

        // 2. Brightness (-1 to 1 => -255 to 255)
        if (brightness != 0f) {
            val b = brightness * 255f
            val brightMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, b,
                    0f, 1f, 0f, 0f, b,
                    0f, 0f, 1f, 0f, b,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(brightMatrix)
        }

        // 3. Contrast (0.2 to 2.5, default 1.0)
        if (contrast != 1f) {
            val scale = contrast
            val translate = (-0.5f * scale + 0.5f) * 255f
            val contrastMatrix = ColorMatrix(
                floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(contrastMatrix)
        }

        // 4. Saturation
        if (saturation != 1f) {
            val satMatrix = ColorMatrix()
            satMatrix.setSaturation(saturation)
            matrix.postConcat(satMatrix)
        }

        // 5. Warmth (-1 to 1)
        if (warmth != 0f) {
            val rShift = warmth * 30f
            val bShift = -warmth * 30f
            val warmthMatrix = ColorMatrix(
                floatArrayOf(
                    1f, 0f, 0f, 0f, rShift,
                    0f, 1f, 0f, 0f, 0f,
                    0f, 0f, 1f, 0f, bShift,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(warmthMatrix)
        }

        // 6. Hue rotation (-180 to 180)
        if (hue != 0f) {
            val rad = Math.toRadians(hue.toDouble())
            val cosVal = cos(rad).toFloat()
            val sinVal = sin(rad).toFloat()
            val lumR = 0.213f
            val lumG = 0.715f
            val lumB = 0.072f

            val hueMatrix = ColorMatrix(
                floatArrayOf(
                    lumR + cosVal * (1 - lumR) + sinVal * (-lumR),
                    lumG + cosVal * (-lumG) + sinVal * (-lumG),
                    lumB + cosVal * (-lumB) + sinVal * (1 - lumB), 0f, 0f,

                    lumR + cosVal * (-lumR) + sinVal * (0.143f),
                    lumG + cosVal * (1 - lumG) + sinVal * (0.140f),
                    lumB + cosVal * (-lumB) + sinVal * (-0.283f), 0f, 0f,

                    lumR + cosVal * (-lumR) + sinVal * (-(1 - lumR)),
                    lumG + cosVal * (-lumG) + sinVal * (lumG),
                    lumB + cosVal * (1 - lumB) + sinVal * (lumB), 0f, 0f,

                    0f, 0f, 0f, 1f, 0f
                )
            )
            matrix.postConcat(hueMatrix)
        }

        return matrix
    }

    fun getColorFilter(
        brightness: Float = 0f,
        contrast: Float = 1f,
        saturation: Float = 1f,
        hue: Float = 0f,
        warmth: Float = 0f,
        preset: FilterPreset = FilterPreset.NONE
    ): ColorFilter {
        return ColorMatrixColorFilter(
            createAdjustmentMatrix(brightness, contrast, saturation, hue, warmth, preset)
        )
    }

    fun toPorterDuff(blendMode: CanvasBlendMode): PorterDuff.Mode {
        return when (blendMode) {
            CanvasBlendMode.NORMAL -> PorterDuff.Mode.SRC_OVER
            CanvasBlendMode.MULTIPLY -> PorterDuff.Mode.MULTIPLY
            CanvasBlendMode.SCREEN -> PorterDuff.Mode.SCREEN
            CanvasBlendMode.OVERLAY -> PorterDuff.Mode.OVERLAY
            CanvasBlendMode.DARKEN -> PorterDuff.Mode.DARKEN
            CanvasBlendMode.LIGHTEN -> PorterDuff.Mode.LIGHTEN
            CanvasBlendMode.COLOR_DODGE -> PorterDuff.Mode.ADD
            CanvasBlendMode.COLOR_BURN -> PorterDuff.Mode.DARKEN
            CanvasBlendMode.DIFFERENCE -> PorterDuff.Mode.XOR
            CanvasBlendMode.SOFT_LIGHT -> PorterDuff.Mode.SRC_OVER
        }
    }

    /**
     * Renders all layers onto a full resolution bitmap matching canvas aspect ratio/dimensions
     */
    fun renderComposition(
        context: Context,
        layers: List<Layer>,
        canvasWidth: Int,
        canvasHeight: Int,
        backgroundColor: Long? = 0xFF141518
    ): Bitmap {
        val output = Bitmap.createBitmap(canvasWidth, canvasHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Draw background if set
        if (backgroundColor != null) {
            val bgPaint = Paint().apply {
                color = backgroundColor.toInt()
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, 0f, canvasWidth.toFloat(), canvasHeight.toFloat(), bgPaint)
        }

        // Render visible layers from bottom to top
        for (layer in layers) {
            if (!layer.isVisible) continue

            canvas.save()

            // Apply transforms centered on the layer position
            canvas.translate(layer.x, layer.y)
            canvas.rotate(layer.rotation)
            val scaleX = if (layer.flipH) -layer.scale else layer.scale
            val scaleY = if (layer.flipV) -layer.scale else layer.scale
            canvas.scale(scaleX, scaleY)

            val layerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                alpha = (layer.opacity.coerceIn(0f, 1f) * 255).toInt()
                xfermode = PorterDuffXfermode(toPorterDuff(layer.blendMode))
            }

            when (layer) {
                is ImageLayer -> {
                    renderImageLayer(context, layer, canvas, layerPaint)
                }
                is TextLayer -> {
                    renderTextLayer(layer, canvas, layerPaint)
                }
                is ShapeLayer -> {
                    renderShapeLayer(layer, canvas, layerPaint)
                }
                is DrawingLayer -> {
                    renderDrawingLayer(layer, canvas, layerPaint)
                }
                is StickerLayer -> {
                    renderStickerLayer(layer, canvas, layerPaint)
                }
            }

            canvas.restore()
        }

        return output
    }

    private fun renderImageLayer(
        context: Context,
        layer: ImageLayer,
        canvas: Canvas,
        paint: Paint
    ) {
        val bmp: Bitmap? = when {
            layer.bitmap != null -> layer.bitmap
            layer.resourceId != null -> BitmapFactory.decodeResource(context.resources, layer.resourceId)
            layer.uriString != null -> {
                try {
                    val uri = Uri.parse(layer.uriString)
                    context.contentResolver.openInputStream(uri)?.use {
                        BitmapFactory.decodeStream(it)
                    }
                } catch (e: Exception) {
                    null
                }
            }
            else -> null
        }

        bmp?.let { src ->
            val filter = getColorFilter(
                layer.brightness,
                layer.contrast,
                layer.saturation,
                layer.hue,
                layer.warmth,
                layer.filterPreset
            )
            paint.colorFilter = filter

            val halfW = layer.width / 2f
            val halfH = layer.height / 2f
            val dstRect = RectF(-halfW, -halfH, halfW, halfH)
            canvas.drawBitmap(src, null, dstRect, paint)
        }
    }

    private fun renderTextLayer(layer: TextLayer, canvas: Canvas, basePaint: Paint) {
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = layer.textColor.toInt()
            alpha = basePaint.alpha
            textSize = layer.fontSize * 1.5f
            textAlign = Paint.Align.CENTER
            xfermode = basePaint.xfermode

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

        val text = layer.text
        val textBounds = android.graphics.Rect()
        textPaint.getTextBounds(text, 0, text.length, textBounds)

        if (layer.hasBackground) {
            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.backgroundColor.toInt()
                alpha = ((layer.backgroundColor ushr 24) * (layer.opacity)).toInt()
                style = Paint.Style.FILL
            }
            val padX = 32f
            val padY = 20f
            val rect = RectF(
                -textBounds.width() / 2f - padX,
                -textBounds.height() / 2f - padY,
                textBounds.width() / 2f + padX,
                textBounds.height() / 2f + padY
            )
            canvas.drawRoundRect(rect, 16f, 16f, bgPaint)
        }

        // Vertical centering offset
        val yOffset = ((textBounds.height()) / 2f) - textBounds.bottom
        canvas.drawText(text, 0f, yOffset, textPaint)
    }

    private fun renderShapeLayer(layer: ShapeLayer, canvas: Canvas, basePaint: Paint) {
        val halfW = layer.width / 2f
        val halfH = layer.height / 2f
        val rect = RectF(-halfW, -halfH, halfW, halfH)

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = layer.fillColor.toInt()
            alpha = ((layer.fillColor ushr 24) * layer.opacity).toInt().coerceIn(0, 255)
            style = Paint.Style.FILL
            xfermode = basePaint.xfermode
        }

        val strokePaint = if (layer.strokeWidth > 0f) {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = layer.strokeColor.toInt()
                alpha = ((layer.strokeColor ushr 24) * layer.opacity).toInt().coerceIn(0, 255)
                style = Paint.Style.STROKE
                strokeWidth = layer.strokeWidth
                xfermode = basePaint.xfermode
            }
        } else null

        val path = createShapePath(layer.shapeType, rect, layer.cornerRadius)
        canvas.drawPath(path, fillPaint)
        strokePaint?.let { canvas.drawPath(path, it) }
    }

    private fun createShapePath(shapeType: ShapeType, rect: RectF, cornerRadius: Float): Path {
        val path = Path()
        when (shapeType) {
            ShapeType.RECTANGLE -> {
                path.addRect(rect, Path.Direction.CW)
            }
            ShapeType.ROUNDED_RECT -> {
                path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
            }
            ShapeType.CIRCLE -> {
                path.addOval(rect, Path.Direction.CW)
            }
            ShapeType.STAR -> {
                val cx = rect.centerX()
                val cy = rect.centerY()
                val rOuter = minOf(rect.width(), rect.height()) / 2f
                val rInner = rOuter * 0.45f
                val numPoints = 5
                val angleStep = Math.PI / numPoints
                var angle = -Math.PI / 2.0

                path.moveTo(
                    (cx + rOuter * cos(angle)).toFloat(),
                    (cy + rOuter * sin(angle)).toFloat()
                )
                for (i in 0 until numPoints * 2) {
                    val r = if (i % 2 == 0) rInner else rOuter
                    angle += angleStep
                    path.lineTo(
                        (cx + r * cos(angle)).toFloat(),
                        (cy + r * sin(angle)).toFloat()
                    )
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

    private fun renderDrawingLayer(layer: DrawingLayer, canvas: Canvas, basePaint: Paint) {
        for (dp in layer.paths) {
            if (dp.points.isEmpty()) continue

            val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = dp.color.toInt()
                alpha = ((layer.opacity * dp.alpha).coerceIn(0f, 1f) * 255).toInt()
                strokeWidth = dp.strokeWidth
                style = Paint.Style.STROKE
                strokeCap = Paint.Cap.ROUND
                strokeJoin = Paint.Join.ROUND
                if (dp.isEraser) {
                    xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
                } else {
                    xfermode = basePaint.xfermode
                }
            }

            val path = Path()
            path.moveTo(dp.points[0].x, dp.points[0].y)
            for (i in 1 until dp.points.size) {
                path.lineTo(dp.points[i].x, dp.points[i].y)
            }
            canvas.drawPath(path, strokePaint)
        }
    }

    private fun renderStickerLayer(layer: StickerLayer, canvas: Canvas, basePaint: Paint) {
        val halfW = layer.width / 2f
        val halfH = layer.height / 2f
        val rect = RectF(-halfW, -halfH, halfW, halfH)

        val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            xfermode = basePaint.xfermode
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textSize = 24f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.DEFAULT_BOLD
            xfermode = basePaint.xfermode
        }

        when (layer.stickerId) {
            "badge_verified" -> {
                badgePaint.color = 0xFF007DFE.toInt()
                canvas.drawRoundRect(rect, 30f, 30f, badgePaint)
                canvas.drawText("✓ VERIFIED", 0f, 8f, textPaint)
            }
            "badge_sale" -> {
                badgePaint.color = 0xFFFF3B30.toInt()
                canvas.drawRoundRect(rect, 16f, 16f, badgePaint)
                canvas.drawText("🔥 50% OFF", 0f, 8f, textPaint)
            }
            "badge_new" -> {
                badgePaint.color = 0xFF00E676.toInt()
                canvas.drawRoundRect(rect, 24f, 24f, badgePaint)
                textPaint.color = android.graphics.Color.BLACK
                canvas.drawText("★ NEW ARRIVAL", 0f, 8f, textPaint)
            }
            "badge_pro" -> {
                badgePaint.color = 0xFFFFB300.toInt()
                canvas.drawRoundRect(rect, 20f, 20f, badgePaint)
                textPaint.color = android.graphics.Color.BLACK
                canvas.drawText("PRO STUDIO", 0f, 8f, textPaint)
            }
            "badge_limited" -> {
                badgePaint.color = 0xFF8A58FF.toInt()
                canvas.drawRoundRect(rect, 24f, 24f, badgePaint)
                canvas.drawText("LIMITED EDITION", 0f, 8f, textPaint)
            }
            else -> {
                badgePaint.color = 0xFF00C7FF.toInt()
                canvas.drawRoundRect(rect, 20f, 20f, badgePaint)
                canvas.drawText(layer.title, 0f, 8f, textPaint)
            }
        }
    }

    /**
     * Saves rendered bitmap to Pictures/Photocraft directory in MediaStore
     */
    fun saveToGallery(context: Context, bitmap: Bitmap, title: String): Uri? {
        val filename = "${title}_${System.currentTimeMillis()}.png"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/Photocraft")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        }

        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)
                }
                return uri
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return null
    }

    /**
     * Saves bitmap to app cache and provides shareable FileProvider URI
     */
    fun createShareableUri(context: Context, bitmap: Bitmap, title: String): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "shared_designs")
            cachePath.mkdirs()
            val file = File(cachePath, "${title}_share.png")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
