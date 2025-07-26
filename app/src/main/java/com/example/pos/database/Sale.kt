package com.example.pos.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * 1回の会計情報を表すエンティティ
 */
@Entity(tableName = "sales")
data class Sale(
    // 主キー。自動で番号を割り振る
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,

    // 会計が行われた日時
    val createdAt: Date,

    // 合計金額
    val totalAmount: Int,

    // 取り消しフラグ
    val isCancelled: Boolean = false
)