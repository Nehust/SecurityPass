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

    data class TotpData(
        val secret: String,
        val issuer: String,
        val accountName: String
    )

    fun parse(uriString: String): TotpData? {
        if (!uriString.startsWith("otpauth://totp/", ignoreCase = true)) {
            return null
        }
        return try {
            val uri = Uri.parse(uriString)
            val secret = uri.getQueryParameter("secret") ?: return null
            
            var issuer = uri.getQueryParameter("issuer") ?: ""
            var accountName = uri.path?.removePrefix("/") ?: ""

            // Handle format like /Issuer:AccountName
            if (accountName.contains(":")) {
                val parts = accountName.split(":", limit = 2)
                if (issuer.isEmpty()) {
                    issuer = parts[0].trim()
                }
                accountName = parts[1].trim()
            } else if (issuer.isEmpty()) {
                // Sometime accountName is just the issuer
                issuer = accountName
            }
            
            TotpData(secret, issuer, accountName)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
