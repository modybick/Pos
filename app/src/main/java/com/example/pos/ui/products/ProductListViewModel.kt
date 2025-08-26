package com.example.pos.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.data.ProductRepository
import com.example.pos.database.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class ProductListUiState(
    val categories: List<String> = emptyList(), // タブに表示するタグのリスト
    val productsByCategory: Map<String, List<Product>> = emptyMap(), // タグでグループ化された商品
    val isLoading: Boolean = true
)

@HiltViewModel
class ProductListViewModel @Inject constructor(
    productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductListUiState())
    val uiState: StateFlow<ProductListUiState> = _uiState.asStateFlow()

    init {
        productRepository.getAllProducts()
            .onEach { products ->
                // タグがnullまたは空のものを「その他」に分類
                val productsByCategory =
                    products.groupBy { it.category?.takeIf { it.isNotBlank() } ?: "その他" }

                // タグのリストを作成し、先頭に「すべて」を追加
                val categories = listOf("すべて") + productsByCategory.keys.sorted()

                // 「すべて」タブ用に全商品のマッピングを追加
                val finalProductsMap = mapOf("すべて" to products) + productsByCategory

                _uiState.update {
                    it.copy(
                        categories = categories,
                        productsByCategory = finalProductsMap,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}