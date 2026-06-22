package com.example.cuentaconmigo.core.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "simple_debts",
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
data class SimpleDebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
    val kind: String = "RECEIVED"          // "RECEIVED" | "GIVEN"
)
