package com.example.passwordmanager.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.passwordmanager.data.Account
import com.example.passwordmanager.data.AccountType
import com.example.passwordmanager.ui.theme.PasswordManagerTheme

data class GridCardItem(
    val icon: ImageVector,
    val iconColor: Color,
    val title: String,
    val count: Int,
    val isCategory: Boolean = true
)

@Composable
fun DashboardScreen(navController: NavController, modifier: Modifier = Modifier, accounts: MutableList<Account> = mutableListOf()) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("createAccount") },
                containerColor = Color.Transparent,
                elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp),
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = "Add new password",
                    tint = Color(0xFF0A84FF),
                    modifier = Modifier.size(32.dp)
                )
            }
        },
        containerColor = Color.Black // iOS Dark Mode
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // Top Section: Title
            Text(
                text = "SecurityPass",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp
                ),
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp, top = 16.dp)
            )

            // Search Bar
            OutlinedTextField(
                value = "",
                onValueChange = { },
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
                    .padding(bottom = 16.dp)
            )

            val wifiCount = accounts.count { it.getType() == AccountType.WIFI && !it.getDeleted() }
            // Web: account web có mật khẩu (không đếm account chỉ-TOTP)
            val webCount = accounts.count { it.isWebAccount() && it.getPassword().isNotEmpty() && !it.getDeleted() }
            val appCount = accounts.count { it.isAppAccount() && it.getPassword().isNotEmpty() && !it.getDeleted() }
            // Codes: các account có TOTP secret (2FA)
            val codesCount = accounts.count { it.getTotpSecret().isNotEmpty() && !it.getDeleted() }
            // All: tất cả trừ account chỉ-TOTP (vì loại đó thuộc Codes)
            val allCount = accounts.count { !it.getDeleted() && !(it.getTotpSecret().isNotEmpty() && it.getPassword().isEmpty() && it.getSsid().isEmpty()) }
            val deletedCount = accounts.count { it.getDeleted() }

            // Grid Menu
            val cardItems = listOf(
                GridCardItem(Icons.Filled.Key, Color(0xFF0A84FF), "All", allCount),
                GridCardItem(Icons.Filled.Wifi, Color(0xFF32ADE6), "WLAN", wifiCount),
                GridCardItem(Icons.Filled.Language, Color(0xFFBF5AF2), "Web", webCount),
                GridCardItem(Icons.Filled.Apps, Color(0xFFFF453A), "App", appCount),
                GridCardItem(Icons.Filled.LockClock, Color(0xFFFFD60A), "Codes", codesCount),
                GridCardItem(Icons.Filled.Delete, Color(0xFFFF9F0A), "Deleted", deletedCount) 
            )

            // Dùng Column/Row thủ công để không bị scroll
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                for (i in cardItems.indices step 2) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            GridCard(item = cardItems[i], onClick = {
                                val routeName = when (cardItems[i].title) {
                                    "All" -> "All"
                                    "WLAN" -> "WLAN"
                                    "Web" -> "WEB"
                                    "App" -> "APP"
                                    "Deleted" -> "Deleted"
                                    else -> "Codes"
                                }
                                val safeName = java.net.URLEncoder.encode(routeName, "UTF-8")
                                navController.navigate("category/$safeName")
                            })
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            if (i + 1 < cardItems.size) {
                                GridCard(item = cardItems[i + 1], onClick = {
                                    val routeName = when (cardItems[i + 1].title) {
                                        "All" -> "All"
                                        "WLAN" -> "WLAN"
                                        "Web" -> "WEB"
                                        "App" -> "APP"
                                        "Deleted" -> "Deleted"
                                        else -> "Codes"
                                    }
                                    val safeName = java.net.URLEncoder.encode(routeName, "UTF-8")
                                    navController.navigate("category/$safeName")
                                })
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "SHARED GROUPS",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 6.dp, start = 16.dp)
            )
            
            // Shared Group Card
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Group,
                        contentDescription = "New Shared Group",
                        tint = Color(0xFF0A84FF),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "New Shared Group",
                        color = Color(0xFF0A84FF),
                        fontSize = 17.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun GridCard(item: GridCardItem, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(item.iconColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        item.icon,
                        contentDescription = item.title,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.count.toString(),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.Gray
                    )
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            
            Text(
                text = item.title,
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
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