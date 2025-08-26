package com.example.pos.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.pos.R
import com.example.pos.ui.sale.SaleViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    saleViewModel: SaleViewModel = hiltViewModel(), // SaleViewModelを注入
    onNavigateBack: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    // 設定値の状態を監視
    val uiState by settingsViewModel.uiState.collectAsState()

    // ファイル選択ランチャーを準備
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            uri?.let {
                saleViewModel.importProductsFromCsv(it)
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("設定") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // バーコード読み取り設定
            Text("バーコード読み取り設定", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // スライダーを秒単位で表示
            val scanIntervalInSeconds = (uiState.scanIntervalInSeconds * 100.0).roundToInt() / 100.0
            Text(text = "読み取り間隔: $scanIntervalInSeconds 秒")
            Slider(
                value = uiState.scanIntervalInSeconds.toFloat(),
                onValueChange = {
                    settingsViewModel.updateScanInterval(it.toDouble())
                },
                valueRange = 0.1f..5.0f, // 0.1秒から1.0秒の範囲に設定
                steps = 48 // 0.1秒単位でステップを設定
            )


            Spacer(modifier = Modifier.height(32.dp))

            // データベース設定
            Text("データベース設定", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            // インポートボタン
            OutlinedButton(
                onClick = { importLauncher.launch("text/csv") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(
                        id = R.drawable.download
                    ), contentDescription = "インポート"
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("商品データベースをインポート")
            }

            Text(
                "CSVファイルから商品データを読み込み、既存のデータは上書きされます。",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, start = 8.dp, end = 8.dp)
            )
        }
    }
}