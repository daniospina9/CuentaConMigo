package com.example.cuentaconmigo.core.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "destination_accounts",
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
data class DestinationAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val type: String,       // "expense" | "savings" | "investment"
    val isDefault: Boolean = false,
    val investmentSubtype: String? = null,
    val parentAccountId: Long? = null   // null = top-level; non-null = investment sub-account
)
