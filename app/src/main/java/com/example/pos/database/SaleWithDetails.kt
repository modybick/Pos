package com.example.pos.database

import androidx.room.Embedded
import androidx.room.Relation

// Saleと、それに紐づくSaleDetailのリストを保持するクラス
data class SaleWithDetails(
    @Embedded val sale: Sale,
    @Relation(
        parentColumn = "id",
        entityColumn = "saleId"
    )
    val details: List<SaleDetail>
)