package com.example.pos.di

import android.content.Context
import androidx.room.Room
import com.example.pos.data.ProductRepository
import com.example.pos.data.ProductRepositoryImpl
import com.example.pos.data.SaleRepository
import com.example.pos.data.SaleRepositoryImpl
import com.example.pos.database.AppDatabase
import com.example.pos.database.ProductDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.pos.database.SaleDao

@Module
@InstallIn(SingletonComponent::class) // アプリ全体で共有するインスタンス
object AppModule {

    @Provides
    @Singleton // 常に同じインスタンスを返す
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pos_database.db"
        ).createFromAsset("prepackaged.db") // 事前DBの利用
            .build()
    }

    @Provides
    @Singleton
    fun provideProductDao(db: AppDatabase): ProductDao {
        return db.productDao()
    }

    @Provides
    @Singleton
    fun provideSaleDao(db: AppDatabase): SaleDao {
        return db.saleDao()
    }
}

// インターフェースと実装クラスを紐付けるためのモジュール
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindProductRepository(
        productRepositoryImpl: ProductRepositoryImpl
    ): ProductRepository

    @Binds
    @Singleton
    abstract fun bindSaleRepository(
        saleRepositoryImpl: SaleRepositoryImpl
    ): SaleRepository
}