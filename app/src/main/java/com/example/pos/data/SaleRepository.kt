package com.example.pos.data

import com.example.pos.database.Sale
import com.example.pos.database.SaleDetail
import com.example.pos.database.SaleDao
import com.example.pos.database.SaleWithDetails
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// 売上データ操作の設計図
interface SaleRepository {
    fun getAllSalesStream(): Flow<List<Sale>>
    suspend fun getSaleDetails(saleId: Long): List<SaleDetail>
    suspend fun cancelSale(saleId: Long)
    suspend fun uncancelSale(saleId: Long)
    suspend fun getSalesWithDetails(): List<SaleWithDetails>
    suspend fun clearAllSales()
}

// 設計図の実装
class SaleRepositoryImpl @Inject constructor(
    private val saleDao: SaleDao
) : SaleRepository {
    override fun getAllSalesStream(): Flow<List<Sale>> = saleDao.getAllSales()
    override suspend fun getSaleDetails(saleId: Long): List<SaleDetail> = saleDao.getSaleDetailsForSale(saleId)
    override suspend fun cancelSale(saleId: Long) {
        saleDao.cancelSaleById(saleId)
    }
    override suspend fun uncancelSale(saleId: Long) {
        saleDao.uncancelSaleById(saleId)
    }
    override suspend fun getSalesWithDetails(): List<SaleWithDetails> {
        return saleDao.getSalesWithDetails()
    }
    override suspend fun clearAllSales() {
        saleDao.clearAllSales()
    }
}