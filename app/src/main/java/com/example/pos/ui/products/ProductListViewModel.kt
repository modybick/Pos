package com.example.pos.ui.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.data.ProductRepository
import com.example.pos.database.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class ProductListUiState(
    val tags: List<String> = emptyList(), // タブに表示するタグのリスト
    val productsByTag: Map<String, List<Product>> = emptyMap(), // タグでグループ化された商品
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
                val productsByTag = products.groupBy { it.tag?.takeIf { it.isNotBlank() } ?: "その他" }

                // タグのリストを作成し、先頭に「すべて」を追加
                val tags = listOf("すべて") + productsByTag.keys.sorted()

                // 「すべて」タブ用に全商品のマッピングを追加
                val finalProductsMap = mapOf("すべて" to products) + productsByTag

                _uiState.update {
                    it.copy(
                        tags = tags,
                        productsByTag = finalProductsMap,
                        isLoading = false
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}