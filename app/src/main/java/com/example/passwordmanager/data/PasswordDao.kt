package com.example.passwordmanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {
    @Insert
    suspend fun insert(password: PasswordEntry)

    @Query("SELECT * FROM passwords ORDER BY timestamp DESC")
    fun getAllPasswords(): Flow<List<PasswordEntry>>

    @Delete
    suspend fun delete(password: PasswordEntry)
}