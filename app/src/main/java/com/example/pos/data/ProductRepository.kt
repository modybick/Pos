package com.example.pos.data

import com.example.pos.database.Product
import com.example.pos.database.ProductDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun findProductByBarcode(barcode: String): Product?
    suspend fun insertProduct(product: Product)
    suspend fun findProductsByBarcodes(barcodes: List<String>): List<Product>
    suspend fun bulkInsertProducts(products: List<Product>)

    suspend fun clearAllProducts()
}

// 設計図の具体的な実装。DAOを使ってDBを操作する
class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
    }

    override suspend fun findProductByBarcode(barcode: String): Product? {
        return productDao.findByBarcode(barcode)
    }

    override suspend fun insertProduct(product: Product) {
        productDao.insert(product)
    }

    override suspend fun findProductsByBarcodes(barcodes: List<String>): List<Product> {
        return productDao.findByBarcodes(barcodes)
    }

    override suspend fun bulkInsertProducts(products: List<Product>) {
        productDao.bulkInsertProducts(products)
    }

    override suspend fun clearAllProducts() {
        productDao.clearAllProducts()
    }

}