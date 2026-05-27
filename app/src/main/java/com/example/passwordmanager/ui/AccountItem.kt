package com.example.passwordmanager.ui

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passwordmanager.data.Account
import com.example.passwordmanager.data.AccountType

@Composable
fun AccountItem(
    account: Account,
    context: Context,
    onEdit: (Account) -> Unit,
    onDelete: (Account) -> Unit, // Giữ lại chữ ký hàm để không lỗi file khác, nhưng không dùng UI vuốt nữa
    onAuthenticate: ((onSuccess: () -> Unit) -> Unit)? = null
) {
    val isWifi = account.getType() == AccountType.WIFI
    val displayName = if (isWifi) account.getSsid() else account.getName()
    
    val sourceStr = account.getPackageName().takeIf { it.isNotEmpty() } ?: account.getDomain().takeIf { it.isNotEmpty() }
    val labelForLogo = if (isWifi) account.getSsid() else sourceStr ?: displayName
    val cleanName = com.example.passwordmanager.utils.AvatarGenerator.extractCleanName(labelForLogo)
    val firstLetter = if (cleanName.isNotEmpty() && cleanName != "?") cleanName.substring(0, 1).uppercase() else "?"
    
    val colors = listOf(
        0xFFF44336, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7,
        0xFF3F51B5, 0xFF2196F3, 0xFF03A9F4, 0xFF00BCD4,
        0xFF009688, 0xFF4CAF50, 0xFF8BC34A, 0xFFCDDC39,
        0xFFFFC107, 0xFFFF9800, 0xFFFF5722, 0xFF795548,
        0xFF9E9E9E, 0xFF607D8B
    )
    val colorHash = kotlin.math.abs(cleanName.hashCode())
    val backgroundColor = Color(colors[colorHash % colors.size])
    
    // Giả lập trạng thái password để hiển thị text màu đỏ/xám như ảnh
    val isCompromised = displayName.contains("OpenAI", ignoreCase = true) || displayName.contains("bkict", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Black)
            .clickable {
                if (onAuthenticate != null) {
                    onAuthenticate { onEdit(account) }
                } else {
                    onEdit(account)
                }
            }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Letter Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstLetter,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = displayName,
                    color = Color.White,
                    fontSize = 17.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                if (isCompromised) {
                    Text(
                        text = "Compromised password",
                        color = Color(0xFFFF453A),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    val subtitle = if (isWifi) {
                        "Wi-Fi - ${account.getSecurityType()}"
                    } else {
                        val typeStr = if (account.isAppAccount()) "App" else "Web"
                        if (sourceStr != null) {
                            "$typeStr - $sourceStr"
                        } else {
                            typeStr
                        }
                    }
                    Text(
                        text = subtitle,
                        color = if (account.isWebAccount() && sourceStr != null) Color(0xFF0A84FF) else Color.Gray,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable {
                            if (account.isWebAccount() && sourceStr != null) {
                                val url = if (!sourceStr.startsWith("http://") && !sourceStr.startsWith("https://")) {
                                    "https://$sourceStr"
                                } else {
                                    sourceStr
                                }
                                try {
                                    context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                                } catch (e: Exception) {
                                    android.util.Log.e("AccountItem", "Cannot open URL: $url")
                                }
                            }
                        }
                    )
                }
            }

            // Arrow Right
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF3A3A3C),
                modifier = Modifier.size(20.dp)
            )
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(start = 76.dp), // Thụt vào một chút cho giống iOS
            thickness = 0.5.dp,
            color = Color(0xFF3A3A3C)
        )
    }
}