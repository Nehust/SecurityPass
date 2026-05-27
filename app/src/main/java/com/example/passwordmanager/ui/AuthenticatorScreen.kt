package com.example.passwordmanager.ui

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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

    // Chỉ lọc các account có TOTP secret và chưa bị xóa
    val codesAccounts = accounts.filter { it.getTotpSecret().isNotEmpty() && !it.getDeleted() }

    // ── QR Scanner Bottom Sheet ───────────────────────────────────────────
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
                            val finalName = when {
                                parsed.issuer.isNotEmpty() && parsed.accountName.isNotEmpty() ->
                                    "${parsed.issuer} (${parsed.accountName})"
                                parsed.issuer.isNotEmpty() -> parsed.issuer
                                parsed.accountName.isNotEmpty() -> parsed.accountName
                                else -> "Authenticator"
                            }
                            val newAccount = Account(
                                name = finalName,
                                type = AccountType.WEB,
                                totpSecret = parsed.secret
                            )
                            accounts.add(newAccount)
                            EncryptionHelper.saveAccounts(context, accounts)
                            Toast.makeText(context, "Đã thêm 2FA: $finalName", Toast.LENGTH_SHORT).show()
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

    // ── Màn hình chính ────────────────────────────────────────────────────
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
                            Spacer(modifier = Modifier.width(4.dp))
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
            // ── Tiêu đề ────────────────────────────────────────────────
            Text(
                text = "Authenticator",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp
                ),
                color = Color.White,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // ── Mô tả ngắn ────────────────────────────────────────────
            Text(
                text = "Mã xác thực 2 yếu tố (TOTP) cho tài khoản của bạn",
                color = Color.Gray,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp)
            )

            // ── Nút quét QR ───────────────────────────────────────────
            Button(
                onClick = { showQRScanner = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.QrCodeScanner,
                    contentDescription = "Scan QR",
                    tint = Color(0xFF0A84FF),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Thêm tài khoản 2FA",
                    color = Color(0xFF0A84FF),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Danh sách 2FA ─────────────────────────────────────────
            if (codesAccounts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "🔐", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Chưa có mã 2FA nào",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Bấm \"Thêm tài khoản 2FA\" ở trên\nrồi quét mã QR từ ứng dụng để bắt đầu.",
                            color = Color.Gray,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                }
            } else {
                // Section header
                Text(
                    text = "${codesAccounts.size} TÀI KHOẢN",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                // Danh sách thẻ 2FA dạng card riêng biệt
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 20.dp)
                ) {
                    items(codesAccounts, key = { it.getName() + it.getTotpSecret() }) { account ->
                        TwoFactorCard(
                            account = account,
                            context = context,
                            onDelete = {
                                deletingAccount = account
                            }
                        )
                    }
                }
            }
        }

        // ── Dialog xóa ────────────────────────────────────────────────────
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

/**
 * Card chuyên dụng cho từng mã 2FA.
 * Thiết kế sạch sẽ: Avatar + Tên issuer + TOTP 6 số + đồng hồ + nút copy.
 */
@Composable
private fun TwoFactorCard(
    account: Account,
    context: android.content.Context,
    onDelete: () -> Unit
) {
    var totpCode by remember { mutableStateOf("") }
    var totpProgress by remember { mutableStateOf(1f) }
    var remainingSeconds by remember { mutableStateOf(30) }

    LaunchedEffect(account.getTotpSecret()) {
        while (true) {
            val time = System.currentTimeMillis()
            val step = 30_000L
            val remaining = step - (time % step)
            totpCode = com.example.passwordmanager.utils.TotpGenerator.generateTotp(account.getTotpSecret(), time)
            totpProgress = remaining.toFloat() / step.toFloat()
            remainingSeconds = (remaining / 1000).toInt() + 1
            kotlinx.coroutines.delay(100)
        }
    }

    val isUrgent = totpProgress < 0.17f
    val codeColor = if (isUrgent) Color(0xFFFF453A) else Color(0xFF30D158)
    val timerColor = if (isUrgent) Color(0xFFFF453A) else Color(0xFF30D158)

    // Avatar
    val cleanName = com.example.passwordmanager.utils.AvatarGenerator.extractCleanName(account.getName())
    val firstLetter = if (cleanName.isNotEmpty() && cleanName != "?") cleanName.substring(0, 1).uppercase() else "?"
    val avatarColors = listOf(
        0xFF3F51B5, 0xFF2196F3, 0xFF009688, 0xFF4CAF50,
        0xFFFF9800, 0xFFE91E63, 0xFF9C27B0, 0xFF673AB7
    )
    val avatarBg = Color(avatarColors[kotlin.math.abs(cleanName.hashCode()) % avatarColors.size])

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ── Avatar ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(avatarBg),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = firstLetter,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // ── Tên tài khoản ──────────────────────────────────────────
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.getName(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "2FA · TOTP",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // ── Mã TOTP + Đồng hồ + Copy ──────────────────────────────
            Column(horizontalAlignment = Alignment.End) {
                if (totpCode.length == 6) {
                    // Mã 6 số
                    Text(
                        text = "${totpCode.substring(0, 3)} ${totpCode.substring(3)}",
                        color = codeColor,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Đồng hồ đếm ngược nhỏ
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(22.dp)) {
                        CircularProgressIndicator(
                            progress = { totpProgress },
                            modifier = Modifier.fillMaxSize(),
                            color = timerColor,
                            strokeWidth = 2.5.dp,
                            trackColor = Color(0xFF3A3A3C)
                        )
                        Text(
                            text = remainingSeconds.toString(),
                            color = timerColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Nút Copy
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color(0xFF0A84FF),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE)
                                    as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("OTP", totpCode))
                                Toast.makeText(context, "Đã sao chép $totpCode", Toast.LENGTH_SHORT).show()
                            }
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    // Nút Xóa
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFF3A3A3C),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onDelete() }
                    )
                }
            }
        }
    }
}
