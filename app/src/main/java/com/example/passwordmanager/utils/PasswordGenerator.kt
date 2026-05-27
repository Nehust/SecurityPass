package com.example.passwordmanager.utils

import java.security.SecureRandom

object PasswordGenerator {
    private const val UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
    private const val NUMBERS = "0123456789"
    private const val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?"

    private val secureRandom = SecureRandom()

    fun generatePassword(
        length: Int,
        useUpper: Boolean,
        useLower: Boolean,
        useNumbers: Boolean,
        useSymbols: Boolean
    ): String {
        if (length <= 0) return ""
        if (!useUpper && !useLower && !useNumbers && !useSymbols) return ""

        val charPool = StringBuilder()
        val password = StringBuilder()

        // Ensure at least one character of each selected type is included
        if (useUpper) {
            charPool.append(UPPERCASE)
            password.append(UPPERCASE[secureRandom.nextInt(UPPERCASE.length)])
        }
        if (useLower) {
            charPool.append(LOWERCASE)
            password.append(LOWERCASE[secureRandom.nextInt(LOWERCASE.length)])
        }
        if (useNumbers) {
            charPool.append(NUMBERS)
            password.append(NUMBERS[secureRandom.nextInt(NUMBERS.length)])
        }
        if (useSymbols) {
            charPool.append(SYMBOLS)
            password.append(SYMBOLS[secureRandom.nextInt(SYMBOLS.length)])
        }

        val poolString = charPool.toString()

        // Fill the rest of the password
        while (password.length < length) {
            password.append(poolString[secureRandom.nextInt(poolString.length)])
        }

        // Shuffle the characters to prevent predictable patterns (like always starting with an uppercase letter)
        val passwordChars = password.toString().toCharArray()
        for (i in passwordChars.indices) {
            val randomIndex = secureRandom.nextInt(passwordChars.size)
            val temp = passwordChars[i]
            passwordChars[i] = passwordChars[randomIndex]
            passwordChars[randomIndex] = temp
        }

        // Return truncated in case the minimum required characters exceeded the length (e.g. length = 2, all 4 types checked)
        return String(passwordChars).take(length)
    }
}
