# Project Summary: SecurePass (Password Manager)

Tài liệu này tổng hợp cấu trúc, tính năng và luồng hoạt động của dự án SecurePass để hỗ trợ việc phát triển và bảo trì.

## 1. Thông tin chung
- **Tên dự án:** SecurePass (Password Manager)
- **Ngôn ngữ:** Kotlin
- **UI Framework:** Jetpack Compose
- **Thư viện chính:** Biometric, DataStore/SharedPreferences, Navigation Compose, Security-Crypto, GSON.

## 2. Cấu trúc thư mục (File Structure)

```text
app/src/main/java/com/example/passwordmanager/
├── MainActivity.kt                # Điểm khởi đầu, quản lý NavHost và trạng thái khóa/mở khóa.
├── data/
│   ├── Account.kt                 # Data class đại diện cho một tài khoản (Name, User, Pass, Notes).
│   └── EncryptionHelper.kt        # Xử lý mã hóa và lưu trữ dữ liệu vào SharedPreferences.
├── ui/
│   ├── DashboardScreen.kt         # Màn hình chính danh sách tài khoản.
│   ├── CreateAccountScreen.kt     # Màn hình thêm mới hoặc chỉnh sửa tài khoản.
│   ├── LockScreen.kt              # Giao diện khi app bị khóa (chờ vân tay).
│   ├── PasswordFallbackScreen.kt  # Giao diện nhập Master Password khi không dùng sinh trắc học.
│   ├── SettingsDialog.kt          # (Cũ/Phụ) Dialog cài đặt.
│   ├── AccountItem.kt             # Thành phần UI hiển thị một dòng tài khoản.
│   ├── FloatingAddButton.kt       # Nút thêm nhanh.
│   ├── HomeScreen.kt              # (Có thể là file cũ hoặc đang tích hợp).
│   ├── theme/                     # Cấu hình màu sắc, kiểu chữ (Material3).
│   └── security/
│       └── SecurityManager.kt     # Quản lý Logic xác thực Biometric Prompt.
```

## 3. Các tính năng hiện tại (Features)

1.  **Xác thực sinh trắc học (Biometric):** 
    *   Tích hợp `BiometricPrompt` với tiêu chuẩn **Class 3 (Strong)**.
    *   Hỗ trợ cả Vân tay và Khuôn mặt (tùy theo phần cứng thiết bị).
    *   Xử lý phân loại lỗi: Phân biệt giữa người dùng hủy, nhập sai quá nhiều lần (Lockout), và chọn dùng mật khẩu dự phòng.
    *   Tối ưu trải nghiệm: Face Unlock không yêu cầu xác nhận thêm (Confirmation Required = false).
2.  **Mật khẩu dự phòng (Fallback):** 
    *   Cung cấp màn hình `PasswordFallbackScreen` khi sinh trắc học không khả dụng hoặc bị lỗi.
    *   (Lưu ý: Hiện tại đang dùng pass mặc định "1234", cần tích hợp vào EncryptedSharedPreferences ở bước tiếp theo).
3.  **Tự động khóa (Auto-lock):** Ứng dụng tự động khóa khi người dùng thoát ra màn hình Home hoặc chuyển sang ứng dụng khác (Lifecycle Observer).
4.  **Quản lý mật khẩu:**
    *   Thêm mới tài khoản.
    *   Sửa thông tin tài khoản hiện có.
    *   Xóa tài khoản.
    *   Ẩn/Hiện mật khẩu trong danh sách.
5.  **Bảo mật dữ liệu:** Dữ liệu được mã hóa trước khi lưu xuống bộ nhớ (thông qua `EncryptionHelper`).
6.  **Cài đặt:**
    *   Bật/Tắt bảo mật.
    *   Xóa toàn bộ dữ liệu.
    *   Import/Export dữ liệu (đang phát triển/cơ bản).
7.  **Chống chụp màn hình:** Hỗ trợ `FLAG_SECURE` để ngăn chụp hoặc quay màn hình (hiện đang comment trong `MainActivity`).

## 4. Luồng hoạt động chính (Flow)

### A. Luồng khởi động & Mở khóa:
1.  `MainActivity` kiểm tra `SecurityManager.isSecurityEnabled()`.
2.  Nếu **Bật**: Hiển thị `LockScreen` -> Tự động gọi `BiometricPrompt`.
    *   **Thành công:** `isUnlocked = true` -> Load dữ liệu -> Vào `DashboardScreen`.
    *   **Thất bại/Hủy:** Người dùng chọn "Sử dụng Master Password" -> Hiện `PasswordFallbackScreen`.
3.  Nếu **Tắt**: Vào thẳng `DashboardScreen`.

### B. Luồng bảo mật Lifecycle:
1.  Khi App rơi vào trạng thái `ON_STOP` (vào nền):
    *   Nếu bảo mật đang bật: Đặt `isUnlocked = false`.
2.  Khi App quay lại `ON_RESUME`:
    *   Nếu `isUnlocked == false`: Yêu cầu xác thực lại từ đầu.

### C. Luồng dữ liệu:
*   **Lưu:** `Account` -> GSON -> Encrypted String -> SharedPreferences.
*   **Đọc:** Encrypted String -> Decrypted String -> GSON -> `List<Account>`.

## 5. Quy ước lập trình
- Sử dụng `rememberSaveable` cho các trạng thái cần giữ lại khi xoay màn hình hoặc hệ thống tạm dừng.
- `SecurityManager` yêu cầu `FragmentActivity` để quản lý `BiometricPrompt`.
- UI tuân thủ Material Design 3.

---
*Cập nhật lần cuối: 24/05/2024*
