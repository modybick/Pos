package com.example.pos.ui.sale

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class BarcodeAnalyzer(
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

        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

        scanner.process(image)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { barcode ->
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