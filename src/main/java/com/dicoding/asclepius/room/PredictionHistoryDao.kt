package com.dicoding.asclepius.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.asclepius.entity.PredictionHistory

@Dao
interface PredictionHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(predictionHistory: PredictionHistory)


    @Query("SELECT * FROM prediction_history ORDER BY id DESC")
    suspend fun getAllHistory(): List<PredictionHistory>
}
