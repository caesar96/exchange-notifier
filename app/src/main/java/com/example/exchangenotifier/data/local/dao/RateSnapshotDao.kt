package com.example.exchangenotifier.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.exchangenotifier.data.local.entity.RateSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RateSnapshotDao {

    @Insert
    suspend fun insert(entity: RateSnapshotEntity)

    /** Live stream of snapshots from [fromMillis] onward — used by the intraday chart. */
    @Query("SELECT * FROM rate_snapshots WHERE timestampMillis >= :fromMillis ORDER BY timestampMillis ASC")
    fun observeSnapshotsAfter(fromMillis: Long): Flow<List<RateSnapshotEntity>>

    /** One-shot range query. */
    @Query("SELECT * FROM rate_snapshots WHERE timestampMillis BETWEEN :fromMillis AND :toMillis ORDER BY timestampMillis ASC")
    suspend fun getSnapshotsBetween(fromMillis: Long, toMillis: Long): List<RateSnapshotEntity>

    /** Removes snapshots older than [beforeMillis]; called by the worker on each run. */
    @Query("DELETE FROM rate_snapshots WHERE timestampMillis < :beforeMillis")
    suspend fun deleteOlderThan(beforeMillis: Long)

    @Query("DELETE FROM rate_snapshots")
    suspend fun deleteAll()
}
