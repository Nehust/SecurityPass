package com.example.passwordmanager.ui

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.passwordmanager.data.Account
import com.example.passwordmanager.data.AccountType
import com.example.passwordmanager.data.WifiHelper
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun AccountItem(
    account: Account,
    context: Context,
    onEdit: (Account) -> Unit,
    onDelete: (Account) -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var isSwiped by remember { mutableStateOf(false) }
    var swipeDirection by remember { mutableStateOf("") }
    var swipeText by remember { mutableStateOf("") }
    var swipeTextAlignment by remember { mutableStateOf(Alignment.Center) }
    var swipeBackground by remember { mutableStateOf(Color.Transparent) }
    val swipeThreshold = 100.dp.value * context.resources.displayMetrics.density

    val passwordColor = if (isSystemInDarkTheme()) {
        Color(0xFF81D4FA)
    } else {
        Color(0xFF1565C0)
    }

    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val isWifi = account.getType() == AccountType.WIFI
    val displayName = if (isWifi) account.getSsid() else account.getName()

    Column {
        Layout(
            content = {
                if (isSwiped && swipeDirection == "right") {
                    swipeText = "Edit"
                    swipeBackground = passwordColor
                    swipeTextAlignment = Alignment.CenterStart
                }
                if (isSwiped && swipeDirection == "left") {
                    swipeText = "Delete"
                    swipeBackground = Color.Red
                    swipeTextAlignment = Alignment.CenterEnd
                }
                if (isSwiped) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(swipeBackground)
                    ) {
                        Text(
                            text = swipeText,
                            modifier = Modifier
                                .align(swipeTextAlignment)
                                .padding(start = 16.dp, end = 16.dp),
                            color = Color.White
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .offset { IntOffset(offsetX.roundToInt(), 0) }
                        .background(MaterialTheme.colorScheme.background)
                        .pointerInput(Unit) {
                            detectHorizontalDragGestures(
                                onDragEnd = {
                                    when {
                                        offsetX > swipeThreshold -> onEdit(account)
                                        offsetX < -swipeThreshold -> onDelete(account)
                                    }
                                    offsetX = 0f
                                    isSwiped = false
                                },
                                onHorizontalDrag = { _, dragAmount ->
                                    offsetX += dragAmount
                                    isSwiped = abs(offsetX) > 0
                                    swipeDirection = if (offsetX > 0) "right" else "left"
                                }
                            )
                        }
                        .combinedClickable(
                            onClick = { isPasswordVisible = !isPasswordVisible },
                            onLongClick = {
                                if (isPasswordVisible) {
                                    val clip = android.content.ClipData.newPlainText("Password", account.getPassword())
                                    clipboardManager.setPrimaryClip(clip)
                                    Toast.makeText(context, "Password copied to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                ) {
                    if (isSwiped) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Gray.copy(alpha = 0.2f))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (isWifi) Icons.Default.Wifi else Icons.Default.Language,
                                    contentDescription = null,
                                    tint = passwordColor,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = displayName,
                                    fontSize = 22.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = if (isPasswordVisible) account.getPassword() else "••••••••",
                                fontSize = 35.sp,
                                color = passwordColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        if (isWifi) {
                            Button(
                                onClick = {
                                    val success = WifiHelper.suggestNetwork(
                                        context,
                                        account.getSsid(),
                                        account.getPassword(),
                                        account.getSecurityType()
                                    )
                                    if (success) {
                                        Toast.makeText(context, "Đã gửi yêu cầu kết nối Wifi", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Thiết bị không hỗ trợ hoặc có lỗi xảy ra", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.padding(end = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = passwordColor)
                            ) {
                                Text("Connect")
                            }
                        }
                    }
                }
            }
        ) { measurable, constraints ->
            val placeable = measurable.map { it.measure(constraints) }
            layout(constraints.maxWidth, 80.dp.roundToPx()) {
                placeable.forEach { it.place(0, 0) }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp)
    }
}