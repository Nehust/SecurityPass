package com.example.passwordmanager.autofill

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.service.autofill.Dataset
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import androidx.fragment.app.FragmentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import com.example.passwordmanager.ui.security.SecurityManager

class AutofillAuthActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val usernameId = intent.getParcelableExtra<AutofillId>("EXTRA_USERNAME_ID")
        val passwordId = intent.getParcelableExtra<AutofillId>("EXTRA_PASSWORD_ID")
        val totpId = intent.getParcelableExtra<AutofillId>("EXTRA_TOTP_ID")
        
        val usernameValue = intent.getStringExtra("EXTRA_USERNAME_VALUE")
        val passwordValue = intent.getStringExtra("EXTRA_PASSWORD_VALUE")
        val totpValue = intent.getStringExtra("EXTRA_TOTP_VALUE")
        
        if (usernameId == null && passwordId == null && totpId == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        val isGenerator = intent.getBooleanExtra("EXTRA_IS_GENERATOR", false)

        val fillDataset = {
            val builder = Dataset.Builder()
            if (usernameId != null && usernameValue != null) {
                builder.setValue(usernameId, AutofillValue.forText(usernameValue))
            }
            if (passwordId != null && passwordValue != null) {
                builder.setValue(passwordId, AutofillValue.forText(passwordValue))
            }
            if (totpId != null && totpValue != null) {
                builder.setValue(totpId, AutofillValue.forText(totpValue))
            }
            val result = Intent()
            result.putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, builder.build())
            setResult(Activity.RESULT_OK, result)
            finish()
        }

        if (isGenerator) {
            setContent {
                var showSheet by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(true) }
                if (showSheet) {
                    com.example.passwordmanager.ui.PasswordGeneratorBottomSheet(
                        onDismiss = {
                            showSheet = false
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                        },
                        onPasswordGenerated = { pwd ->
                            showSheet = false
                            val builder = Dataset.Builder()
                            if (passwordId != null) {
                                builder.setValue(passwordId, AutofillValue.forText(pwd))
                            }
                            if (usernameId != null && usernameValue != null) { // Retain username if passed
                                builder.setValue(usernameId, AutofillValue.forText(usernameValue))
                            }
                            val result = Intent()
                            result.putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, builder.build())
                            setResult(Activity.RESULT_OK, result)
                            finish()
                        }
                    )
                }
            }
            return
        }

        val securityManager = SecurityManager(this)
        if (securityManager.isSecurityEnabled()) {
            securityManager.authenticate(
                onSuccess = { fillDataset() },
                onError = {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            )
        } else {
            fillDataset()
        }
    }
}
