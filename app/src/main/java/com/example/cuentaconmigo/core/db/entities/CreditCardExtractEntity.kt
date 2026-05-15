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
    val billingAmount: Long,        // Facturación del mes (centavos)
    val currentInterest: Long,      // Intereses corrientes (centavos)
    val lateInterest: Long,         // Intereses de mora (centavos)
    val otherCharges: Long,         // Otros cargos — cuota manejo, seguros (centavos)
    val paymentsAndCredits: Long,   // Pagos y abonos según banco (centavos)
    val totalBankBalance: Long,     // Saldo total banco (centavos)
    val minimumPayment: Long,       // Pago mínimo (centavos)
    val uncollectedInterest: Long,  // Intereses no cobrados (centavos)
    val isReconciled: Boolean = false,
    val registeredAt: Long = System.currentTimeMillis()
)
