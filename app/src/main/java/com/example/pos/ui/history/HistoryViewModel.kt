package com.example.pos.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.data.SaleRepository
import com.example.pos.database.Sale
import com.example.pos.database.SaleDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HistoryUiState(
    val sales: List<Sale> = emptyList(),
    val totalSalesAmount: Int = 0,
    val selectedSale: Sale? = null,
    val selectedSaleDetails: List<SaleDetail>? = null, // 選択された会計の詳細
    val isLoading: Boolean = true
)

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val saleRepository: SaleRepository
) : ViewModel() {

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
}