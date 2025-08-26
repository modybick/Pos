package com.example.pos.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 商品テーブルを表すエンティティ
 *
 * @param barcode 商品のバーコード。このテーブルの主キー。
 * @param name 商品名。
 * @param price 商品の価格（円）。
 */
@Entity(tableName = "products")
data class Product(
    @PrimaryKey
    val barcode: String,
    val name: String,
    val price: Int,
    val category: String?
)