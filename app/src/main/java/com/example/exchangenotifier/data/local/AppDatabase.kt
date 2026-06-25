package com.example.exchangenotifier.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.exchangenotifier.data.local.dao.RateSnapshotDao
import com.example.exchangenotifier.data.local.entity.RateSnapshotEntity

@Database(
    entities = [RateSnapshotEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun rateSnapshotDao(): RateSnapshotDao
}
