package com.example.cuentaconmigo.core.db.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "asset_operations",
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
data class AssetOperationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val subAccountId: Long,
    val type: String,
    val date: Long,
    val balanceEffect: Long,
    val assetValueDelta: Long = 0L,
    val description: String? = null,
    val liabilityId: Long? = null,
    val withdrawalGroupId: String? = null
)
