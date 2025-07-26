package com.example.pos.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * 売上(Sale)と売上明細(SaleDetail)テーブルを操作するためのDAO
 */
@Dao
interface SaleDao {

    // --- データ登録用のメソッド ---

    // @Insertアノテーションはsuspend関数である必要があるため、
    // トランザクション内で呼び出すために個別のsuspend関数として定義
    @Insert
    suspend fun insertSale(sale: Sale): Long // 登録したSaleのIDを返す

    @Insert
    suspend fun insertSaleDetails(details: List<SaleDetail>)

    /**
     * 1回の会計情報（売上と明細）をまとめて登録する。
     * @Transactionにより、この処理全体が成功するか失敗するかのどちらかになり、
     * データの不整合を防ぐ。
     * @param sale 登録する売上
     * @param details 登録する売上明細のリスト
     */
    @Transaction
    suspend fun insertSaleAndDetails(sale: Sale, details: List<SaleDetail>) {
        // 1. まず売上情報を登録し、そのIDを取得
        val saleId = insertSale(sale)

        // 2. 各明細に取得した会計IDを設定
        details.forEach { it.saleId = saleId }

        // 3. 明細情報をまとめて登録
        insertSaleDetails(details)
    }


    // --- データ取得用のメソッド ---

    /**
     * すべての売上を日付の新しい順で取得する。
     * @return 売上リストのFlow
     */
    @Query("SELECT * FROM sales ORDER BY createdAt DESC")
    fun getAllSales(): Flow<List<Sale>>

    /**
     * 指定した期間の売上を取得する。
     * @param startDate 開始日時
     * @param endDate 終了日時
     * @return 指定期間の売上リストのFlow
     */
    @Query("SELECT * FROM sales WHERE createdAt BETWEEN :startDate AND :endDate ORDER BY createdAt DESC")
    fun getSalesBetweenDates(startDate: Date, endDate: Date): Flow<List<Sale>>

    /**
     * 特定の会計IDに紐づくすべての明細を取得する。
     * @param saleId 会計ID
     * @return 売上明細のリスト
     */
    @Query("SELECT * FROM sale_details WHERE saleId = :saleId")
    suspend fun getSaleDetailsForSale(saleId: Long): List<SaleDetail>

    @Query("UPDATE sales SET isCancelled = 1 WHERE id = :saleId")
    suspend fun cancelSaleById(saleId: Long)

    @Query("UPDATE sales SET isCancelled = 0 WHERE id = :saleId")
    suspend fun uncancelSaleById(saleId: Long)
}