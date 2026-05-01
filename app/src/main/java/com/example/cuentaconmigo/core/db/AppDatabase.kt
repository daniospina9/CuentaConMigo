package com.example.cuentaconmigo.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.cuentaconmigo.core.db.converters.Converters
import com.example.cuentaconmigo.core.db.dao.DepositAccountDao
import com.example.cuentaconmigo.core.db.dao.DestinationAccountDao
import com.example.cuentaconmigo.core.db.dao.InvestmentFluctuationDao
import com.example.cuentaconmigo.core.db.dao.TransactionDao
import com.example.cuentaconmigo.core.db.dao.UserDao
import com.example.cuentaconmigo.core.db.entities.DepositAccountEntity
import com.example.cuentaconmigo.core.db.entities.DestinationAccountEntity
import com.example.cuentaconmigo.core.db.entities.InvestmentFluctuationEntity
import com.example.cuentaconmigo.core.db.entities.TransactionEntity
import com.example.cuentaconmigo.core.db.entities.UserEntity

@Database(
    entities = [
        UserEntity::class,
        DepositAccountEntity::class,
        DestinationAccountEntity::class,
        TransactionEntity::class,
        InvestmentFluctuationEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun depositAccountDao(): DepositAccountDao
    abstract fun destinationAccountDao(): DestinationAccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun investmentFluctuationDao(): InvestmentFluctuationDao
}
