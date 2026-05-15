package com.example.cuentaconmigo.core.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_card_extracts",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["creditCardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("creditCardId")]
)
data class CreditCardExtractEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val creditCardId: Long,
    val cutOffDate: Long,
    val billingAmount: Long,
    val currentInterest: Long,
    val lateInterest: Long,
    val otherCharges: Long,
    val paymentsAndCredits: Long,
    val totalBankBalance: Long,
    val minimumPayment: Long,
    val uncollectedInterest: Long,
    val isReconciled: Boolean = false,
    val registeredAt: Long = System.currentTimeMillis()
)
