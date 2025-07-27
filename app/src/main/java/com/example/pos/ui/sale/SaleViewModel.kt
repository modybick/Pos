package com.example.pos.ui.sale

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import android.app.Application // Applicationをインポート
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.lifecycle.AndroidViewModel // ViewModelから変更
import androidx.lifecycle.viewModelScope
import com.example.pos.R // Rファイルをインポート
import kotlinx.coroutines.launch
import com.example.pos.data.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.example.pos.database.Sale
import com.example.pos.database.SaleDao
import com.example.pos.database.SaleDetail
import java.util.Date

@HiltViewModel
class SaleViewModel @Inject constructor(
    application: Application,
    private val productRepository: ProductRepository,
    private val saleDao: SaleDao
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SaleUiState())
    val uiState: StateFlow<SaleUiState> = _uiState.asStateFlow()

    private var soundPool: SoundPool
    private var scanSoundId: Int = 0
    private var errorSoundId: Int = 0
    private var checkoutSoundId: Int = 0

    // サンプル用に初期データを追加
    init {
        // SoundPoolの初期化
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()

        // 音声ファイルの読み込み
        scanSoundId = soundPool.load(getApplication(), R.raw.success, 1)
        errorSoundId = soundPool.load(getApplication(), R.raw.error, 1)
        checkoutSoundId = soundPool.load(getApplication(), R.raw.checkout, 1)
    }

    fun onQuantityChanged(barcode: String, change: Int) {
        val currentItems = _uiState.value.cartItems.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.product.barcode == barcode }

        if (itemIndex != -1) {
            val item = currentItems[itemIndex]
            val newQuantity = item.quantity + change
            if (newQuantity > 0) {
                currentItems[itemIndex] = item.copy(quantity = newQuantity)
            } else {
                currentItems.removeAt(itemIndex)
            }
            _uiState.update {
                it.copy(
                    cartItems = currentItems,
                    totalAmount = calculateTotal(currentItems)
                )
            }
        }
    }

    private fun calculateTotal(items: List<CartItem>): Int {
        return items.sumOf { it.product.price * it.quantity }
    }

    fun onBarcodeScanned(barcode: String) {
        viewModelScope.launch {
            // リポジトリを使ってDBから商品を検索
            val product = productRepository.findProductByBarcode(barcode)

            if (product != null) {
                // --- 商品が見つかった場合 ---
                soundPool.play(scanSoundId, 1f, 1f, 0, 0, 1f)

                val currentItems = _uiState.value.cartItems
                val existingItem = currentItems.find { it.product.barcode == barcode }

                if (existingItem != null) {
                    // 既にカートにあれば数量を+1
                    onQuantityChanged(barcode, 1)
                } else {
                    // カートになければ新規追加
                    val newItems = currentItems + CartItem(product, 1)
                    _uiState.update {
                        it.copy(cartItems = newItems, totalAmount = calculateTotal(newItems))
                    }
                }
            } else {
                // --- 商品が見つからなかった場合 ---
                soundPool.play(errorSoundId, 1f, 1f, 0, 0, 1f)
                // TODO: ユーザーに「商品が見つかりません」と通知する（Toastなど）
            }
        }
    }

    fun finalizeSale(tenderedAmount: Int) {
        if (_uiState.value.cartItems.isEmpty()) return

        soundPool.play(checkoutSoundId, 1f, 1f, 0, 0, 1f)

        viewModelScope.launch {
            val total = _uiState.value.totalAmount
            val change = tenderedAmount - total

            val sale = Sale(
                createdAt = Date(),
                totalAmount = _uiState.value.totalAmount,
                tenderedAmount = tenderedAmount,
                changeAmount = change,
                isCancelled = false
            )

            val details = _uiState.value.cartItems.map { cartItem ->
                SaleDetail(
                    saleId = 0, // Dao側で設定されるので仮の値
                    productBarcode = cartItem.product.barcode,
                    productName = cartItem.product.name,
                    price = cartItem.product.price,
                    quantity = cartItem.quantity
                )
            }
            // DBに保存
            saleDao.insertSaleAndDetails(sale, details)
            // カートを空にする
            _uiState.value = SaleUiState()
        }
    }

    fun clearCart() {
        _uiState.value = SaleUiState()
    }

    override fun onCleared() {
        super.onCleared()
        // ViewModelが破棄されるときにSoundPoolを解放
        soundPool.release()
    }
}