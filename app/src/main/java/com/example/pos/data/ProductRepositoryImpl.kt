package com.example.pos.data

import com.example.pos.database.Product
import com.example.pos.database.ProductDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

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
}