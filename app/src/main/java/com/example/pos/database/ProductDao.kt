package com.example.pos.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * 商品(Product)テーブルを操作するためのDAO
 */
@Dao
interface ProductDao {

    /**
     * バーコードを元に商品を1件検索する。
     * @param barcode 検索するバーコード文字列
     * @return 見つかった商品。見つからない場合はnull
     */
    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun findByBarcode(barcode: String): Product?

    /**
     * すべての商品を商品名順で取得する。
     * Flowを使用することで、データが変更されるたびに自動で最新のリストが通知される。
     * @return 商品リストのFlow
     */
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    /**
     * 新しい商品を1件追加する。
     * もし同じバーコードの商品が既に存在する場合は、新しい情報で上書きする。
     * @param product 追加する商品オブジェクト
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: Product)

    /**
     * 商品の情報を更新する。
     * @param product 更新する商品オブジェクト
     */
    @Update
    suspend fun update(product: Product)

    /**
     * 商品を1件削除する。
     * @param product 削除する商品オブジェクト
     */
    @Delete
    suspend fun delete(product: Product)
}