# 🛡️ SecurePass - Advanced Project Blueprint

## 🎯 1. Mục tiêu dự án (Project Goals)
- **Bảo mật tuyệt đối:** Sử dụng Biometric Strong (Class 3) và Keystore System.
- **Tối ưu trải nghiệm (UX):** Giảm thiểu thao tác nhập liệu thủ công thông qua cơ chế Autofill.
- **Quản lý tập trung:** Trở thành kho lưu trữ duy nhất cho mọi loại thông tin bảo mật (Wifi, App, Web).

## 🚀 2. Danh sách tính năng (Expanded Feature Set)

### A. Nhóm tính năng cốt lõi (Đã có)
- **Biometric Auth:** Mở khóa app bằng vân tay/khuôn mặt.
- **Master Password Fallback:** Màn hình nhập pass dự phòng khi sinh trắc học lỗi.
- **Anti-Screenshot:** Chống chụp màn hình bằng `FLAG_SECURE`.
- **Lưu trữ mật khẩu:** Sử dụng các phương pháp lưu trữ hiện đại được cung cấp bởi Android.

### B. Nhóm tính năng tự động hóa (Mới)
- 🤖 **Credential Manager Integration:** Sử dụng API mới nhất của Android để thay thế Autofill Framework cũ, cho phép lưu và điền mật khẩu Google, App và Web.
- 📶 **Wifi & Network Vault:** Danh mục riêng để lưu thông tin Wi-Fi (SSID, Password, Security Type). *(Lưu ý: Do chính sách bảo mật Android, app chỉ lưu những gì người dùng nhập vào, không tự ý quét pass hệ thống).*
- 🪄 **Auto-Suggestion:** Đề xuất mật khẩu mạnh (Strong Password Generator) khi người dùng tạo tài khoản mới.
- ⚡ **Quick Fill Service:** Tự động nhận diện ô nhập liệu (Input Field) và hiển thị gợi ý ngay trên bàn phím.

## 🛠️ 3. Công nghệ sử dụng (Tech Stack)

| Thành phần | Công nghệ |
| :--- | :--- |
| **UI Framework** | Jetpack Compose (Material Design 3) |
| **Security** | Android Biometric API, AES-256 GCM, Keystore |
| **Automation** | Credential Manager API, Autofill Service API |
| **Storage** | Room Database (Encrypted), EncryptedSharedPreferences |
| **Architecture** | MVVM (Model-View-ViewModel) |

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