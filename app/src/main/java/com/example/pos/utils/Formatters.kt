package com.example.pos.utils

import java.text.NumberFormat
import java.util.*

// カンマ区切りのフォーマッターを準備
private val numberFormatter = NumberFormat.getNumberInstance(Locale.JAPAN)

/**
 * Intをカンマ区切りの文字列に変換する拡張関数
 * 例: 1000 -> "1,000"
 */
fun Int.toCurrencyFormat(): String {
    return numberFormatter.format(this)
}