package com.example.passwordmanager.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.passwordmanager.data.Account
import com.example.passwordmanager.data.AccountType
import com.example.passwordmanager.data.EncryptionHelper

@SuppressLint("ConfigurationScreenWidthHeight", "MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAccountScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    accounts: MutableList<Account>,
    existingAccount: Account? = null,
    context: Context
) {
    var accountType by remember { mutableStateOf(existingAccount?.getType() ?: AccountType.LOGIN) }
    var username by remember { mutableStateOf(existingAccount?.getName() ?: "") }
    var ssid by remember { mutableStateOf(existingAccount?.getSsid() ?: "") }
    var password by remember { mutableStateOf(existingAccount?.getPassword() ?: "") }
    var securityType by remember { mutableStateOf(existingAccount?.getSecurityType() ?: "WPA2") }
    
    var passwordVisible by remember { mutableStateOf(false) }
    var showQRScanner by remember { mutableStateOf(false) }
    var showWifiListDialog by remember { mutableStateOf(false) }
    var availableWifiList by remember { mutableStateOf<List<String>>(emptyList()) }

    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
            val results = wifiManager.scanResults
            availableWifiList = results.map { it.SSID }.filter { it.isNotEmpty() }.distinct()
            showWifiListDialog = true
        }
    }

    if (showQRScanner) {
        QRScannerScreen(
            onQRCodeScanned = { scannedSsid, scannedPass, scannedType ->
                ssid = scannedSsid
                password = scannedPass
                securityType = scannedType
                showQRScanner = false
            },
            onCancel = { showQRScanner = false }
        )
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        TopAppBar(
            title = {
                Text(
                    if (existingAccount != null) "Edit Account" else "Add Account Details",
                    fontSize = 20.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        // Tabs for Login or Wifi
        if (existingAccount == null) {
            TabRow(selectedTabIndex = if (accountType == AccountType.LOGIN) 0 else 1) {
                Tab(
                    selected = accountType == AccountType.LOGIN,
                    onClick = { accountType = AccountType.LOGIN },
                    text = { Text("Web/App") }
                )
                Tab(
                    selected = accountType == AccountType.WIFI,
                    onClick = { accountType = AccountType.WIFI },
                    text = { Text("Wi-Fi") }
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .weight(1f, fill = false)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            if (accountType == AccountType.LOGIN) {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username / Website") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            } else {
                OutlinedTextField(
                    value = ssid,
                    onValueChange = { ssid = it },
                    label = { Text("Wi-Fi Name (SSID)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(onClick = { showQRScanner = true }) {
                        Text("Quét QR Wi-Fi")
                    }
                    Button(onClick = {
                        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                            val results = wifiManager.scanResults
                            availableWifiList = results.map { it.SSID }.filter { it.isNotEmpty() }.distinct()
                            showWifiListDialog = true
                        } else {
                            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }) {
                        Text("Mạng gần đây")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility"
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (accountType == AccountType.LOGIN) {
                Button(
                    onClick = { password = generateStrongPassword() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Generate Strong Password")
                }
            } else {
                OutlinedTextField(
                    value = securityType,
                    onValueChange = { securityType = it },
                    label = { Text("Security Type (WPA2, WEP, NONE)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Button(
                onClick = {
                    keyboardController?.hide()
                    if (existingAccount != null) {
                        if (accountType == AccountType.LOGIN) existingAccount.setName(username) else existingAccount.setSsid(ssid)
                        existingAccount.setPassword(password)
                        existingAccount.setSecurityType(securityType)
                        existingAccount.setType(accountType)
                    } else {
                        val newAccount = Account().apply {
                            if (accountType == AccountType.LOGIN) setName(username) else setSsid(ssid)
                            setPassword(password)
                            setSecurityType(securityType)
                            setType(accountType)
                        }
                        if (newAccount.getName().isNotEmpty() || newAccount.getSsid().isNotEmpty()) {
                            accounts.add(newAccount)
                        }
                    }
                    EncryptionHelper.saveAccounts(context, accounts)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (existingAccount != null) "Save" else "Add")
            }
        }
    }

    if (showWifiListDialog) {
        AlertDialog(
            onDismissRequest = { showWifiListDialog = false },
            title = { Text("Available Wi-Fi Networks") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (availableWifiList.isEmpty()) {
                        Text("No networks found. Ensure Location is enabled.")
                    } else {
                        availableWifiList.forEach { network ->
                            TextButton(onClick = {
                                ssid = network
                                showWifiListDialog = false
                            }) {
                                Text(network)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWifiListDialog = false }) { Text("Close") }
            }
        )
    }
}

private fun generateStrongPassword(): String {
    val uppercase = ('A'..'Z').toList()
    val lowercase = ('a'..'z').toList()
    val digits = ('0'..'9').toList()
    val specials = listOf('!', '@', '#', '$', '%', '^', '&', '*', '(', ')')

    val passwordLength = 16

    return (1..passwordLength).map {
        when (it % 4) {
            0 -> uppercase.random()
            1 -> lowercase.random()
            2 -> digits.random()
            else -> specials.random()
        }
    }.shuffled().joinToString("")
}
