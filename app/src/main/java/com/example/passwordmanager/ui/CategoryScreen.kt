package com.example.passwordmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.passwordmanager.data.Account
import com.example.passwordmanager.data.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    navController: NavController,
    categoryName: String,
    accounts: MutableList<Account>,
    onAuthenticate: (onSuccess: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    var deletingAccount by remember { mutableStateOf<Account?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredAccounts = accounts.filter {
        val matchesCategory = when (categoryName.uppercase()) {
            "WLAN" -> it.getType() == AccountType.WIFI
            "WEB/APP" -> it.getType() == AccountType.LOGIN
            "ALL" -> true
            else -> false
        }
        val matchesSearch = it.getName().contains(searchQuery, ignoreCase = true) || it.getSsid().contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { }, // Đưa tiêu đề xuống dưới để làm chữ to giống iOS
                navigationIcon = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
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
            // Large Title
            Text(
                text = categoryName.replace("WEB/APP", "Security"), // Giả lập title như ảnh
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp
                ),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search", tint = Color.Gray) },
                trailingIcon = { Icon(Icons.Filled.Mic, contentDescription = "Voice search", tint = Color.Gray) },
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFF1C1C1E),
                    unfocusedContainerColor = Color(0xFF1C1C1E),
                    disabledContainerColor = Color(0xFF1C1C1E),
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredAccounts.isEmpty()) {
                Text(
                    text = "No accounts found.",
                    modifier = Modifier.padding(16.dp),
                    color = Color.Gray
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(filteredAccounts) { account ->
                        AccountItem(
                            account = account,
                            context = context,
                            onEdit = { accountToEdit ->
                                val index = accounts.indexOf(accountToEdit)
                                navController.navigate("createAccount/$index")
                            },
                            onDelete = { deletingAccount = it },
                            onAuthenticate = onAuthenticate
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
                    com.example.passwordmanager.data.EncryptionHelper.saveAccounts(context, accounts)
                }
            )
        }
    }
}
