package com.example.cuentaconmigo.core.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "credit_cards",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId")]
)
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val lastFourDigits: String? = null,
    val creditLimit: Long,           // centavos
    val interestRateAnnual: Double,  // TEA, ej: 26.0 para 26% EA
    val cutOffDay: Int,              // 1-31
    val paymentDueDay: Int,          // día del mes en que vence el pago
    val minPaymentType: String,      // "PERCENTAGE" | "FIXED"
    val minPaymentPercent: Double = 0.0,
    val minPaymentFixed: Long = 0L,  // centavos
    val monthlyFee: Long = 0L,       // cuota de manejo mensual, centavos
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)
