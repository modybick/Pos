package com.example.pos.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

/**
 * 売上明細を表すエンティティ
 */
@Entity(
    tableName = "sale_details",
    // 主キーをsaleIdとproductBarcodeの組み合わせにする
    primaryKeys = ["saleId", "productBarcode"],
    // 外部キー制約。どの会計(Sale)に紐づくかを定義
    foreignKeys = [
        ForeignKey(
            entity = Sale::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE // 親の会計が削除されたら明細も削除
        )
    ],
    // 検索を高速化するためのインデックス
    indices = [Index(value = ["saleId"])]
)
data class SaleDetail(
    // どの会計IDに紐づくか
    var saleId: Long,

    // どの商品のバーコードか
    val productBarcode: String,

    // 商品名（履歴表示の際に便利）
    val productName: String,

    // 販売時の価格（価格改定に対応するため）
    val price: Int,

    // 数量
    val quantity: Int
)