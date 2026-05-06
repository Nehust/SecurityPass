package com.example.passwordmanager

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.passwordmanager.data.Account
import com.example.passwordmanager.data.EncryptionHelper
import com.example.passwordmanager.ui.*
import com.example.passwordmanager.ui.security.SecurityManager
import com.example.passwordmanager.ui.theme.PasswordManagerTheme
import kotlinx.coroutines.delay

class MainActivity : FragmentActivity() {
    private lateinit var securityManager: SecurityManager

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        // 🛡️ 1. CHỐNG CHỤP MÀN HÌNH (FLAG_SECURE)
//        window.setFlags(
//            WindowManager.LayoutParams.FLAG_SECURE,
//            WindowManager.LayoutParams.FLAG_SECURE
//        )

        super.onCreate(savedInstanceState)
        securityManager = SecurityManager(this)
        enableEdgeToEdge()

        setContent {
            PasswordManagerTheme {
                val context = LocalContext.current
                val navController = rememberNavController()
                val lifecycleOwner = LocalLifecycleOwner.current

                // -----------------------------------------------------------
                // 2. KHAI BÁO STATE (Sử dụng rememberSaveable để không bị reset khi hiện vân tay)
                // -----------------------------------------------------------
                val accounts = remember { mutableStateListOf<Account>() }
                var isUnlocked by rememberSaveable { mutableStateOf(!securityManager.isSecurityEnabled()) }
                var isDataLoaded by remember { mutableStateOf(false) }

                // -----------------------------------------------------------
                // 3. XỬ LÝ LIFECYCLE (Tự động khóa app khi ẩn xuống nền)
                // -----------------------------------------------------------
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_STOP) {
                            if (securityManager.isSecurityEnabled()) {
                                isUnlocked = false
                                isDataLoaded = false
                            }
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                // -----------------------------------------------------------
                // 4. SIDE EFFECTS (Tự động kích hoạt quét sinh trắc học)
                // -----------------------------------------------------------

                // Tự động gọi quét sinh trắc học hoặc nhập PIN
                LaunchedEffect(isUnlocked) {
                    if (!isUnlocked && securityManager.isSecurityEnabled()) {
                        delay(500) // Tăng delay một chút để UI ổn định hoàn toàn
                        securityManager.authenticate(
                            onSuccess = { isUnlocked = true },
                            onError = { error ->
                                if (!error.contains("hủy", ignoreCase = true)) {
                                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }

                // Load dữ liệu khi đã mở khóa thành công
                LaunchedEffect(isUnlocked) {
                    if (isUnlocked && !isDataLoaded) {
                        val loadedAccounts = EncryptionHelper.loadAccounts(this@MainActivity)
                        accounts.clear()
                        accounts.addAll(loadedAccounts)
                        isDataLoaded = true
                    }
                }

                // Thiết lập bảo mật lần đầu nếu máy hỗ trợ mà app chưa bật
                LaunchedEffect(Unit) {
                    val securityEnabled = securityManager.isSecurityEnabled()
                    val biometricReady = securityManager.isBiometricReady()
                    println("MainActivity: Security Enabled = $securityEnabled, Biometric Ready = $biometricReady")

                    if (!securityEnabled && biometricReady) {
                        securityManager.setupSecurity(this@MainActivity) {
                            if (securityManager.isSecurityEnabled()) isUnlocked = false
                        }
                    }
                }

                // -----------------------------------------------------------
                // 5. VẼ GIAO DIỆN (UI RENDERING)
                // -----------------------------------------------------------
                if (!isUnlocked && securityManager.isSecurityEnabled()) {
                    // Màn hình Khóa
                    LockScreen(onUnlockRequested = {
                        securityManager.authenticate(
                            onSuccess = { isUnlocked = true },
                            onError = { }
                        )
                    })
                } else {
                    // Màn hình chính sau khi mở khóa
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        contentWindowInsets = WindowInsets(0, 0, 0, 0)
                    ) {
                        NavHost(navController, startDestination = "home") {
                            composable("home") {
                                DashboardScreen(navController = navController, accounts = accounts)
                            }

                            composable("category/{categoryName}") { backStackEntry ->
                                val categoryName = backStackEntry.arguments?.getString("categoryName") ?: "All"
                                CategoryScreen(
                                    navController = navController,
                                    categoryName = categoryName,
                                    accounts = accounts,
                                    onAuthenticate = { onSuccess ->
                                        securityManager.authenticate(
                                            onSuccess = onSuccess,
                                            onError = { error ->
                                                if (!error.contains("hủy", ignoreCase = true)) {
                                                    Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        )
                                    }
                                )
                            }

                            composable("createAccount") {
                                CreateAccountScreen(
                                    navController = navController,
                                    modifier = Modifier,
                                    accounts = accounts,
                                    existingAccount = null,
                                    context = this@MainActivity
                                )
                            }

                            composable("createAccount/{accountName}") { backStackEntry ->
                                val accountName = backStackEntry.arguments?.getString("accountName")
                                val account = accounts.find { it.getName() == accountName }
                                CreateAccountScreen(
                                    navController = navController,
                                    modifier = Modifier,
                                    accounts = accounts,
                                    existingAccount = account,
                                    context = this@MainActivity
                                )
                            }

                            composable("settings") {
                                SettingsScreen(
                                    navController = navController,
                                    securityManager = securityManager,
                                    onSecurityChanged = { enabled -> isUnlocked = !enabled },
                                    onImport = { imported ->
                                        accounts.clear()
                                        accounts.addAll(imported)
                                        EncryptionHelper.saveAccounts(this@MainActivity, accounts)
                                    },
                                    onExport = { accounts.toList() },
                                    onDeleteAll = {
                                        accounts.clear()
                                        EncryptionHelper.saveAccounts(this@MainActivity, accounts)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}