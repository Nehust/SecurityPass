package com.example.passwordmanager.autofill

import android.app.PendingIntent
import android.content.Intent
import android.os.CancellationSignal
import android.service.autofill.*
import android.view.autofill.AutofillId
import android.widget.RemoteViews
import com.example.passwordmanager.R
import com.example.passwordmanager.data.Account
import com.example.passwordmanager.data.AccountType
import com.example.passwordmanager.data.EncryptionHelper
import android.util.Log

class SecureAutofillService : AutofillService() {

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        val structure = request.fillContexts.last().structure
        val packageName = structure.activityComponent.packageName
        Log.d("AutofillDebug", "=== onFillRequest TRIGGERED for package: $packageName ===")
        
        val parser = StructureParser(structure)
        parser.parse()

        val savedIds = mutableListOf<AutofillId>()
        if (parser.usernameId != null) savedIds.add(parser.usernameId!!)
        if (parser.passwordId != null) savedIds.add(parser.passwordId!!)

        if (savedIds.isEmpty()) {
            callback.onSuccess(null)
            return
        }

        val accounts = EncryptionHelper.loadAccounts(this)

        // Improved matching: prioritize by package name, then by domain
        val matchedAccounts = accounts.filter {
            it.getType() == AccountType.LOGIN
        }.sortedWith(compareByDescending<Account> { account ->
            // Exact package match gets highest priority
            account.getPackageName() == packageName
        }.thenByDescending { account ->
            // Domain match gets second priority
            val accountDomain = account.getDomain().lowercase()
            val webDomain = structure.activityComponent.className.lowercase()
            accountDomain.isNotEmpty() && webDomain.contains(accountDomain)
        }.thenBy { account ->
            // Alphabetical order as tiebreaker
            account.getName().lowercase()
        })

        val fillResponseBuilder = FillResponse.Builder()

        // Hỗ trợ lưu trên các trang nhiều bước (Multi-step login)
        // Chỉ trigger hỏi lưu khi Password bị ẩn đi (hoàn tất đăng nhập). 
        // Tránh hỏi lưu ngay sau khi nhập email ở trang 1.
        if (parser.passwordId != null) {
            val saveInfo = SaveInfo.Builder(
                SaveInfo.SAVE_DATA_TYPE_PASSWORD or SaveInfo.SAVE_DATA_TYPE_USERNAME,
                arrayOf(parser.passwordId!!)
            ).setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE).build()
            fillResponseBuilder.setSaveInfo(saveInfo)
        }

        if (matchedAccounts.isNotEmpty()) {
            for (account in matchedAccounts) {
                val presentation = RemoteViews(this.packageName, R.layout.autofill_item)
                presentation.setTextViewText(R.id.text_view, account.getName())

                val authIntent = Intent(this, AutofillAuthActivity::class.java).apply {
                    putExtra("EXTRA_USERNAME_ID", parser.usernameId)
                    putExtra("EXTRA_PASSWORD_ID", parser.passwordId)
                    putExtra("EXTRA_USERNAME_VALUE", account.getName())
                    putExtra("EXTRA_PASSWORD_VALUE", account.getPassword())
                }
                
                val pendingIntent = PendingIntent.getActivity(
                    this, 
                    account.hashCode(), 
                    authIntent, 
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

                val datasetBuilder = Dataset.Builder()
                if (parser.usernameId != null) {
                    datasetBuilder.setValue(parser.usernameId!!, null, presentation)
                }
                if (parser.passwordId != null) {
                    datasetBuilder.setValue(parser.passwordId!!, null, presentation)
                }
                datasetBuilder.setAuthentication(pendingIntent.intentSender)

                fillResponseBuilder.addDataset(datasetBuilder.build())
            }
        }

        callback.onSuccess(fillResponseBuilder.build())
    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        Log.d("AutofillDebug", "=== onSaveRequest TRIGGERED ===")
        var finalUsername = ""
        var finalPassword = ""
        var targetPackageName = ""

        for (context in request.fillContexts) {
            val structure = context.structure
            if (targetPackageName.isEmpty()) {
                targetPackageName = structure.activityComponent.packageName
            }
            val parser = StructureParser(structure)
            parser.parse()

            val uText = parser.usernameNode?.text?.toString()
            if (!uText.isNullOrEmpty()) finalUsername = uText

            val pText = parser.passwordNode?.text?.toString()
            if (!pText.isNullOrEmpty()) finalPassword = pText
        }

        if (finalUsername.isNotEmpty() && finalPassword.isNotEmpty()) {
            val account = Account(
                name = finalUsername,
                password = finalPassword,
                type = AccountType.LOGIN,
                packageName = targetPackageName
            )
            val accounts = EncryptionHelper.loadAccounts(this).toMutableList()
            accounts.add(account)
            EncryptionHelper.saveAccounts(this, accounts)
        }
        callback.onSuccess()
    }
}

class StructureParser(private val structure: android.app.assist.AssistStructure) {
    var usernameId: AutofillId? = null
    var passwordId: AutofillId? = null
    var usernameNode: android.app.assist.AssistStructure.ViewNode? = null
    var passwordNode: android.app.assist.AssistStructure.ViewNode? = null

    fun parse() {
        val windowCount = structure.windowNodeCount
        for (i in 0 until windowCount) {
            val windowNode = structure.getWindowNodeAt(i)
            traverseNode(windowNode.rootViewNode)
        }
    }

    private fun traverseNode(node: android.app.assist.AssistStructure.ViewNode?) {
        if (node == null) return

        var isUser = false
        var isPass = false

        // Check autofill hints first (highest priority)
        val hints = node.autofillHints
        if (hints != null) {
            for (hint in hints) {
                val h = hint.lowercase()
                if (h.contains("username") || h.contains("email") || h.contains("login") || h.contains("user") || h.contains("account")) {
                    isUser = true
                }
                if (h.contains("password") || h.contains("pass") || h.contains("passwd")) {
                    isPass = true
                }
            }
        }

        val className = node.className?.toString() ?: ""
        val viewId = node.idEntry?.lowercase() ?: ""
        val inputType = node.inputType
        val hintText = node.hint?.toString()?.lowercase() ?: ""
        val text = node.text?.toString()?.lowercase() ?: ""

        // In ra log để theo dõi cấu trúc WebView (chỉ in những view có khả năng là thẻ nhập liệu)
        if (className.contains("EditText") || className.contains("View") || inputType != 0 || hintText.isNotEmpty()) {
            Log.d("AutofillDebug", "Node: class=$className, id=$viewId, hint=$hintText, type=$inputType, text-len=${text.length}, htmlInfo=${node.htmlInfo != null}")
        }

        // Enhanced field detection for username/email
        if (!isUser && !isPass && (className.contains("EditText") || className.contains("TextInput") || className.contains("android.webkit.WebView") || className.contains("View") || className.contains("Button"))) {
            val userKeywords = listOf("user", "email", "login", "account", "tài khoản", "người dùng", "số điện thoại", "phone")
            val passKeywords = listOf("pass", "mật khẩu", "password", "passwd", "pin")

            // Check input type for password fields
            val baseInputType = inputType and 0xFFF
            if (userKeywords.any { viewId.contains(it) }) {
                isUser = true
            } else if (passKeywords.any { viewId.contains(it) }) {
                isPass = true
            }
            // Check hint text
            else if (userKeywords.any { hintText.contains(it) }) {
                isUser = true
            } else if (passKeywords.any { hintText.contains(it) }) {
                isPass = true
            }
            // Check input type (stripping flags like NO_SUGGESTIONS)
            else if (baseInputType == 129 || baseInputType == 225 || baseInputType == 18 || baseInputType == 145) {
                isPass = true
            }
            else if (baseInputType == 33) { // Email address type
                isUser = true
            }
            // Check if text contains @ (likely email field)
            else if (text.contains("@") && text.contains(".")) {
                isUser = true
            }
        }

        // Enhanced HTML field detection
        val htmlInfo = node.htmlInfo
        if (htmlInfo != null && !isUser && !isPass) {
            val type = htmlInfo.attributes?.firstOrNull { it.first.lowercase() == "type" }?.second?.lowercase()
            val name = htmlInfo.attributes?.firstOrNull { it.first.lowercase() == "name" }?.second?.lowercase()
            val id = htmlInfo.attributes?.firstOrNull { it.first.lowercase() == "id" }?.second?.lowercase()
            val placeholder = htmlInfo.attributes?.firstOrNull { it.first.lowercase() == "placeholder" }?.second?.lowercase()

            val userKeywords = listOf("user", "email", "login", "account")
            val passKeywords = listOf("pass", "password", "passwd")

            if (type == "email" || type == "text") {
                if (userKeywords.any { name?.contains(it) == true } ||
                    userKeywords.any { id?.contains(it) == true } ||
                    userKeywords.any { placeholder?.contains(it) == true }) {
                    isUser = true
                }
            }
            if (type == "password" ||
                passKeywords.any { name?.contains(it) == true } ||
                passKeywords.any { id?.contains(it) == true } ||
                passKeywords.any { placeholder?.contains(it) == true }) {
                isPass = true
                isUser = false
            }
        }

        // Assign IDs (first match wins)
        if (isUser && usernameId == null) {
            usernameId = node.autofillId
            usernameNode = node
        }
        if (isPass && passwordId == null) {
            passwordId = node.autofillId
            passwordNode = node
        }

        // Recursively traverse children
        val childCount = node.childCount
        for (i in 0 until childCount) {
            traverseNode(node.getChildAt(i))
        }
    }
}
