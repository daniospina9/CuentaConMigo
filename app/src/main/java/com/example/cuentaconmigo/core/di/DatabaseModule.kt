package com.example.cuentaconmigo.core.di

import android.content.Context
import androidx.room.Room
import com.example.cuentaconmigo.core.db.AppDatabase
import com.example.cuentaconmigo.core.db.dao.AssetLiabilityDao
import com.example.cuentaconmigo.core.db.dao.AssetOperationDao
import com.example.cuentaconmigo.core.db.dao.DepositAccountDao
import com.example.cuentaconmigo.core.db.dao.DestinationAccountDao
import com.example.cuentaconmigo.core.db.dao.InvestmentFluctuationDao
import com.example.cuentaconmigo.core.db.dao.TransactionDao
import com.example.cuentaconmigo.core.db.dao.UserDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "cuentaconmigo.db")
            .addMigrations(
                AppDatabase.MIGRATION_2_3,
                AppDatabase.MIGRATION_3_4,
                AppDatabase.MIGRATION_4_5,
                AppDatabase.MIGRATION_5_6,
                AppDatabase.MIGRATION_6_7
            )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao = db.userDao()

    @Provides
    fun provideDepositAccountDao(db: AppDatabase): DepositAccountDao = db.depositAccountDao()

    @Provides
    fun provideDestinationAccountDao(db: AppDatabase): DestinationAccountDao = db.destinationAccountDao()

    @Provides
    fun provideTransactionDao(db: AppDatabase): TransactionDao = db.transactionDao()

    @Provides
    fun provideInvestmentFluctuationDao(db: AppDatabase): InvestmentFluctuationDao = db.investmentFluctuationDao()

    @Provides
    fun provideAssetOperationDao(db: AppDatabase): AssetOperationDao = db.assetOperationDao()

    @Provides
    fun provideAssetLiabilityDao(db: AppDatabase): AssetLiabilityDao = db.assetLiabilityDao()
}
