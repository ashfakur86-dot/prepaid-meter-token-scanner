package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TokenDao {
    @Query("SELECT * FROM tokens ORDER BY timestamp ASC")
    fun getAllTokens(): Flow<List<TokenEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertToken(token: TokenEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tokens: List<TokenEntity>)

    @Update
    suspend fun updateToken(token: TokenEntity)

    @Query("DELETE FROM tokens WHERE id = :id")
    suspend fun deleteTokenById(id: Int)

    @Query("DELETE FROM tokens")
    suspend fun clearAllTokens()
}
