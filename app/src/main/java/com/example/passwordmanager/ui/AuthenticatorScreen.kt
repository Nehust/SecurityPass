package com.example.passwordmanager.ui

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.passwordmanager.data.Account
import com.example.passwordmanager.data.AccountType
import com.example.passwordmanager.data.EncryptionHelper
import com.example.passwordmanager.utils.OtpUriParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthenticatorScreen(
    navController: NavController,
    accounts: MutableList<Account>,
    onAuthenticate: (onSuccess: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var deletingAccount by remember { mutableStateOf<Account?>(null) }
    var showQRScanner by remember { mutableStateOf(false) }
    
    val codesAccounts = accounts.filter { it.getTotpSecret().isNotEmpty() && !it.getDeleted() }

    if (showQRScanner) {
        ModalBottomSheet(
            onDismissRequest = { showQRScanner = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = Modifier.fillMaxHeight(0.9f)
        ) {
            QRScannerScreen(
                onQRCodeScanned = { result ->
                    showQRScanner = false
                    try {
                        val parsed = OtpUriParser.parse(result)
                        if (parsed != null && parsed.secret.isNotEmpty()) {
                            val finalName = if (parsed.accountName.isNotEmpty()) {
                                parsed.accountName
                            } else if (parsed.issuer.isNotEmpty()) {
                                parsed.issuer
                            } else {
                                "Authenticator"
                            }
                            val newAccount = Account(
                                name = finalName,
                                type = AccountType.WEB,
                                totpSecret = parsed.secret
                            )
                            accounts.add(newAccount)
                            EncryptionHelper.saveAccounts(context, accounts)
                            Toast.makeText(context, "Đã thêm mã 2FA thành công!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Mã QR không chứa dữ liệu TOTP hợp lệ.", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Mã QR không hợp lệ: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                },
                onCancel = { showQRScanner = false }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { }, 
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color(0xFF0A84FF)
                            )
                            Text(text = "Passwords", color = Color(0xFF0A84FF), fontSize = 17.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            Text(
                text = "Authenticator",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp
                ),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Scan QR Button
            Button(
                onClick = { showQRScanner = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = "Scan", tint = Color(0xFF0A84FF))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Quét mã QR Thêm 2FA", color = Color(0xFF0A84FF), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (codesAccounts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Chưa có mã 2FA nào.\nBấm Quét mã QR ở trên để thêm.",
                        color = Color.Gray,
                        modifier = Modifier.padding(16.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(codesAccounts) { account ->
                        AccountItem(
                            account = account,
                            context = context,
                            onEdit = { accountToEdit ->
                                val index = accounts.indexOf(accountToEdit)
                                navController.navigate("createAccount/$index")
                            },
                            onDelete = { deletingAccount = it },
                            onAuthenticate = onAuthenticate,
                            isCodesView = true // Added parameter to hide username/password in Codes view
                        )
                    }
                }
            }
        }

        deletingAccount?.let { account ->
            DeleteAccountDialog(
                account = account,
                onDismiss = { deletingAccount = null },
                onDelete = {
                    accounts.remove(account)
                    deletingAccount = null
                    EncryptionHelper.saveAccounts(context, accounts)
                }
            )
        }
    }
}
