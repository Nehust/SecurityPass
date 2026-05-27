# 🛡️ Project Summary: SecurePass

Tài liệu này là nguồn "Single Source of Truth" để theo dõi cấu trúc, tính năng, và trạng thái phát triển của dự án SecurePass. **File này phải được cập nhật ngay sau khi hoàn thành bất kỳ tính năng mới nào.**

## 1. 📌 Thông tin chung
- **Tên dự án:** SecurePass (Password Manager)
- **Ngôn ngữ:** Kotlin
- **UI Framework:** Jetpack Compose (Material Design 3)
- **Thiết bị Test thực tế:** Android Realme V15 5G
- **Thư viện chính:** Biometric, DataStore/SharedPreferences, Navigation Compose, Security-Crypto, GSON.

## 2. 🤖 Chỉ dẫn cho AI (AI Agent Instructions)
- **Tối ưu Token:** Tuyệt đối **BỎ QUA** việc đọc các file `.gradle`, `.idea`, hoặc thư mục `build` trừ khi gặp lỗi build cụ thể. Tập trung 100% vào code logic (Kotlin) và UI (Compose).
- **Cập nhật liên tục:** Bắt buộc cập nhật file `PROJECT_SUMMARY.md` này ngay sau khi code xong và test thành công một tính năng mới, nhằm giữ context cho các phiên làm việc sau.
- **Testing:** Người dùng sẽ chủ động test app trực tiếp trên máy thật (Realme V15 5G) thông qua ADB và cung cấp feedback nếu có lỗi. AI cần chờ feedback sau mỗi lần deploy tính năng lớn.

## 3. 📂 Cấu trúc thư mục (File Structure)
```text
app/src/main/java/com/example/passwordmanager/
├── MainActivity.kt                # Quản lý NavHost, Lifecycle (Auto-lock), và trạng thái khóa.
├── data/
│   ├── Account.kt                 # Data model: Account (Name, Password).
│   └── EncryptionHelper.kt        # Core: Mã hóa AES-256 GCM và lưu bằng EncryptedFile/Keystore.
├── ui/
│   ├── DashboardScreen.kt         # Màn hình chính hiển thị danh sách mật khẩu.
│   ├── CreateAccountScreen.kt     # Form thêm/sửa tài khoản & Nút Auto-Suggestion (Tạo Pass mạnh).
│   ├── LockScreen.kt              # Màn hình chờ quét sinh trắc học (Có tích hợp fallback nhập PIN máy).
│   ├── SettingsDialog.kt          # Dialog Cài đặt (Bảo mật, Xóa data, Import/Export).
│   ├── AccountItem.kt             # UI Item cho mỗi dòng tài khoản (có nút Ẩn/Hiện pass).
│   └── security/
│       └── SecurityManager.kt     # Lõi xử lý xác thực Biometric Class 3 (Vân tay/Khuôn mặt).
```

## 4. 🚀 Trạng thái tính năng (Feature Status)

### ✅ Đã hoàn thiện
1. **Xác thực sinh trắc học (Biometric & Device Credential):** Class 3 (Strong) bằng vân tay/khuôn mặt. Khi thất bại, tự động chuyển sang xác thực bằng Mật khẩu/PIN khóa màn hình của thiết bị.
2. **Lưu trữ bảo mật:** Mã hóa AES-256 GCM + Keystore (EncryptedFile).
3. **Quản lý Vòng đời (Auto-lock):** App tự khóa khi rơi vào trạng thái `ON_STOP`.
4. **Quản lý mật khẩu:** Thêm, sửa, ẩn/hiện mật khẩu.
5. **Thùng rác (Recycle Bin):** Tính năng Soft Delete (chuyển vào mục Deleted), hỗ trợ Khôi phục (Recover) hoặc Xóa vĩnh viễn (Delete Permanently).
6. **Phân loại Thông minh:** Tự động phân tách danh mục Web và App dựa trên Package Name hoặc URL Domain.
7. **Gợi ý mật khẩu mạnh (Auto-Suggestion):** Nút tạo pass ngẫu nhiên trong form thêm mới.
8. **Autofill Service (Native & Smart):** Tự động điền mật khẩu trên các app/web khác và gợi ý lưu mật khẩu mới. Tích hợp quét vân tay/khuôn mặt trước khi điền. Đã cải thiện:
   - **Phát hiện trường nâng cao:** Hỗ trợ phát hiện username/password field tốt hơn với từ khóa tiếng Việt và tiếng Anh.
   - **Khớp thông minh:** Ưu tiên theo package name, sau đó theo domain, cuối cùng theo thứ tự bảng chữ cái.
   - **Hiển thị nguồn gốc:** Giao diện hiển thị rõ nguồn gốc mật khẩu (App Package / Web Domain).
   - **Tự động ghi đè (Auto-Update):** Nếu đăng nhập vào cùng ứng dụng với tài khoản đã có, tự động ghi đè/cập nhật mật khẩu mới thay vì tạo bản sao rác.
   - **Hỗ trợ HTML:** Phát hiện tốt hơn các trường trong WebView với placeholder, name, id attributes.
   - **Quick Fill (Inline Autofill):** Tích hợp hoàn hảo với Gboard và các bàn phím hỗ trợ Android 11+ để hiển thị thẻ chip gợi ý trực tiếp trên bàn phím. Hỗ trợ Fallback kép.
9. **Kho lưu trữ Wi-Fi (Wifi & Network Vault):** Hỗ trợ lưu trữ SSID/Mật khẩu Wi-Fi. 
   - **Scan-to-Save:** Tích hợp Camera quét QR Code để tự động nhận diện và lưu mạng.
   - **Tự động kết nối:** Cho phép điện thoại kết nối thẳng vào mạng Wi-Fi từ bên trong app thông qua `WifiNetworkSuggestion`.

### ⏳ Đang phát triển / Chưa làm
1. **Chống chụp màn hình (Anti-Screenshot):** Đã có code `FLAG_SECURE` nhưng đang bị comment lại trong `MainActivity`.

## 5. 🔄 Luồng hoạt động (Core Flow)
- **Mở App:** `MainActivity` -> Kiểm tra `SecurityManager.isSecurityEnabled()` -> Gọi `BiometricPrompt` -> Thành công -> `DashboardScreen`.
- **Background/Foreground:** Ẩn app xuống nền -> `ON_STOP` -> `isUnlocked = false`. Mở lại app -> Yêu cầu quét lại sinh trắc.
- **Lưu/Đọc Data:** `Account` <-> GSON <-> AES-256 Encrypted String <-> `EncryptedFile`.

---
*Cập nhật lần cuối: Chuẩn bị phát triển tính năng Autofill*
