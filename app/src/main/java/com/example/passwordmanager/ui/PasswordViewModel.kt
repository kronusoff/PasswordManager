package com.example.passwordmanager.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.passwordmanager.data.AppDatabase
import com.example.passwordmanager.data.PasswordEntry
import com.example.passwordmanager.util.PasswordGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PasswordViewModel(private val database: AppDatabase) : ViewModel() {
    private val _passwords = MutableStateFlow<List<PasswordEntry>>(emptyList())
    val passwords: StateFlow<List<PasswordEntry>> = _passwords.asStateFlow()

    init {
        viewModelScope.launch {
            database.passwordDao().getAllPasswords().collect { passwordList ->
                _passwords.value = passwordList
            }
        }
    }

    fun generatePassword(
        length: Int,
        useLowercase: Boolean,
        useUppercase: Boolean,
        useDigits: Boolean,
        useSpecial: Boolean
    ): String {
        return PasswordGenerator.generatePassword(length, useLowercase, useUppercase, useDigits, useSpecial)
    }

    fun savePassword(accountName: String, password: String) {
        viewModelScope.launch {
            val passwordEntry = PasswordEntry(
                accountName = accountName,
                password = password,
                timestamp = System.currentTimeMillis()
            )
            database.passwordDao().insert(passwordEntry)
        }
    }

    fun deletePassword(password: PasswordEntry) {
        viewModelScope.launch {
            database.passwordDao().delete(password)
        }
    }
}