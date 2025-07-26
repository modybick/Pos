package com.example.pos.ui.sale

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview as PV
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview as Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.res.painterResource
import com.example.pos.R
import androidx.compose.ui.graphics.Color

/**
 * メインのレジ画面
 * 上部にカメラプレビュー、下部にカート情報を表示する
 */
@Composable
fun SaleScreen(
    saleViewModel: SaleViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToCheckout: () -> Unit,
) {
    val uiState by saleViewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isTorchOn by remember { mutableStateOf(false) }

    var showClearConfirmDialog by remember { mutableStateOf(false) }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = {
                // ダイアログの外側をタップした時や、戻るボタンを押した時に閉じる
                showClearConfirmDialog = false
            },
            title = {
                Text("確認")
            },
            text = {
                Text("カートを空にしてもよろしいですか？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        saleViewModel.clearCart() // OKを押したらカートをクリア
                        showClearConfirmDialog = false // ダイアログを閉じる
                    }
                ) {
                    Text("はい")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false // キャンセルなのでダイアログを閉じるだけ
                    }
                ) {
                    Text("いいえ")
                }
            }
        )
    }

    // カメラ権限の状態を管理
    var hasCamPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    // 権限をリクエストするためのランチャー
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCamPermission = granted
        }
    )
    // Composableが最初に表示されたときに権限を確認・リクエスト
    LaunchedEffect(key1 = true) {
        if (!hasCamPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .safeDrawingPadding()) {
        // --- 上部：カメラプレビュー領域 ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f) // 画面上部40%
        ) {
            if (hasCamPermission) {
                CameraPreview(
                    isTorchOn = isTorchOn,
                    onBarcodeScanned = saleViewModel::onBarcodeScanned
                )
                IconButton(
                    onClick = { isTorchOn = !isTorchOn },
                    modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.White // 背景を白に
                    )
                ) {
                    Icon(
                        painter = painterResource(
                            id = if (isTorchOn) R.drawable.flashlight_on else R.drawable.flashlight_off
                        ),
                        contentDescription = "ライト切り替え",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
            } else {
                // 権限がない場合にメッセージを表示
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("カメラの権限が必要です")
                }
            }
        }

        // --- 下部：カート情報と操作ボタン ---
        Card(
            modifier = Modifier
                .weight(0.6f)
                .fillMaxWidth(),
            // ...
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxHeight()) {
                CartList(
                    items = uiState.cartItems,
                    onQuantityChanged = saleViewModel::onQuantityChanged,
                    modifier = Modifier.weight(1f)
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                TotalAmountDisplay(totalAmount = uiState.totalAmount)
                Spacer(modifier = Modifier.height(16.dp))

                // ▼▼▼ ここからボタンのレイアウトを変更 ▼▼▼

                // メインの「会計する」ボタンを一番上に配置
                Button(
                    onClick = onNavigateToCheckout,
                    enabled = uiState.cartItems.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("会計する")
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 「クリア」ボタンと「売上履歴」ボタンを横並びに配置
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // クリアボタン（旧商品管理ボタンの位置）
                    OutlinedButton(
                        onClick = { showClearConfirmDialog = true },
                        enabled = uiState.cartItems.isNotEmpty(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("クリア")
                    }

                    // 売上履歴ボタン
                    OutlinedButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("売上履歴")
                    }
                }
            }
        }
    }
}

/**
 * CameraXのプレビューと画像解析を表示するComposable
 */
@Composable
private fun CameraPreview(
    isTorchOn: Boolean,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val camera = remember { mutableStateOf<androidx.camera.core.Camera?>(null) }

    // isTorchOnの状態が変化したら、ライトを制御する
    LaunchedEffect(isTorchOn) {
        camera.value?.cameraControl?.enableTorch(isTorchOn)
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()

            val preview = PV.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer(onBarcodeScanned))
                }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll() // 既存のバインドを解除
                camera.value = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                // ライフサイクルバインド時のエラー
            }
            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}

/**
 * カート内の商品リストを表示するComposable
 */
@Composable
private fun CartList(
    items: List<CartItem>,
    onQuantityChanged: (String, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (items.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("商品をスキャンしてください")
        }
    } else {
        LazyColumn(modifier = modifier) {
            items(items, key = { it.product.barcode }) { item ->
                CartItemRow(item = item, onQuantityChanged = onQuantityChanged)
                HorizontalDivider()
            }
        }
    }
}

/**
 * カート内の商品1行分を表示するComposable
 */
@Composable
private fun CartItemRow(
    item: CartItem,
    onQuantityChanged: (String, Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(item.product.name, fontWeight = FontWeight.Bold)
            Text("単価: ${item.product.price}円")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 数量が1の場合は削除アイコン、それ以外はマイナスアイコン
            IconButton(onClick = { onQuantityChanged(item.product.barcode, -1) }) {
                Icon(
                    imageVector = if (item.quantity == 1) Icons.Default.Delete else Icons.Default.KeyboardArrowDown,
                    contentDescription = "数量を減らす/削除"
                )
            }
            Text(item.quantity.toString(), modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(onClick = { onQuantityChanged(item.product.barcode, 1) }) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "数量を増やす")
            }
        }
    }
}

/**
 * 合計金額を表示するComposable
 */
@Composable
private fun TotalAmountDisplay(totalAmount: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("合計", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text("${totalAmount}円", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}


/**
 * Android Studioでのプレビュー用
 */
@Preview(showBackground = true)
@Composable
fun SaleScreenPreview() {
    MaterialTheme {
        // プレビューはViewModelを直接インスタンス化できないため、表示が難しい
        // 代わりに、UIの骨格だけを表示するなどの工夫が必要
        // SaleScreen(saleViewModel = SaleViewModel()) // HiltViewModelはプレビュー不可
    }
}