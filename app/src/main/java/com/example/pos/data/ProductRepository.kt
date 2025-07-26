package com.example.pos.data

import com.example.pos.database.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun findProductByBarcode(barcode: String): Product?
    suspend fun insertProduct(product: Product)
}