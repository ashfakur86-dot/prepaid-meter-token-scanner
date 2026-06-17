package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tokens")
data class TokenEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isEntered: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
