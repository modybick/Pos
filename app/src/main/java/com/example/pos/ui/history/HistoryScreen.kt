package com.example.pos.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.text.style.TextDecoration

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyViewModel: HistoryViewModel = hiltViewModel()
) {
    val uiState by historyViewModel.uiState.collectAsState()
    val sheetState = rememberModalBottomSheetState()

    var showCancelConfirmDialog by remember { mutableStateOf(false) }
    var showUncancelConfirmDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("売上履歴") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // --- 売上履歴リスト ---
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.sales.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("売上履歴はありません")
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)){
                    items(uiState.sales, key = { it.id }) { sale ->
                        SaleHistoryRow(
                            sale = sale,
                            onClick = { historyViewModel.onSaleSelected(sale) }
                        )
                        Divider()
                    }
                }
                // 👇 合計金額表示をリストの下に追加
                Divider()
                TotalSalesRow(totalAmount = uiState.totalSalesAmount)
            }
        }
    }

    // --- 詳細表示ボトムシート ---
    if (uiState.selectedSaleDetails != null) {
        ModalBottomSheet(
            onDismissRequest = { historyViewModel.onDismissSaleDetails() },
            sheetState = sheetState
        ) {
            SaleDetailSheetContent(
                details = uiState.selectedSaleDetails!!,
                isCancelled = uiState.selectedSale!!.isCancelled,
                onCancelClick = { showCancelConfirmDialog = true },
                onUncancelClick = { showUncancelConfirmDialog = true }
            )
        }
    }
    if (showCancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCancelConfirmDialog = false },
            title = { Text("確認") },
            text = { Text("この売上を取り消しますか？\nこの操作は元に戻せません。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        historyViewModel.cancelSelectedSale()
                        showCancelConfirmDialog = false
                    }
                ) {
                    Text("はい、取り消します")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelConfirmDialog = false }) {
                    Text("いいえ")
                }
            }
        )
    }
    if (showUncancelConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showUncancelConfirmDialog = false },
            title = { Text("確認") },
            text = { Text("この売上の取り消しをキャンセルしますか？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        historyViewModel.uncancelSelectedSale()
                        showUncancelConfirmDialog = false
                    }
                ) {
                    Text("はい")
                }
            },
            dismissButton = {
                TextButton(onClick = { showUncancelConfirmDialog = false }) {
                    Text("いいえ")
                }
            }
        )
    }
}

@Composable
private fun SaleHistoryRow(sale: com.example.pos.database.Sale, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.JAPAN) }
    val textDecoration = if (sale.isCancelled) TextDecoration.LineThrough else null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(dateFormat.format(sale.createdAt), modifier = Modifier.weight(1f), textDecoration = textDecoration)
        Text("${sale.totalAmount} 円", textDecoration = textDecoration)
    }
}

@Composable
private fun SaleDetailSheetContent(
    details: List<com.example.pos.database.SaleDetail>,
    isCancelled: Boolean,
    onCancelClick: () -> Unit,
    onUncancelClick: () -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("会計詳細", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        // --- ヘッダー行を追加 ---
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "商品名",
                modifier = Modifier.weight(0.5f),
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "数量",
                modifier = Modifier.weight(0.2f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "金額",
                modifier = Modifier.weight(0.3f),
                textAlign = TextAlign.End,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Divider()

        // --- 商品リスト ---
        LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
            items(details) { detail ->
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 商品名
                    Text(
                        text = detail.productName,
                        modifier = Modifier.weight(0.5f)
                    )
                    // 数量
                    Text(
                        text = detail.quantity.toString(),
                        modifier = Modifier.weight(0.2f),
                        textAlign = TextAlign.Center
                    )
                    // 金額
                    Text(
                        text = "${detail.price * detail.quantity}円",
                        modifier = Modifier.weight(0.3f),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        if (isCancelled) {
            // 取り消し済みの売上の場合
            Button(
                onClick = onUncancelClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("取り消しをキャンセル")
            }
        } else {
            // 通常の売上の場合
            Button(
                onClick = onCancelClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("この売上を取り消す")
            }
        }
    }
}

@Composable
private fun TotalSalesRow(totalAmount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("合計売上", style = MaterialTheme.typography.titleMedium)
        Text(
            "$totalAmount 円",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}