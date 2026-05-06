package com.example.passwordmanager.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    
    val filteredAccounts = accounts.filter {
        when (categoryName.uppercase()) {
            "WLAN" -> it.getType() == AccountType.WIFI
            "ALL" -> true
            else -> false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = categoryName, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (filteredAccounts.isEmpty()) {
                Text(
                    text = "No accounts found.",
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    items(filteredAccounts) { account ->
                        AccountItem(
                            account = account,
                            context = context,
                            onEdit = { accountToEdit ->
                                val name = if (accountToEdit.getType() == AccountType.WIFI) accountToEdit.getSsid() else accountToEdit.getName()
                                navController.navigate("createAccount/$name")
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
