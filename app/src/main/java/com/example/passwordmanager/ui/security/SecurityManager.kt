package com.example.passwordmanager.ui.security

import android.content.Context
import android.content.SharedPreferences
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.util.concurrent.Executor

class SecurityManager(private val activity: FragmentActivity) {

    private val prefs: SharedPreferences = activity.getSharedPreferences("app_security", Context.MODE_PRIVATE)
    private val executor: Executor = ContextCompat.getMainExecutor(activity)

    fun isSecurityEnabled(): Boolean = prefs.getBoolean("security_enabled", false)

    fun setSecurityEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("security_enabled", enabled).apply()
    }

    fun isBiometricReady(): Boolean {
        val biometricManager = BiometricManager.from(activity)
        val authenticators = BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL

        val canAuthenticateResult = biometricManager.canAuthenticate(authenticators)

        return when (canAuthenticateResult) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                // Máy có phần cứng nhưng em chưa đăng ký vân tay/khuôn mặt trong cài đặt máy
                false
            }
            else -> false
        }
    }

    fun authenticate(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Mở khóa SecurePass")
            .setSubtitle("Xác nhận danh tính")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    fun setupSecurity(setupActivity: FragmentActivity, onComplete: () -> Unit) {
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Thiết lập bảo mật")
            .setSubtitle("Đăng ký sinh trắc học để bảo vệ mật khẩu")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                        BiometricManager.Authenticators.BIOMETRIC_WEAK or
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        val biometricPrompt = BiometricPrompt(setupActivity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    setSecurityEnabled(true)
                    onComplete()
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }
}