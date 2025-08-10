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

    // 端末ID
    val terminalId: String,

    // 会計が行われた日時
    val createdAt: Date,

    // 決済方法
    val paymentMethod: String,

    // 合計金額
    val totalAmount: Int,

    // 預かり金額
    val tenderedAmount: Int,

    // お釣り
    val changeAmount: Int,

    // 取り消しフラグ
    val isCancelled: Boolean = false
)