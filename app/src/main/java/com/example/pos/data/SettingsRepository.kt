package com.example.pos.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStoreインスタンスを生成
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepository @Inject constructor(@ApplicationContext private val context: Context) {
    // データストアのキーを定義
    companion object {
        val BARCODE_SCAN_INTERVAL = longPreferencesKey("barcode_scan_interval")
        const val DEFAULT_SCAN_INTERVAL = 1000L // 1000ミリ秒をデフォルト値とする
    }

    // 設定値をFlowとして公開
    val barcodeScanInterval: Flow<Long> = context.dataStore.data
        .map { preferences ->
            preferences[BARCODE_SCAN_INTERVAL] ?: DEFAULT_SCAN_INTERVAL
        }

    // 設定値を保存する
    suspend fun setBarcodeScanInterval(interval: Long) {
        context.dataStore.edit { preferences ->
            preferences[BARCODE_SCAN_INTERVAL] = interval
        }
    }
}