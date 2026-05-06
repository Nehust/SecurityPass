package com.example.passwordmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController // Import NavController
import androidx.navigation.compose.rememberNavController // Import for Preview
import com.example.passwordmanager.ui.theme.PasswordManagerTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.passwordmanager.data.Account

data class GridCardItem(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val count: Int
)

@Composable
fun DashboardScreen(navController: NavController, modifier: Modifier = Modifier, accounts: MutableList<Account> = mutableListOf()) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("createAccount") }, // Navigate to createAccount
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Filled.Add, "Add new password", tint = MaterialTheme.colorScheme.onPrimary)
            }
        },
        content = { paddingValues ->
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                // Top Section: Title
                Text(
                    text = "Passwords",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Search Bar
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* Handle search text change */ },
                    placeholder = { Text("Search") },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                    trailingIcon = { Icon(Icons.Filled.Mic, contentDescription = "Voice search") },
                    shape = RoundedCornerShape(20.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        disabledBorderColor = Color.Transparent,
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                )

                // Grid Menu
                val cardItems = listOf(
                    GridCardItem(Icons.Filled.Key, Color(0xFF007AFF), "All", 25),
                    GridCardItem(Icons.Filled.Person, Color(0xFF34C759), "Passkeys", 3),
                    GridCardItem(Icons.Filled.Lock, Color(0xFFFFCC00), "Codes", 1),
                    GridCardItem(Icons.Filled.Wifi, Color(0xFF32ADE6), "WLAN", 167),
                    GridCardItem(Icons.Filled.Warning, Color(0xFFFF3B30), "Security", 13),
                    GridCardItem(Icons.Filled.Delete, Color(0xFFFF9500), "Deleted", 0)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(cardItems) { item ->
                        GridCard(item = item)
                    }
                }

                // Accounts List Section
                Text(
                    text = "MY ACCOUNTS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(top = 16.dp, bottom = 12.dp)
                )

                val context = LocalContext.current
                var deletingAccount by remember { mutableStateOf<Account?>(null) }
                var searchQuery by remember { mutableStateOf("") } // Basic search state

                val filteredAccounts = accounts.filter {
                    it.getName().contains(searchQuery, ignoreCase = true) || 
                    it.getSsid().contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ) {
                    items(filteredAccounts) { account ->
                        AccountItem(
                            account = account,
                            context = context,
                            onEdit = { accountToEdit ->
                                val name = if (accountToEdit.getType() == com.example.passwordmanager.data.AccountType.WIFI) accountToEdit.getSsid() else accountToEdit.getName()
                                navController.navigate("createAccount/$name")
                            },
                            onDelete = { deletingAccount = it }
                        )
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
    )
}

@Composable
fun GridCard(item: GridCardItem) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(item.iconColor.copy(alpha = 0.2f)), // Lighter background for the icon circle
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    item.icon,
                    contentDescription = item.title,
                    tint = item.iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.count.toString(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "View ${item.title}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    PasswordManagerTheme(darkTheme = true) {
        DashboardScreen(rememberNavController())
    }
}