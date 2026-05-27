package com.example.passwordmanager.utils

import org.apache.commons.codec.binary.Base32
import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object TotpGenerator {

    private const val HMAC_ALGO = "HmacSHA1"
    private const val TIME_STEP = 30L // 30 seconds
    private const val TOTP_LENGTH = 6

    /**
     * Sinh mã TOTP 6 số từ chuỗi Secret Key chuẩn Base32
     */
    fun generateTotp(secretBase32: String, timeMillis: Long = System.currentTimeMillis()): String {
        try {
            // Loại bỏ khoảng trắng nếu có
            val cleanSecret = secretBase32.replace(" ", "").uppercase()
            if (cleanSecret.isEmpty()) return "------"

            val base32 = Base32()
            val keyBytes = base32.decode(cleanSecret)

            val timeIndex = timeMillis / 1000 / TIME_STEP

            // Chuyển timeIndex thành mảng 8 bytes
            val timeBytes = ByteBuffer.allocate(8).putLong(timeIndex).array()

            // Tạo mã MAC
            val mac = Mac.getInstance(HMAC_ALGO)
            val keySpec = SecretKeySpec(keyBytes, HMAC_ALGO)
            mac.init(keySpec)
            val hash = mac.doFinal(timeBytes)

            // Trích xuất mã nhị phân động (Dynamic Truncation theo RFC 4226)
            val offset = hash[hash.size - 1].toInt() and 0x0F
            val binary = (hash[offset].toInt() and 0x7F shl 24) or
                    (hash[offset + 1].toInt() and 0xFF shl 16) or
                    (hash[offset + 2].toInt() and 0xFF shl 8) or
                    (hash[offset + 3].toInt() and 0xFF)

            val otp = binary % Math.pow(10.0, TOTP_LENGTH.toDouble()).toInt()

            return String.format("%0${TOTP_LENGTH}d", otp)
        } catch (e: Exception) {
            e.printStackTrace()
            return "ERROR "
        }
    }
}
