package com.example.cuentaconmigo.core.di

import com.example.cuentaconmigo.core.db.repository.AssetLiabilityRepositoryImpl
import com.example.cuentaconmigo.core.db.repository.AssetOperationRepositoryImpl
import com.example.cuentaconmigo.core.db.repository.DepositAccountRepositoryImpl
import com.example.cuentaconmigo.core.db.repository.DestinationAccountRepositoryImpl
import com.example.cuentaconmigo.core.db.repository.InvestmentFluctuationRepositoryImpl
import com.example.cuentaconmigo.core.db.repository.SavingsMovementRepositoryImpl
import com.example.cuentaconmigo.core.db.repository.TransactionRepositoryImpl
import com.example.cuentaconmigo.core.db.repository.UserRepositoryImpl
import com.example.cuentaconmigo.domain.repository.AssetLiabilityRepository
import com.example.cuentaconmigo.domain.repository.AssetOperationRepository
import com.example.cuentaconmigo.domain.repository.DepositAccountRepository
import com.example.cuentaconmigo.domain.repository.DestinationAccountRepository
import com.example.cuentaconmigo.domain.repository.InvestmentFluctuationRepository
import com.example.cuentaconmigo.domain.repository.SavingsMovementRepository
import com.example.cuentaconmigo.domain.repository.TransactionRepository
import com.example.cuentaconmigo.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindUserRepository(impl: UserRepositoryImpl): UserRepository

    @Binds @Singleton
    abstract fun bindDepositAccountRepository(impl: DepositAccountRepositoryImpl): DepositAccountRepository

    @Binds @Singleton
    abstract fun bindDestinationAccountRepository(impl: DestinationAccountRepositoryImpl): DestinationAccountRepository

    @Binds @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    @Binds @Singleton
    abstract fun bindInvestmentFluctuationRepository(impl: InvestmentFluctuationRepositoryImpl): InvestmentFluctuationRepository

    @Binds @Singleton
    abstract fun bindAssetOperationRepository(impl: AssetOperationRepositoryImpl): AssetOperationRepository

    @Binds @Singleton
    abstract fun bindAssetLiabilityRepository(impl: AssetLiabilityRepositoryImpl): AssetLiabilityRepository

    @Binds @Singleton
    abstract fun bindSavingsMovementRepository(impl: SavingsMovementRepositoryImpl): SavingsMovementRepository
}
