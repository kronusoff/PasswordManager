package com.example.passwordmanager.util

import java.security.SecureRandom

object PasswordGenerator {
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS = "0123456789"
    private const val SPECIAL = "!@#$%^&*()-_=+"

    fun generatePassword(
        length: Int,
        useLowercase: Boolean,
        useUppercase: Boolean,
        useDigits: Boolean,
        useSpecial: Boolean
    ): String {
        val chars = StringBuilder()
        if (useLowercase) chars.append(LOWERCASE)
        if (useUppercase) chars.append(UPPERCASE)
        if (useDigits) chars.append(DIGITS)
        if (useSpecial) chars.append(SPECIAL)

        if (chars.isEmpty()) return ""

        val random = SecureRandom()
        return (1..length)
            .map { chars[random.nextInt(chars.length)] }
            .joinToString("")
    }
}