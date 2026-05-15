package com.example.cuentaconmigo.core.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.cuentaconmigo.core.db.converters.Converters
import com.example.cuentaconmigo.core.db.dao.AssetLiabilityDao
import com.example.cuentaconmigo.core.db.dao.AssetOperationDao
import com.example.cuentaconmigo.core.db.dao.CreditCardDao
import com.example.cuentaconmigo.core.db.dao.CreditCardExtractDao
import com.example.cuentaconmigo.core.db.dao.CreditCardTransactionDao
import com.example.cuentaconmigo.core.db.dao.DepositAccountDao
import com.example.cuentaconmigo.core.db.dao.DestinationAccountDao
import com.example.cuentaconmigo.core.db.dao.InvestmentFluctuationDao
import com.example.cuentaconmigo.core.db.dao.SavingsMovementDao
import com.example.cuentaconmigo.core.db.dao.TransactionDao
import com.example.cuentaconmigo.core.db.dao.UserDao
import com.example.cuentaconmigo.core.db.entities.AssetLiabilityEntity
import com.example.cuentaconmigo.core.db.entities.AssetOperationEntity
import com.example.cuentaconmigo.core.db.entities.CreditCardEntity
import com.example.cuentaconmigo.core.db.entities.CreditCardExtractEntity
import com.example.cuentaconmigo.core.db.entities.CreditCardTransactionEntity
import com.example.cuentaconmigo.core.db.entities.DepositAccountEntity
import com.example.cuentaconmigo.core.db.entities.DestinationAccountEntity
import com.example.cuentaconmigo.core.db.entities.InvestmentFluctuationEntity
import com.example.cuentaconmigo.core.db.entities.SavingsMovementEntity
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
        AssetLiabilityEntity::class,
        SavingsMovementEntity::class,
        CreditCardEntity::class,
        CreditCardTransactionEntity::class,
        CreditCardExtractEntity::class
    ],
    version = 14,
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
    abstract fun savingsMovementDao(): SavingsMovementDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun creditCardTransactionDao(): CreditCardTransactionDao
    abstract fun creditCardExtractDao(): CreditCardExtractDao

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

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS savings_movements (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        subAccountId INTEGER NOT NULL,
                        amount INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        description TEXT,
                        groupId TEXT,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY(subAccountId) REFERENCES destination_accounts(id) ON DELETE RESTRICT
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_savings_movements_userId ON savings_movements(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_savings_movements_subAccountId ON savings_movements(subAccountId)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE savings_movements_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        subAccountId INTEGER NOT NULL,
                        amount INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        description TEXT,
                        groupId TEXT,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY(subAccountId) REFERENCES destination_accounts(id) ON DELETE RESTRICT
                    )
                """.trimIndent())
                database.execSQL("INSERT INTO savings_movements_new SELECT * FROM savings_movements")
                database.execSQL("DROP TABLE savings_movements")
                database.execSQL("ALTER TABLE savings_movements_new RENAME TO savings_movements")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_savings_movements_userId ON savings_movements(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_savings_movements_subAccountId ON savings_movements(subAccountId)")
            }
        }

        val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS credit_cards (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        lastFourDigits TEXT,
                        creditLimit INTEGER NOT NULL,
                        interestRateMonthly REAL NOT NULL,
                        cutOffDay INTEGER NOT NULL,
                        paymentDueDay INTEGER NOT NULL,
                        minPaymentType TEXT NOT NULL,
                        minPaymentPercent REAL NOT NULL DEFAULT 0.0,
                        minPaymentFixed INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_credit_cards_userId ON credit_cards(userId)")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS credit_card_transactions (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        creditCardId INTEGER NOT NULL,
                        userId INTEGER NOT NULL,
                        type TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        description TEXT,
                        date INTEGER NOT NULL,
                        destinationAccountId INTEGER,
                        linkedTransactionId INTEGER,
                        FOREIGN KEY(creditCardId) REFERENCES credit_cards(id) ON DELETE CASCADE,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_credit_card_transactions_creditCardId ON credit_card_transactions(creditCardId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_credit_card_transactions_userId ON credit_card_transactions(userId)")
            }
        }

        val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Recreate credit_cards renaming paymentDueDays → paymentDueDay
                database.execSQL("DROP TABLE IF EXISTS credit_cards")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS credit_cards (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        lastFourDigits TEXT,
                        creditLimit INTEGER NOT NULL,
                        interestRateMonthly REAL NOT NULL,
                        cutOffDay INTEGER NOT NULL,
                        paymentDueDay INTEGER NOT NULL,
                        minPaymentType TEXT NOT NULL,
                        minPaymentPercent REAL NOT NULL DEFAULT 0.0,
                        minPaymentFixed INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_credit_cards_userId ON credit_cards(userId)")
            }
        }

        val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Recreate credit_cards: rename interestRateMonthly → interestRateAnnual, add monthlyFee
                database.execSQL("DROP TABLE IF EXISTS credit_cards")
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS credit_cards (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        lastFourDigits TEXT,
                        creditLimit INTEGER NOT NULL,
                        interestRateAnnual REAL NOT NULL,
                        cutOffDay INTEGER NOT NULL,
                        paymentDueDay INTEGER NOT NULL,
                        minPaymentType TEXT NOT NULL,
                        minPaymentPercent REAL NOT NULL DEFAULT 0.0,
                        minPaymentFixed INTEGER NOT NULL DEFAULT 0,
                        monthlyFee INTEGER NOT NULL DEFAULT 0,
                        createdAt INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_credit_cards_userId ON credit_cards(userId)")
                // Add installments column to credit_card_transactions
                database.execSQL("ALTER TABLE credit_card_transactions ADD COLUMN installments INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS transactions_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        userId INTEGER NOT NULL,
                        depositAccountId INTEGER,
                        destinationAccountId INTEGER,
                        type TEXT NOT NULL,
                        amount INTEGER NOT NULL,
                        date INTEGER NOT NULL,
                        description TEXT,
                        transferGroupId TEXT,
                        FOREIGN KEY(userId) REFERENCES users(id) ON DELETE CASCADE,
                        FOREIGN KEY(depositAccountId) REFERENCES deposit_accounts(id) ON DELETE CASCADE,
                        FOREIGN KEY(destinationAccountId) REFERENCES destination_accounts(id) ON DELETE RESTRICT
                    )
                """.trimIndent())
                database.execSQL("INSERT INTO transactions_new SELECT * FROM transactions")
                database.execSQL("DROP TABLE transactions")
                database.execSQL("ALTER TABLE transactions_new RENAME TO transactions")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_userId ON transactions(userId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_depositAccountId ON transactions(depositAccountId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_transactions_destinationAccountId ON transactions(destinationAccountId)")
            }
        }

        val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS credit_card_extracts (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        creditCardId INTEGER NOT NULL,
                        billingAmount INTEGER NOT NULL,
                        currentInterest INTEGER NOT NULL,
                        lateInterest INTEGER NOT NULL,
                        otherCharges INTEGER NOT NULL,
                        paymentsAndCredits INTEGER NOT NULL,
                        totalBankBalance INTEGER NOT NULL,
                        minimumPayment INTEGER NOT NULL,
                        uncollectedInterest INTEGER NOT NULL,
                        isReconciled INTEGER NOT NULL DEFAULT 0,
                        registeredAt INTEGER NOT NULL,
                        FOREIGN KEY(creditCardId) REFERENCES credit_cards(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                database.execSQL("CREATE INDEX IF NOT EXISTS index_credit_card_extracts_creditCardId ON credit_card_extracts(creditCardId)")
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
