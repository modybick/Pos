@file:Suppress("DEPRECATION")

package com.example.pos.ui.sale

import android.Manifest
import android.content.pm.PackageManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.pos.R
import com.example.pos.ui.products.ProductListScreen
import com.example.pos.utils.toCurrencyFormat
import java.util.concurrent.Executors
import androidx.camera.core.Preview as PV

/**
 * メインのレジ画面
 * 上部にカメラプレビュー、下部にカート情報を表示する
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaleScreen(
    saleViewModel: SaleViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToCheckout: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val uiState by saleViewModel.uiState.collectAsState()
    val context = LocalContext.current

    val lazyListState = rememberLazyListState() // 👈 LazyColumnの状態を管理

    var isTorchOn by remember { mutableStateOf(false) }

    var isVibrationOn by remember { mutableStateOf(true) }
    LaunchedEffect(isVibrationOn) {
        saleViewModel.setVibrationEnabled(isVibrationOn)
    }

    // 👇 ボトムシートの表示状態を管理
    val sheetState = rememberModalBottomSheetState()
    var showProductSheet by remember { mutableStateOf(false) }

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

    LaunchedEffect(Unit) {
        // スキャンイベントを監視
        saleViewModel.scrollToBarcode.collect { barcode ->
            // データが更新されるのを少し待ってからインデックスを探す
            // こうしないと、古いリストからインデックスを探してしまうことがある
            kotlinx.coroutines.delay(100)

            val index = uiState.cartItems.indexOfFirst { it.product.barcode == barcode }
            // 👇 indexが-1でない（商品が見つかった）場合のみスクロール
            if (index != -1) {
                lazyListState.animateScrollToItem(index = index)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
    ) {
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
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // バイブレーションON/OFFボタン
                    IconButton(
                        onClick = {
                            isVibrationOn = !isVibrationOn
                            // バイブレーションをONにしたとき、触覚フィードバックの値をチェック
                            if (isVibrationOn) {
                                val hapticFeedbackEnabled = Settings.System.getInt(
                                    context.contentResolver,
                                    Settings.System.HAPTIC_FEEDBACK_ENABLED,
                                    0
                                ) != 0

                                if (!hapticFeedbackEnabled) {
                                    Toast.makeText(
                                        context,
                                        "端末の触覚フィードバックがOFFになっています。",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isVibrationOn) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isVibrationOn) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.mobile_vibrate
                            ),
                            contentDescription = "バイブレーション切り替え",
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    // ライトのON/OFFボタン
                    IconButton(
                        onClick = { isTorchOn = !isTorchOn },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = if (isTorchOn) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isTorchOn) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            painter = painterResource(
                                id = R.drawable.flashlight_on
                            ),
                            contentDescription = "ライト切り替え",
                            modifier = Modifier.size(36.dp)
                        )
                    }
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
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxHeight()
            ) {
                CartList(
                    items = uiState.cartItems,
                    onQuantityChanged = saleViewModel::onQuantityChanged,
                    modifier = Modifier.weight(1f),
                    lazyListState = lazyListState
                )
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                TotalAmountDisplay(totalAmount = uiState.totalAmount)
                Spacer(modifier = Modifier.height(16.dp))

                // ボタン１行目
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onNavigateToCheckout,
                        enabled = uiState.cartItems.isNotEmpty(),
                        modifier = Modifier
                            .weight(7f)
                            .height(48.dp)
                    ) {
                        Text("会計する")
                    }
                    // クリアボタン（旧商品管理ボタンの位置）
                    OutlinedButton(
                        onClick = { showClearConfirmDialog = true },
                        enabled = uiState.cartItems.isNotEmpty(),
                        modifier = Modifier
                            .weight(3f)
                            .height(48.dp)
                    ) {
                        Text("クリア")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // ボタン２行目
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = { showProductSheet = true }, // 👈 ボトムシートを表示
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("商品一覧")
                    }

                    // 売上履歴ボタン
                    OutlinedButton(
                        onClick = onNavigateToHistory,
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("売上履歴")
                    }

                    // 設定ボタン
                    OutlinedButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("設定")
                    }
                }
            }
        }
    }
    // 👇 商品一覧ボトムシートの定義
    if (showProductSheet) {
        ModalBottomSheet(
            onDismissRequest = { showProductSheet = false },
            sheetState = sheetState
        ) {
            // ボトムシートの中身としてProductListScreenを呼び出す
            ProductListScreen(
                onProductSelected = { barcode ->
                    saleViewModel.onBarcodeScanned(barcode)
                    // 連続追加できるよう、ここではシートを閉じない
                }
            )
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

    var previewSize by remember { mutableStateOf(Pair(0, 0)) }

    // isTorchOnの状態が変化したら、ライトを制御する
    LaunchedEffect(isTorchOn) {
        camera.value?.cameraControl?.enableTorch(isTorchOn)
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                // 👇 プレビューのサイズが確定したらStateを更新
                addOnLayoutChangeListener { _, left, top, right, bottom, _, _, _, _ ->
                    previewSize = Pair(right - left, bottom - top)
                }
            }
        },
        // 👇 プレビューサイズが取得できたら、カメラをバインドする
        update = { previewView ->
            if (previewSize.first > 0 && previewSize.second > 0) {
                val cameraProvider = cameraProviderFuture.get()
                val preview = PV.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                    .also {
                        // 👇 Analyzerにプレビューサイズを渡す
                        it.setAnalyzer(
                            cameraExecutor,
                            BarcodeAnalyzer(
                                viewWidth = previewSize.first,
                                viewHeight = previewSize.second,
                                onBarcodeDetected = onBarcodeScanned
                            )
                        )
                    }

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()
                    camera.value = cameraProvider.bindToLifecycle(
                        lifecycleOwner, cameraSelector, preview, imageAnalysis
                    )
                } catch (e: Exception) { /* ... */
                }
            }
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
    modifier: Modifier = Modifier,
    lazyListState: androidx.compose.foundation.lazy.LazyListState
) {
    if (items.isEmpty()) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("商品をスキャンしてください")
        }
    } else {
        LazyColumn(
            modifier = modifier,
            state = lazyListState
        ) {
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
            Text("単価: ${item.product.price.toCurrencyFormat()}円")
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
        Text(
            "${totalAmount.toCurrencyFormat()}円",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
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