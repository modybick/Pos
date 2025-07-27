package com.example.pos.ui.sale

import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlin.math.roundToInt

class BarcodeAnalyzer(
    private val viewWidth: Int,
    private val viewHeight: Int,
    private val onBarcodeDetected: (barcode: String) -> Unit,
) : ImageAnalysis.Analyzer {

    companion object {
        const val COOLDOWN_MILLIS = 2000L // 2秒のクールダウン
    }

    private val scanner = BarcodeScanning.getClient()
    private var lastScanTime = 0L

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()
        // 前回のスキャンからクールダウン時間が経過していない場合は処理を中断
        if (currentTime - lastScanTime < COOLDOWN_MILLIS) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image ?: return

        // 解析する画像のサイズと画面のプレビューサイズから、有効なスキャン領域を計算
        val rotationDegrees = imageProxy.imageInfo.rotationDegrees
        val imageWidth = if (rotationDegrees == 90 || rotationDegrees == 270) imageProxy.height else imageProxy.width
        val imageHeight = if (rotationDegrees == 90 || rotationDegrees == 270) imageProxy.width else imageProxy.height

        val viewAspectRatio = viewWidth.toFloat() / viewHeight
        val imageAspectRatio = imageWidth.toFloat() / imageHeight

        val scanBounds: Rect
        if (viewAspectRatio > imageAspectRatio) {
            // プレビューの方が横長の場合（画像の上と下がクロップされる）
            val newHeight = (imageWidth / viewAspectRatio).roundToInt()
            val yOffset = (imageHeight - newHeight) / 2
            scanBounds = Rect(0, yOffset, imageWidth, yOffset + newHeight)
        } else {
            // プレビューの方が縦長の場合（画像の左右がクロップされる）
            val newWidth = (imageHeight * viewAspectRatio).roundToInt()
            val xOffset = (imageWidth - newWidth) / 2
            scanBounds = Rect(xOffset, 0, xOffset + newWidth, imageHeight)
        }

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull { barcode ->
                    // バーコードがスキャン領域に収まっているか
                    barcode.boundingBox?.let { scanBounds.contains(it) } ?: false
                }?.rawValue?.let { barcode ->
                    // バーコードを検出したら、最終スキャン時刻を更新
                    lastScanTime = currentTime
                    onBarcodeDetected(barcode)
                }
            }
            .addOnFailureListener {
                // エラー処理
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}