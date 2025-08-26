package com.example.pos.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pos.utils.toCurrencyFormat
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    androidx.compose.foundation.ExperimentalFoundationApi::class
)
@Composable
fun ProductListScreen(
    productListViewModel: ProductListViewModel = hiltViewModel(),
    onProductSelected: (String) -> Unit
) {
    val uiState by productListViewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        if (uiState.isLoading) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(400.dp), contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.categories.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { uiState.categories.size })
            val scope = rememberCoroutineScope()

            // タブ
            TabRow(selectedTabIndex = pagerState.currentPage) {
                uiState.categories.forEachIndexed { index, title ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        text = { Text(title) }
                    )
                }
            }

            // ページャー
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                val currentCategory = uiState.categories[page]
                val productsForCategory = uiState.productsByCategory[currentCategory] ?: emptyList()

                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(productsForCategory, key = { it.barcode }) { product ->
                        ProductRow(
                            product = product,
                            onClick = { onProductSelected(product.barcode) }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun ProductRow(product: com.example.pos.database.Product, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, style = MaterialTheme.typography.bodyLarge)
            Text(product.category ?: "", style = MaterialTheme.typography.bodySmall)
        }
        Text("${product.price.toCurrencyFormat()} 円")
    }
}