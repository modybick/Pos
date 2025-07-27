package com.example.pos.ui.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pos.utils.toCurrencyFormat
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ProductListScreen(
    productListViewModel: ProductListViewModel = hiltViewModel(),
    onProductSelected: (String) -> Unit
) {
    val uiState by productListViewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxWidth()) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxWidth().height(400.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.tags.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { uiState.tags.size })
            val scope = rememberCoroutineScope()

            // タブ
            TabRow(selectedTabIndex = pagerState.currentPage) {
                uiState.tags.forEachIndexed { index, title ->
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
                modifier = Modifier.fillMaxWidth().weight(1f)
            ) { page ->
                val currentTag = uiState.tags[page]
                val productsForTag = uiState.productsByTag[currentTag] ?: emptyList()

                LazyColumn(modifier = Modifier.fillMaxHeight()) {
                    items(productsForTag, key = { it.barcode }) { product ->
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
            Text(product.tag ?: "", style = MaterialTheme.typography.bodySmall)
        }
        Text("${product.price.toCurrencyFormat()} 円")
    }
}