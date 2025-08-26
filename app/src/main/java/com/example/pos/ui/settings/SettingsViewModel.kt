package com.example.pos.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pos.data.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val scanIntervalInSeconds: Double = 2.0
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        // 設定を初期化
        viewModelScope.launch {
            settingsRepository.barcodeScanInterval.collectLatest { intervalInMs ->
                _uiState.value = _uiState.value.copy(scanIntervalInSeconds = intervalInMs / 1000.0)
            }
        }
    }

    fun updateScanInterval(intervalInSeconds: Double) {
        viewModelScope.launch {
            // UIから秒単位の値を受け取り、ミリ秒に変換してリポジトリに保存
            settingsRepository.setBarcodeScanInterval((intervalInSeconds * 1000).toLong())
        }
    }
}