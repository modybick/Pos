package com.example.pos.ui.history

import android.app.Application
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.pos.data.SaleRepository
import com.example.pos.database.Sale
import com.example.pos.database.SaleDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.icu.text.SimpleDateFormat
import android.net.Uri
import android.widget.Toast
import androidx.core.content.edit
import java.io.IOException
import java.util.Locale
import com.example.pos.data.ProductRepository
import androidx.lifecycle.AndroidViewModel
import com.google.gson.Gson

data class HistoryUiState(
    val sales: List<Sale> = emptyList(),
    val totalSalesAmount: Int = 0,
    val selectedSale: Sale? = null,
    val selectedSaleDetails: List<SaleDetail>? = null, // 選択された会計の詳細
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val saleRepository: SaleRepository,
    private val productRepository: ProductRepository,
    private val application: Application
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        // ViewModel起動時に、DBから全売上履歴を取得し続ける
        viewModelScope.launch {
            saleRepository.getAllSalesStream().collect { sales ->
                val total = sales.filter { !it.isCancelled }.sumOf { it.totalAmount }
                _uiState.update { it.copy(sales = sales, totalSalesAmount = total, isLoading = false) }
            }
        }
    }

    // ユーザーがリストの項目をタップしたときに呼ばれる
    fun onSaleSelected(sale: Sale) {
        viewModelScope.launch {
            val details = saleRepository.getSaleDetails(sale.id)
            _uiState.update { it.copy(selectedSale = sale, selectedSaleDetails = details) }
        }
    }

    // ボトムシートが閉じたときに呼ばれる
    fun onDismissSaleDetails() {
        _uiState.update { it.copy(selectedSale = null, selectedSaleDetails = null) }
    }

    fun cancelSelectedSale() {
        // 現在選択されている会計の詳細情報からIDを取得
        _uiState.value.selectedSaleDetails?.firstOrNull()?.let { detail ->
            viewModelScope.launch {
                saleRepository.cancelSale(detail.saleId)
                // 詳細表示を閉じる
                onDismissSaleDetails()
            }
        }
    }

    fun uncancelSelectedSale() {
        _uiState.value.selectedSale?.id?.let { saleId ->
            viewModelScope.launch {
                saleRepository.uncancelSale(saleId)
                onDismissSaleDetails()
            }
        }
    }
    fun exportSalesToCsv(uri: Uri) {
        viewModelScope.launch {
            try {
                // 1. 売上と明細の全データを取得
                val salesWithDetails = saleRepository.getSalesWithDetails()
                if (salesWithDetails.isEmpty()) {
                    Toast.makeText(application, "エクスポートするデータがありません", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 2. 明細に含まれる全商品のバーコードを重複なく取得
                val productBarcodes = salesWithDetails
                    .flatMap { it.details }
                    .map { it.productBarcode }
                    .distinct()

                // 3. バーコードを元に商品マスタ情報をまとめて取得し、マップに変換
                val productsMap = productRepository.findProductsByBarcodes(productBarcodes)
                    .associateBy { it.barcode }

                // 4. CSV形式の文字列を作成
                val csvContent = buildCsvContent(salesWithDetails, productsMap)

                // 5. ファイルに書き込む
                application.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(csvContent.toByteArray())
                }

                // 成功メッセージを表示
                Toast.makeText(application, "CSVファイルが保存されました", Toast.LENGTH_LONG).show()

            } catch (e: IOException) {
                // エラーメッセージを表示
                Toast.makeText(application, "エクスポートに失敗しました", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun buildCsvContent(
        sales: List<com.example.pos.database.SaleWithDetails>,
        productsMap: Map<String, com.example.pos.database.Product>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.JAPAN)
        val stringBuilder = StringBuilder()
        // ヘッダー行
        stringBuilder.append("端末ID,会計ID,会計日時,決済方法,合計金額,預かり金額,お釣り,取り消し,商品バーコード,商品名,tag,販売単価,数量\n")

        // データ行
        sales.forEach { saleWithDetails ->
            saleWithDetails.details.forEach { detail ->
                val tag = productsMap[detail.productBarcode]?.tag ?: ""
                stringBuilder.append(
                    "${saleWithDetails.sale.terminalId}," +
                            "${saleWithDetails.sale.id}," +
                            "${dateFormat.format(saleWithDetails.sale.createdAt)}," +
                            "${saleWithDetails.sale.paymentMethod}," +
                            "${saleWithDetails.sale.totalAmount}," +
                            "${saleWithDetails.sale.tenderedAmount}," +
                            "${saleWithDetails.sale.changeAmount}," +
                            "${saleWithDetails.sale.isCancelled}," +
                            "${detail.productBarcode}," +
                            "\"${detail.productName}\"," + // 商品名にカンマが含まれる可能性を考慮
                            "\"$tag\"," +
                            "${detail.price}," +
                            "${detail.quantity}," + "\n"
                )
            }
        }
        return stringBuilder.toString()
    }

    // 👇 カート再現リクエストを保存するメソッド
    fun requestCartReproduction() {
        _uiState.value.selectedSaleDetails?.let { details ->
            if (details.isNotEmpty()) {
                val prefs = application.getSharedPreferences("pos_prefs", Context.MODE_PRIVATE)
                val gson = Gson()
                // 明細リストをJSON文字列に変換して保存
                val detailsJson = gson.toJson(details)
                prefs.edit {
                    putString("reproduce_cart_details", detailsJson)
                }
                onDismissSaleDetails()
            }
        }
    }
}
