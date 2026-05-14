package com.example.cuentaconmigo.core.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_card_transactions",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["creditCardId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("creditCardId"), Index("userId")]
)
data class CreditCardTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val creditCardId: Long,
    val userId: Long,
    val type: String,                        // "PURCHASE" | "PAYMENT" | "INTEREST" | "FEE"
    val amount: Long,                        // centavos, siempre positivo
    val description: String? = null,
    val date: Long,                          // epoch millis
    val destinationAccountId: Long? = null,  // para PURCHASE
    val linkedTransactionId: Long? = null,   // para PAYMENT: ID del Transaction EXPENSE creado
    val installments: Int = 1               // 1 = de contado, N = en cuotas
)