# 🛡️ SecurePass - Advanced Project Blueprint

## 🎯 1. Mục tiêu dự án (Project Goals)
- **Bảo mật tuyệt đối:** Sử dụng Biometric Strong (Class 3) và Keystore System.
- **Tối ưu trải nghiệm (UX):** Giảm thiểu thao tác nhập liệu thủ công thông qua cơ chế Autofill.
- **Quản lý tập trung:** Trở thành kho lưu trữ duy nhất cho mọi loại thông tin bảo mật (Wifi, App, Web).

## 🚀 2. Danh sách tính năng (Expanded Feature Set)

### A. Nhóm tính năng cốt lõi (Đã có)
- **Biometric Auth:** Mở khóa app bằng vân tay/khuôn mặt.
- **Device Credential Fallback:** Sử dụng Mật khẩu/Mã PIN/Pattern khóa màn hình gốc của thiết bị khi sinh trắc học lỗi hoặc không khả dụng.
- **Anti-Screenshot:** Chống chụp màn hình bằng `FLAG_SECURE`.
- **Lưu trữ mật khẩu:** Sử dụng các phương pháp lưu trữ hiện đại được cung cấp bởi Android.
- 📶 **Wifi & Network Vault (Scan-to-Save):** Danh mục riêng lưu thông tin Wi-Fi. Tích hợp quét QR Code chia sẻ Wi-Fi (Google ML Kit) để tự động điền thông tin. Cung cấp tính năng kết nối mạng trực tiếp từ App thông qua `WifiNetworkSuggestion`.
- 🤖 **Smart Autofill Service:** Tự động điền mật khẩu nâng cao. Nhận diện Web/App thông minh thông qua cấu trúc WebView và Package Name. Tính năng tự động cập nhật/ghi đè mật khẩu rác.
- ⚡ **Quick Fill Service (Inline Autofill):** Tự động nhận diện ô nhập liệu (Input Field) và hiển thị thẻ chip gợi ý ngay trên thanh công cụ của bàn phím ảo (như Gboard).
- 🗑️ **Data Lifecycle Management:** Hệ thống Soft Delete, khôi phục hoặc xóa vĩnh viễn mật khẩu linh hoạt.

### B. Nhóm tính năng tự động hóa (Sắp tới)
- 🪄 **Auto-Suggestion:** Đề xuất mật khẩu mạnh (Strong Password Generator) khi người dùng tạo tài khoản mới.

## 🛠️ 3. Công nghệ sử dụng (Tech Stack)

| Thành phần | Công nghệ |
| :--- | :--- |
| **UI Framework** | Jetpack Compose (Material Design 3) |
| **Security** | Android Biometric API, AES-256 GCM, Keystore |
| **Automation** | Credential Manager API, Autofill Service API |
| **Storage** | Room Database (Encrypted), EncryptedSharedPreferences |
| **Architecture** | MVVM (Model-View-ViewModel) |
| **Computer Vision**| Google ML Kit (Barcode/Vision API) & CameraX |

## 🧠 4. Cấu trúc logic & Luồng dữ liệu (Logic Flow)

- **Cơ chế Autofill:**
  1. App đăng ký làm Autofill Service của hệ thống.
  2. Khi người dùng chạm vào ô mật khẩu ở app khác (ví dụ Facebook), hệ thống gửi yêu cầu đến SecurePass.
  3. SecurePass yêu cầu xác thực Biometric $\rightarrow$ Nếu thành công $\rightarrow$ Điền mật khẩu.
- **Quản lý trạng thái:**
  - `isUnlocked`: Quản lý bằng `rememberSaveable` tại `MainActivity`.
  - `LifecycleObserver`: Tự động khóa app khi ở trạng thái `ON_STOP`.

## 🤖 5. Chỉ dẫn dành cho AI Agent (Instruction for AI)

- **Context Reliance:** Sử dụng `PROJECT_BLUEPRINT.md` làm tiêu chuẩn đầu ra.
- **Code Focus:** Tập trung vào việc triển khai Credential Manager API và tối ưu lớp UI trong package `ui`.
- **Efficiency:** Không quét các file `.gradle`, `.idea` hoặc thư mục `build`.
- **Constraint:** Mọi chức năng lưu trữ mới phải thông qua lớp mã hóa `EncryptionHelper`.