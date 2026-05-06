package com.example.cuentaconmigo.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cuentaconmigo.core.db.converters.Converters
import com.example.cuentaconmigo.core.db.dao.AssetLiabilityDao
import com.example.cuentaconmigo.core.db.dao.AssetOperationDao
import com.example.cuentaconmigo.core.db.dao.DepositAccountDao
import com.example.cuentaconmigo.core.db.dao.DestinationAccountDao
import com.example.cuentaconmigo.core.db.dao.InvestmentFluctuationDao
import com.example.cuentaconmigo.core.db.dao.TransactionDao
import com.example.cuentaconmigo.core.db.dao.UserDao
import com.example.cuentaconmigo.core.db.entities.AssetLiabilityEntity
import com.example.cuentaconmigo.core.db.entities.AssetOperationEntity
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
        InvestmentFluctuationEntity::class,
        AssetOperationEntity::class,
        AssetLiabilityEntity::class
    ],
    version = 7,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun depositAccountDao(): DepositAccountDao
    abstract fun destinationAccountDao(): DestinationAccountDao
    abstract fun transactionDao(): TransactionDao
    abstract fun investmentFluctuationDao(): InvestmentFluctuationDao
    abstract fun assetOperationDao(): AssetOperationDao
    abstract fun assetLiabilityDao(): AssetLiabilityDao

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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("UPDATE transactions SET amount = amount * 100")
                database.execSQL("UPDATE investment_fluctuations SET amount = amount * 100")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE investment_fluctuations ADD COLUMN withdrawalGroupId TEXT"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE destination_accounts ADD COLUMN assetInitialValue INTEGER NOT NULL DEFAULT 0")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS asset_operations (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        subAccountId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        date INTEGER NOT NULL,
                        balanceEffect INTEGER NOT NULL,
                        assetValueDelta INTEGER NOT NULL DEFAULT 0,
                        description TEXT,
                        liabilityId INTEGER,
                        withdrawalGroupId TEXT,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY(subAccountId) REFERENCES destination_accounts(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_asset_operations_userId ON asset_operations(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_asset_operations_subAccountId ON asset_operations(subAccountId)")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS asset_liabilities (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        subAccountId INTEGER NOT NULL,
                        description TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        createdDate INTEGER NOT NULL,
                        isPaid INTEGER NOT NULL DEFAULT 0,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY(subAccountId) REFERENCES destination_accounts(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_asset_liabilities_userId ON asset_liabilities(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_asset_liabilities_subAccountId ON asset_liabilities(subAccountId)")
            }
        }
    }
}
