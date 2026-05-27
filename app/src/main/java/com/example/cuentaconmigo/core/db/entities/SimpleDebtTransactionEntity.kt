package com.example.cuentaconmigo.core.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "simple_debt_transactions",
    foreignKeys = [
        ForeignKey(
            entity = SimpleDebtEntity::class,
            parentColumns = ["id"],
            childColumns = ["debtId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("debtId"), Index("userId")]
)
data class SimpleDebtTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val debtId: Long,
    val userId: Long,
    val type: String,                       // "LOAN_RECEIVED" | "PAYMENT" | "INTEREST"
    val amount: Long,                       // centavos, siempre positivo
    val description: String? = null,
    val date: Long,                         // epoch millis
    val depositAccountId: Long? = null,     // para LOAN_RECEIVED y PAYMENT
    val linkedTransactionId: Long? = null   // ID del Transaction creado en la tabla transactions
)
