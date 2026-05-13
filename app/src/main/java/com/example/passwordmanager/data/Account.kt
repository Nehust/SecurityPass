package com.example.passwordmanager.data

import java.io.Serializable

enum class AccountType {
    LOGIN,
    WIFI
}

class Account(
    private var name: String = "Untitled",
    private var password: String = "",
    private var type: AccountType = AccountType.LOGIN,
    private var ssid: String = "",
    private var securityType: String = "WPA", // WPA, WEP, NONE
    private var packageName: String = "",
    private var domain: String = ""
) : Serializable {
    fun setName(name: String) { this.name = name }
    fun getName(): String = name

    fun setPassword(password: String) { this.password = password }
    fun getPassword(): String = password

    fun setType(type: AccountType) { this.type = type }
    fun getType(): AccountType = type

    fun setSsid(ssid: String) { this.ssid = ssid }
    fun getSsid(): String = ssid

    fun setSecurityType(securityType: String) { this.securityType = securityType }
    fun getSecurityType(): String = securityType

    fun setPackageName(packageName: String) { this.packageName = packageName }
    fun getPackageName(): String = packageName

    fun setDomain(domain: String) { this.domain = domain }
    fun getDomain(): String = domain
}