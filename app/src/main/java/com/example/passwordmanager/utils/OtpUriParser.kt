package com.example.passwordmanager.utils

import android.net.Uri

object OtpUriParser {
    
    /**
     * Parse the scanned QR code URI and extract the TOTP secret key.
     * Returns null if the URI is invalid or doesn't contain a secret.
     */
    fun parseSecretFromUri(uriString: String): String? {
        if (!uriString.startsWith("otpauth://totp/", ignoreCase = true)) {
            return null
        }
        
        return try {
            val uri = Uri.parse(uriString)
            uri.getQueryParameter("secret")
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
