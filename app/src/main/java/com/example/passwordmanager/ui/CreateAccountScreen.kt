package com.example.passwordmanager.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.net.wifi.WifiManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
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

    val saveAction = {
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
    }

    val isEditMode = existingAccount != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) "Passwords" else "New Password",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        if (isEditMode) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xFF0A84FF))
                                Text("Back", color = Color(0xFF0A84FF), fontSize = 17.sp)
                            }
                        } else {
                            Text("Cancel", color = Color(0xFF0A84FF), fontSize = 17.sp)
                        }
                    }
                },
                actions = {
                    if (!isEditMode) {
                        TextButton(onClick = { saveAction() }) {
                            Text("Save", color = Color(0xFF0A84FF), fontWeight = FontWeight.Bold, fontSize = 17.sp)
                        }
                    } else {
                        TextButton(onClick = { saveAction() }) {
                            Text("Save", color = Color(0xFF0A84FF), fontSize = 17.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
        ) {
            // View Mode Header
            if (isEditMode) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF2C2C2E)),
                        contentAlignment = Alignment.Center
                    ) {
                        val firstLetter = if (accountType == AccountType.LOGIN) {
                            if (username.isNotEmpty()) username.substring(0, 1).uppercase() else "?"
                        } else {
                            if (ssid.isNotEmpty()) ssid.substring(0, 1).uppercase() else "?"
                        }
                        Text(firstLetter, fontSize = 40.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    if (username.contains("OpenAI", ignoreCase = true) || username.contains("bkict", ignoreCase = true)) {
                        Text(
                            "This password should be changed.",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "You're already using this password elsewhere.",
                            color = Color.Gray,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 32.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { /* Change password action */ },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0A84FF)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Change Password...")
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Hide", color = Color(0xFFFF453A), fontSize = 15.sp, modifier = Modifier.clickable { })
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } else {
                Spacer(modifier = Modifier.height(16.dp))
                // Tab Selection for New Password
                TabRow(
                    selectedTabIndex = if (accountType == AccountType.LOGIN) 0 else 1,
                    containerColor = Color.Transparent,
                    contentColor = Color.White,
                    divider = { }
                ) {
                    Tab(
                        selected = accountType == AccountType.LOGIN,
                        onClick = { accountType = AccountType.LOGIN },
                        text = { Text("Web/App", color = if (accountType == AccountType.LOGIN) Color(0xFF0A84FF) else Color.Gray) }
                    )
                    Tab(
                        selected = accountType == AccountType.WIFI,
                        onClick = { accountType = AccountType.WIFI },
                        text = { Text("Wi-Fi", color = if (accountType == AccountType.WIFI) Color(0xFF0A84FF) else Color.Gray) }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Form Fields Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Column {
                    if (!isEditMode) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Key, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            if (accountType == AccountType.LOGIN) {
                                OutlinedTextField(
                                    value = username,
                                    onValueChange = { username = it },
                                    placeholder = { Text("Website or Label", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            } else {
                                OutlinedTextField(
                                    value = ssid,
                                    onValueChange = { ssid = it },
                                    placeholder = { Text("Wi-Fi Name (SSID)", color = Color.Gray) },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedBorderColor = Color.Transparent,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    modifier = Modifier.weight(1f),
                                    singleLine = true
                                )
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 64.dp), color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                    }

                    // User Name Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (accountType == AccountType.LOGIN) "User Name" else "Security", color = Color.White, modifier = Modifier.width(100.dp))
                        if (accountType == AccountType.LOGIN) {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                placeholder = { Text("user", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.Gray,
                                    unfocusedTextColor = Color.Gray
                                ),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        } else {
                            OutlinedTextField(
                                value = securityType,
                                onValueChange = { securityType = it },
                                placeholder = { Text("WPA2", color = Color.Gray) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedBorderColor = Color.Transparent,
                                    unfocusedBorderColor = Color.Transparent,
                                    focusedTextColor = Color.Gray,
                                    unfocusedTextColor = Color.Gray
                                ),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = Color(0xFF3A3A3C), thickness = 0.5.dp)

                    // Password Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Password", color = Color.White, modifier = Modifier.width(100.dp))
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("password", color = Color.Gray) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle password",
                                        tint = Color.Gray
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = Color.Gray,
                                unfocusedTextColor = Color.Gray
                            ),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    if (accountType == AccountType.WIFI && !isEditMode) {
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { showQRScanner = true }.padding(16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Filled.QrCode, contentDescription = null, tint = Color(0xFF0A84FF))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan QR Code", color = Color(0xFF0A84FF))
                        }
                        HorizontalDivider(modifier = Modifier.padding(start = 16.dp), color = Color(0xFF3A3A3C), thickness = 0.5.dp)
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
                                    val results = wifiManager.scanResults
                                    availableWifiList = results.map { it.SSID }.filter { it.isNotEmpty() }.distinct()
                                    showWifiListDialog = true
                                } else {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }.padding(16.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("Find nearby networks", color = Color(0xFF0A84FF))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Notes Section
            Text(
                text = "NOTES",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp, start = 32.dp)
            )

            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = { },
                    placeholder = { Text("Add Notes", color = Color.Gray) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
            }
            
            if (accountType == AccountType.LOGIN && !isEditMode) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { password = generateStrongPassword() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(50.dp)
                ) {
                    Text("Generate Strong Password", color = Color(0xFF0A84FF), fontSize = 17.sp)
                }
            }

            if (isEditMode) {
                Spacer(modifier = Modifier.height(24.dp))
                var showDeleteDialog by remember { mutableStateOf(false) }
                var showPermanentDeleteDialog by remember { mutableStateOf(false) }

                val isDeleted = existingAccount?.getDeleted() == true

                if (isDeleted) {
                    // Recover Button
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(50.dp)
                            .clickable { 
                                existingAccount.setDeleted(false)
                                EncryptionHelper.saveAccounts(context, accounts)
                                navController.popBackStack()
                            }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Recover Password", color = Color(0xFF0A84FF), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Delete Permanently Button
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(50.dp)
                            .clickable { showPermanentDeleteDialog = true }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Delete Permanently", color = Color(0xFFFF453A), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                } else {
                    // Soft Delete Button
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(50.dp)
                            .clickable { showDeleteDialog = true }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Delete Password", color = Color(0xFFFF453A), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                if (showDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDialog = false },
                        title = { Text("Delete Password", color = Color.White) },
                        text = { Text("This password will be moved to the Deleted folder. You can recover it later.", color = Color.Gray) },
                        containerColor = Color(0xFF1C1C1E),
                        confirmButton = {
                            TextButton(onClick = {
                                showDeleteDialog = false
                                existingAccount?.setDeleted(true)
                                EncryptionHelper.saveAccounts(context, accounts)
                                navController.popBackStack()
                            }) {
                                Text("Delete", color = Color(0xFFFF453A), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDialog = false }) {
                                Text("Cancel", color = Color(0xFF0A84FF))
                            }
                        }
                    )
                }

                if (showPermanentDeleteDialog) {
                    AlertDialog(
                        onDismissRequest = { showPermanentDeleteDialog = false },
                        title = { Text("Delete Permanently", color = Color.White) },
                        text = { Text("Are you sure you want to permanently delete this password? This action cannot be undone.", color = Color.Gray) },
                        containerColor = Color(0xFF1C1C1E),
                        confirmButton = {
                            TextButton(onClick = {
                                showPermanentDeleteDialog = false
                                accounts.remove(existingAccount)
                                EncryptionHelper.saveAccounts(context, accounts)
                                navController.popBackStack()
                            }) {
                                Text("Delete", color = Color(0xFFFF453A), fontWeight = FontWeight.Bold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPermanentDeleteDialog = false }) {
                                Text("Cancel", color = Color(0xFF0A84FF))
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

        }
    }

    if (showWifiListDialog) {
        AlertDialog(
            onDismissRequest = { showWifiListDialog = false },
            title = { Text("Available Wi-Fi Networks", color = Color.White) },
            containerColor = Color(0xFF1C1C1E),
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    if (availableWifiList.isEmpty()) {
                        Text("No networks found. Ensure Location is enabled.", color = Color.Gray)
                    } else {
                        availableWifiList.forEach { network ->
                            TextButton(onClick = {
                                ssid = network
                                showWifiListDialog = false
                            }) {
                                Text(network, color = Color(0xFF0A84FF))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showWifiListDialog = false }) { Text("Close", color = Color(0xFF0A84FF)) }
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
