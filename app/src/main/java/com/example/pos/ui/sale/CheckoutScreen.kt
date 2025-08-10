package com.example.pos.ui.sale

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pos.utils.NumberCommaTransformation
import com.example.pos.utils.toCurrencyFormat
import androidx.compose.runtime.remember

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

    // カートのアイテムを並び替え
    val sortedCartItems = remember(uiState.cartItems) {
        uiState.cartItems.sortedBy { it.product.barcode }
    }

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
                items(sortedCartItems) { item ->
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
                            text = "${(item.product.price * item.quantity).toCurrencyFormat()}円",
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
            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                // 同額ボタン
                Button(
                    onClick = { tenderedAmount = uiState.totalAmount.toString() },
                    modifier = Modifier.height(48.dp)
                ) {
                    Text("同額")
                }
                Spacer(modifier = Modifier.width(8.dp))
                // 入力欄
                OutlinedTextField(
                    value = tenderedAmount,
                    onValueChange = { tenderedAmount = it.filter { char -> char.isDigit() } },
                    label = { Text("預かり金額") },
                    visualTransformation = NumberCommaTransformation(),
                    suffix = {},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 28.sp,
                        textAlign = TextAlign.End
                    ),
                    leadingIcon = {
                        if (tenderedAmount.isNotEmpty()) {
                            IconButton(onClick = { tenderedAmount = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "入力クリア"
                                )
                            }
                        }
                    }
                )
                // 単位
                Text(
                    text = " 円",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            // --- お釣り表示 ---
            AmountRow(label = "お釣り", amount = if (change > 0) change else 0)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.Bottom,
                modifier = Modifier.fillMaxWidth()
            ) {
                // --- PayPay ---
                Button(
                    onClick = {
                        saleViewModel.finalizeSale(tendered, "PayPay")
                        onNavigateBack() // レジ画面に戻る
                    },
                    // 預かり金額が合計以上の場合のみ有効
                    enabled = tendered >= uiState.totalAmount,
                    modifier = Modifier.weight(1f).height(48.dp),

                ) {
                    Text("PayPay")
                }
                Spacer(modifier = Modifier.width(16.dp))
                // --- 現金 ---
                Button(
                    onClick = {
                        saleViewModel.finalizeSale(tendered, "現金")
                        onNavigateBack() // レジ画面に戻る
                    },
                    // 預かり金額が合計以上の場合のみ有効
                    enabled = tendered >= uiState.totalAmount,
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text("現金")
                }
            }
        }
    }
}

@Composable
private fun AmountRow(label: String, amount: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
        Text(
            "${amount.toCurrencyFormat()} 円",
            style = MaterialTheme.typography.headlineLarge,
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.primary
        )
    }
}