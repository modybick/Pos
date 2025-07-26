package com.example.pos.ui.sale

import com.example.pos.database.Product


// カート内の商品を表現するデータクラス
data class CartItem(
    val product: Product,
    val quantity: Int
)

// レジ画面全体のUI状態
data class SaleUiState(
    val cartItems: List<CartItem> = emptyList(),
    val totalAmount: Int = 0
)