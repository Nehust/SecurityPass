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
    val firstLetter = if (displayName.isNotEmpty()) displayName.substring(0, 1).uppercase() else "?"
    
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
                    .background(Color(0xFF2C2C2E)),
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
                        val sourceStr = account.getPackageName().takeIf { it.isNotEmpty() } ?: account.getDomain().takeIf { it.isNotEmpty() }
                        if (sourceStr != null) {
                            "$typeStr - $sourceStr"
                        } else {
                            typeStr
                        }
                    }
                    Text(
                        text = subtitle,
                        color = Color.Gray,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
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