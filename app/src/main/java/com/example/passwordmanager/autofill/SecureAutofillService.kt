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

        val parsedWebDomain = parser.webDomain.lowercase()
        val isBrowser = packageName.contains("chrome") || packageName.contains("browser")
        
        val matchedAccounts = accounts.filter { account ->
            if (account.getDeleted()) return@filter false
            if (!account.isWebAccount() && !account.isAppAccount()) return@filter false

            val accountDomain = account.getDomain().lowercase()
            
            if (parsedWebDomain.isNotEmpty()) {
                // Đang lướt web: CHỈ hiển thị tài khoản có domain khớp với trang web hiện tại
                accountDomain.isNotEmpty() && parsedWebDomain.contains(accountDomain)
            } else if (isBrowser) {
                // Ở trong trình duyệt nhưng không ở trang web cụ thể nào (ví dụ trang Cài đặt của Chrome)
                false
            } else {
                // Đang dùng App: CHỈ hiển thị tài khoản của đúng App đó
                account.getPackageName() == packageName
            }
        }.sortedBy { it.getName().lowercase() }

        val fillResponseBuilder = FillResponse.Builder()

        // Kiểm tra Inline Suggestions (Android 11+)
        var inlineRequest: android.view.inputmethod.InlineSuggestionsRequest? = null
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            inlineRequest = request.inlineSuggestionsRequest
        }

        // Tối ưu hộp thoại Lưu mật khẩu
        if (parser.passwordId != null) {
            val builder = SaveInfo.Builder(
                SaveInfo.SAVE_DATA_TYPE_PASSWORD or SaveInfo.SAVE_DATA_TYPE_USERNAME,
                arrayOf(parser.passwordId!!)
            )
            
            if (parser.usernameId != null) {
                builder.setOptionalIds(arrayOf(parser.usernameId!!))
            }

            val domainOrApp = if (parsedWebDomain.isNotEmpty()) parsedWebDomain else packageName
            builder.setDescription("Lưu tài khoản cho: $domainOrApp")

            // Bí quyết: Với App, không dùng cờ INVISIBLE. Hộp thoại CHỈ hiện khi Activity Login đóng (đăng nhập thành công).
            // Với Web (Chrome), Activity không đóng nên BẮT BUỘC phải dùng cờ INVISIBLE (mọi view biến mất).
            if (parsedWebDomain.isNotEmpty()) {
                builder.setFlags(SaveInfo.FLAG_SAVE_ON_ALL_VIEWS_INVISIBLE)
            }

            fillResponseBuilder.setSaveInfo(builder.build())
        }

        if (matchedAccounts.isNotEmpty()) {
            for (account in matchedAccounts) {
                val labelForLogo = if (account.isWebAccount() && account.getDomain().isNotEmpty()) account.getDomain() else account.getPackageName()
                val avatarBitmap = com.example.passwordmanager.utils.AvatarGenerator.generateAvatarBitmap(labelForLogo)
                val avatarIcon = com.example.passwordmanager.utils.AvatarGenerator.generateAvatarIcon(labelForLogo)

                val presentation = RemoteViews(this.packageName, R.layout.autofill_item)
                presentation.setTextViewText(R.id.text_view, account.getName())
                presentation.setImageViewBitmap(R.id.logo_view, avatarBitmap)

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

                // Khởi tạo InlinePresentation nếu bàn phím hỗ trợ
                var inlinePresentation: InlinePresentation? = null
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && inlineRequest != null && inlineRequest.inlinePresentationSpecs.isNotEmpty()) {
                    val spec = inlineRequest.inlinePresentationSpecs.first()
                    try {
                        val slice = androidx.autofill.inline.v1.InlineSuggestionUi.newContentBuilder(pendingIntent)
                            .setTitle(account.getName())
                            .setSubtitle(if (account.isAppAccount()) "App" else "Web")
                            .setStartIcon(avatarIcon)
                            .build()
                            .slice
                        inlinePresentation = InlinePresentation(slice, spec, false)
                    } catch (e: Exception) {
                        Log.e("AutofillDebug", "Lỗi tạo InlinePresentation: ${e.message}")
                    }
                }

                val datasetBuilder = Dataset.Builder()
                if (parser.usernameId != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && inlinePresentation != null) {
                        datasetBuilder.setValue(parser.usernameId!!, null, presentation, inlinePresentation)
                    } else {
                        datasetBuilder.setValue(parser.usernameId!!, null, presentation)
                    }
                }
                if (parser.passwordId != null) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R && inlinePresentation != null) {
                        datasetBuilder.setValue(parser.passwordId!!, null, presentation, inlinePresentation)
                    } else {
                        datasetBuilder.setValue(parser.passwordId!!, null, presentation)
                    }
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
        var targetWebDomain = ""

        for (context in request.fillContexts) {
            val structure = context.structure
            if (targetPackageName.isEmpty()) {
                targetPackageName = structure.activityComponent.packageName
            }
            val parser = StructureParser(structure)
            parser.parse()

            if (parser.webDomain.isNotEmpty()) {
                targetWebDomain = parser.webDomain
            }

            val uText = parser.usernameNode?.text?.toString()
            if (!uText.isNullOrEmpty()) finalUsername = uText

            val pText = parser.passwordNode?.text?.toString()
            if (!pText.isNullOrEmpty()) finalPassword = pText
        }

        if (finalUsername.isNotEmpty() && finalPassword.isNotEmpty()) {
            val isApp = targetWebDomain.isEmpty() && targetPackageName.isNotEmpty() && !targetPackageName.contains("chrome") && !targetPackageName.contains("browser")
            val accounts = EncryptionHelper.loadAccounts(this).toMutableList()

            // Logic tìm tài khoản trùng lặp: Nếu là App thì khớp PackageName, nếu là Web thì khớp Domain
            val existingAccount = accounts.find { 
                it.getName() == finalUsername && 
                !it.getDeleted() &&
                ((isApp && it.getPackageName() == targetPackageName) || (!isApp && it.getDomain() == targetWebDomain && targetWebDomain.isNotEmpty()))
            }

            if (existingAccount != null) {
                existingAccount.setPassword(finalPassword)
                existingAccount.setType(if (isApp) AccountType.APP else AccountType.WEB)
                if (!isApp) existingAccount.setDomain(targetWebDomain)
            } else {
                val account = Account(
                    name = finalUsername,
                    password = finalPassword,
                    type = if (isApp) AccountType.APP else AccountType.WEB,
                    packageName = targetPackageName,
                    domain = targetWebDomain
                )
                accounts.add(account)
            }
            
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
    var webDomain: String = ""

    // Danh sách lưu các ô nhập liệu (để dự đoán theo vị trí)
    private val textFields = mutableListOf<android.app.assist.AssistStructure.ViewNode>()

    fun parse() {
        val windowCount = structure.windowNodeCount
        for (i in 0 until windowCount) {
            val windowNode = structure.getWindowNodeAt(i)
            traverseNode(windowNode.rootViewNode)
        }

        // --- THUẬT TOÁN NỘI SUY DỰA TRÊN VỊ TRÍ ---
        // Nếu đã tìm thấy tài khoản, nhưng không tìm thấy password
        if (passwordId == null && usernameId != null) {
            val uIndex = textFields.indexOf(usernameNode)
            // Lấy ô nhập liệu kế tiếp ngay sau ô tài khoản
            if (uIndex != -1 && uIndex + 1 < textFields.size) {
                val nextNode = textFields[uIndex + 1]
                passwordId = nextNode.autofillId
                passwordNode = nextNode
                Log.d("AutofillDebug", "Heuristics: Đoán ô thứ ${uIndex+2} là Mật khẩu vì nằm ngay sau Tài khoản.")
            }
        }
        
        // Nếu chỉ có đúng 2 ô nhập liệu, mặc định ô 1 là User, ô 2 là Pass
        if (usernameId == null && passwordId == null && textFields.size >= 2) {
            usernameId = textFields[0].autofillId
            usernameNode = textFields[0]
            passwordId = textFields[1].autofillId
            passwordNode = textFields[1]
            Log.d("AutofillDebug", "Heuristics: Chỉ có các ô nhập liệu chung chung, gán 2 ô đầu tiên làm User/Pass.")
        }
    }

    private fun traverseNode(node: android.app.assist.AssistStructure.ViewNode?) {
        if (node == null) return

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            val domain = node.webDomain?.toString()
            if (!domain.isNullOrEmpty()) {
                webDomain = domain
            }
        }

        var isUser = false
        var isPass = false

        // 1. Phân tích Autofill Hints (Mạnh nhất)
        val hints = node.autofillHints
        if (hints != null) {
            for (hint in hints) {
                val h = hint.lowercase()
                if (h.contains("username") || h.contains("email") || h.contains("login") || h.contains("user") || h.contains("account")) {
                    isUser = true
                }
                if (h.contains("password") || h.contains("pass") || h.contains("passwd") || h.contains("current-password")) {
                    isPass = true
                }
            }
        }

        val className = node.className?.toString() ?: ""
        val viewId = node.idEntry?.lowercase() ?: ""
        val inputType = node.inputType
        val baseInputType = inputType and 0xFFF
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

        // Nếu là ô nhập liệu, đưa vào mảng để nội suy sau này
        if (className.contains("EditText") || className.contains("TextInput") || baseInputType > 0) {
            textFields.add(node)
        }

        // 3. Phân tích mã HTML5 (WebView / Chrome)
        val htmlInfo = node.htmlInfo
        if (htmlInfo != null && !isUser && !isPass) {
            val type = htmlInfo.attributes?.firstOrNull { it.first.lowercase() == "type" }?.second?.lowercase()
            val name = htmlInfo.attributes?.firstOrNull { it.first.lowercase() == "name" }?.second?.lowercase()
            val id = htmlInfo.attributes?.firstOrNull { it.first.lowercase() == "id" }?.second?.lowercase()
            val placeholder = htmlInfo.attributes?.firstOrNull { it.first.lowercase() == "placeholder" }?.second?.lowercase()
            val autocomplete = htmlInfo.attributes?.firstOrNull { it.first.lowercase() == "autocomplete" }?.second?.lowercase()

            val userKeywords = listOf("user", "email", "login", "account", "phone")
            val passKeywords = listOf("pass", "password", "passwd", "current-password")

            if (type == "email" || type == "text" || type == "tel") {
                if (userKeywords.any { name?.contains(it) == true } ||
                    userKeywords.any { id?.contains(it) == true } ||
                    userKeywords.any { placeholder?.contains(it) == true } ||
                    userKeywords.any { autocomplete?.contains(it) == true }) {
                    isUser = true
                }
            }
            if (type == "password" ||
                passKeywords.any { name?.contains(it) == true } ||
                passKeywords.any { id?.contains(it) == true } ||
                passKeywords.any { placeholder?.contains(it) == true } ||
                passKeywords.any { autocomplete?.contains(it) == true }) {
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
