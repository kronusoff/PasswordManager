package com.example.passwordmanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passwords")
data class PasswordEntry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val accountName: String,
    val password: String,
    val timestamp: Long
)