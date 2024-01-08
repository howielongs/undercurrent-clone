package com.undercurrent.legacy.utils.encryption

import java.io.FileInputStream
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec


object JasyptEncryptUtils {
    private const val ALGORITHM = "AES"
    private var KEY: String = ""

    init {
        val properties = Properties()
        FileInputStream(System.getProperty("SECRET_key_path")).use { input ->
            properties.load(input)
        }
        KEY = properties.getProperty("SECRET.key", "")
    }

    fun encrypt(plaintext: String): String {
        val keySpec = SecretKeySpec(KEY.toByteArray(StandardCharsets.UTF_8), ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val encryptedBytes = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decrypt(ciphertext: String): String {
        val keySpec = SecretKeySpec(KEY.toByteArray(StandardCharsets.UTF_8), ALGORITHM)
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, keySpec)
        val decodedBytes = Base64.getDecoder().decode(ciphertext)
        val decryptedBytes = cipher.doFinal(decodedBytes)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }
}