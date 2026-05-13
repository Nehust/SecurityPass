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
        val usernameValue = intent.getStringExtra("EXTRA_USERNAME_VALUE")
        val passwordValue = intent.getStringExtra("EXTRA_PASSWORD_VALUE")
        
        if (usernameId == null || passwordId == null || usernameValue == null || passwordValue == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        val securityManager = SecurityManager(this)
        if (securityManager.isSecurityEnabled()) {
            securityManager.authenticate(
                onSuccess = {
                    val dataset = Dataset.Builder()
                        .setValue(usernameId, AutofillValue.forText(usernameValue))
                        .setValue(passwordId, AutofillValue.forText(passwordValue))
                        .build()
                        
                    val result = Intent()
                    result.putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)
                    setResult(Activity.RESULT_OK, result)
                    finish()
                },
                onError = {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            )
        } else {
            val dataset = Dataset.Builder()
                .setValue(usernameId, AutofillValue.forText(usernameValue))
                .setValue(passwordId, AutofillValue.forText(passwordValue))
                .build()
                
            val result = Intent()
            result.putExtra(AutofillManager.EXTRA_AUTHENTICATION_RESULT, dataset)
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }
}
