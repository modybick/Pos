package com.example.pos.ui.sale

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    saleViewModel: SaleViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by saleViewModel.uiState.collectAsState()
    var tenderedAmount by remember { mutableStateOf("") } // 預かり金額の入力

    val tendered = tenderedAmount.toIntOrNull() ?: 0
    val change = tendered - uiState.totalAmount // お釣り

    val totalItemCount = uiState.cartItems.sumOf { it.quantity } // 商品点数を計算

    Scaffold(
        topBar = { TopAppBar(title = { Text("会計") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // --- 商品リスト（確認用） ---
            Text("会計内容", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            // --- ヘッダー行 ---
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "商品名",
                    modifier = Modifier.weight(0.5f), // 比率 50%
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "数量",
                    modifier = Modifier.weight(0.2f), // 比率 20%
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "金額",
                    modifier = Modifier.weight(0.3f), // 比率 30%
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            HorizontalDivider()
            // --- 商品リスト（確認用） ---
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.cartItems) { item ->
                    // 👇 各データ行
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 商品名
                        Text(
                            text = item.product.name,
                            modifier = Modifier.weight(0.5f) // ヘッダーと同じ比率
                        )
                        // 数量
                        Text(
                            text = item.quantity.toString(),
                            modifier = Modifier.weight(0.2f),
                            textAlign = TextAlign.Center
                        )
                        // 金額
                        Text(
                            text = "${item.product.price * item.quantity}円",
                            modifier = Modifier.weight(0.3f),
                            textAlign = TextAlign.End
                        )
                    }
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            // --- 金額表示 ---
            AmountRow(label = "合計金額（${totalItemCount}点）", amount = uiState.totalAmount)
            Spacer(modifier = Modifier.height(8.dp))

            // --- 預かり金額入力 ---
            OutlinedTextField(
                value = tenderedAmount,
                onValueChange = { tenderedAmount = it.filter { char -> char.isDigit() } },
                label = { Text("預かり金額") },
                suffix = { Text("円") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 28.sp,
                    textAlign = TextAlign.End
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            // --- お釣り表示 ---
            AmountRow(label = "お釣り", amount = if (change > 0) change else 0)

            Spacer(modifier = Modifier.height(24.dp))

            // --- 確定ボタン ---
            Button(
                onClick = {
                    saleViewModel.finalizeSale()
                    onNavigateBack() // レジ画面に戻る
                },
                // 預かり金額が合計以上の場合のみ有効
                enabled = tendered >= uiState.totalAmount,
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Text("会計を確定")
            }
        }
    }
}

@Composable
private fun AmountRow(label: String, amount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(
            "$amount 円",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.primary
        )
    }
}