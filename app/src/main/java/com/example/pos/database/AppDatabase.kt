package com.example.pos.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters

@Database(
    entities = [
        Product::class,
        Sale::class,
        SaleDetail::class
    ],
    version = 3,
    exportSchema = false // 👈 この行を追加
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
}