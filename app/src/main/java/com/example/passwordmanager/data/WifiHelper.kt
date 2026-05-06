package com.example.passwordmanager.data

import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build

object WifiHelper {
    fun suggestNetwork(context: Context, ssid: String, pass: String, securityType: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            val builder = WifiNetworkSuggestion.Builder().setSsid(ssid)

            if (securityType.uppercase() != "NONE") {
                builder.setWpa2Passphrase(pass)
            }

            val suggestion = builder.build()

            // Cố gắng xóa suggestion cũ nếu có (bỏ qua bước filter phức tạp để tránh lỗi SDK cũ)
            try {
                wifiManager.removeNetworkSuggestions(listOf(suggestion))
            } catch (e: Exception) {
                // Ignore
            }

            val status = wifiManager.addNetworkSuggestions(listOf(suggestion))
            return status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS
        }
        return false // Tính năng này yêu cầu Android 10 trở lên
    }
}
