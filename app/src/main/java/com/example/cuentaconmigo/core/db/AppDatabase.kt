package com.example.cuentaconmigo.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun depositAccountDao(): DepositAccountDao
    abstract fun destinationAccountDao(): DestinationAccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun investmentFluctuationDao(): InvestmentFluctuationDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE destination_accounts ADD COLUMN investmentSubtype TEXT"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE destination_accounts ADD COLUMN parentAccountId INTEGER"
                )
            }
        }
    }
}
