package com.example.cuentaconmigo.core.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "savings_movements",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DestinationAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["subAccountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("userId"),
        Index("subAccountId")
    ]
)
data class SavingsMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val subAccountId: Long,
    val amount: Long,           // positivo = depósito, negativo = retiro
    val date: Long,             // LocalDate.toEpochDay()
    val description: String? = null,
    val groupId: String? = null // liga el movimiento con su transaction pareada
)