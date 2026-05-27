package com.example.passwordmanager.autofill

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.service.autofill.Dataset
import android.view.autofill.AutofillId
import android.view.autofill.AutofillManager
import android.view.autofill.AutofillValue
import androidx.fragment.app.FragmentActivity
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
