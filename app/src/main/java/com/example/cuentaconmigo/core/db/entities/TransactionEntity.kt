package com.example.cuentaconmigo.core.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = DepositAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["depositAccountId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = DestinationAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["destinationAccountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("userId"),
        Index("depositAccountId"),
        Index("destinationAccountId")
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val depositAccountId: Long,
    val destinationAccountId: Long? = null, // null for INCOME
    val type: String,           // "INCOME" | "EXPENSE"
    val amount: Long,           // pesos COP, always positive
    val date: Long,             // LocalDate.toEpochDay()
    val description: String? = null
)
