package com.example.exchangenotifier.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "rate_snapshots")
data class RateSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val rate: Double,
    val timestampMillis: Long,
    val pairKey: String,
)
