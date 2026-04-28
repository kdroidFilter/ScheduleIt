package dev.nucleus.scheduleit.data.drive

import java.io.File
import java.security.SecureRandom
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class StoredTokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtEpochSec: Long,
    val email: String? = null,
    val backupFileId: String? = null,
    val lastBackupEpochSec: Long? = null,
)

internal class TokenStore(
    private val file: File = File(AppDataDir.resolve(), "drive-tokens.bin"),
    private val keyFile: File = File(AppDataDir.resolve(), "drive-tokens.key"),
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    fun load(): StoredTokens? {
        if (!file.exists() || !keyFile.exists()) return null
        return runCatching {
            val key = loadOrCreateKey()
            val blob = file.readBytes()
            val plain = decrypt(blob, key)
            json.decodeFromString(StoredTokens.serializer(), String(plain, Charsets.UTF_8))
        }.getOrNull()
    }

    fun save(tokens: StoredTokens) {
        val key = loadOrCreateKey()
        val plain = json.encodeToString(StoredTokens.serializer(), tokens).toByteArray(Charsets.UTF_8)
        file.writeBytes(encrypt(plain, key))
        runCatching { setOwnerOnly(file) }
    }

    fun clear() {
        runCatching { file.delete() }
        runCatching { keyFile.delete() }
    }

    private fun loadOrCreateKey(): SecretKey {
        if (keyFile.exists()) {
            val raw = Base64.getDecoder().decode(keyFile.readText().trim())
            return SecretKeySpec(raw, "AES")
        }
        val raw = ByteArray(32).also { SecureRandom().nextBytes(it) }
        keyFile.writeText(Base64.getEncoder().encodeToString(raw))
        runCatching { setOwnerOnly(keyFile) }
        return SecretKeySpec(raw, "AES")
    }

    private fun encrypt(plain: ByteArray, key: SecretKey): ByteArray {
        val iv = ByteArray(12).also { SecureRandom().nextBytes(it) }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key, GCMParameterSpec(128, iv))
        return iv + cipher.doFinal(plain)
    }

    private fun decrypt(blob: ByteArray, key: SecretKey): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(128, blob.copyOfRange(0, 12)))
        return cipher.doFinal(blob.copyOfRange(12, blob.size))
    }

    private fun setOwnerOnly(target: File) {
        target.setReadable(false, false)
        target.setReadable(true, true)
        target.setWritable(false, false)
        target.setWritable(true, true)
    }
}
