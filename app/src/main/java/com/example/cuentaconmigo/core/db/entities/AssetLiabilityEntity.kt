package com.example.cuentaconmigo.core.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "asset_liabilities",
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
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("userId"), Index("subAccountId")]
)
data class AssetLiabilityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val subAccountId: Long,
    val description: String,
    val amount: Long,
    val createdDate: Long,
    val isPaid: Boolean = false
)
