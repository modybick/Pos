package com.example.pos.data

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceIdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("pos_prefs", Context.MODE_PRIVATE)
    private var deviceId: String? = null

    fun getDeviceId(): String {
        if (deviceId != null) return deviceId!!

        var id = prefs.getString(KEY_DEVICE_ID, null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit {
                putString(KEY_DEVICE_ID, id)
            }
        }
        deviceId = id
        return id
    }

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
    }
}