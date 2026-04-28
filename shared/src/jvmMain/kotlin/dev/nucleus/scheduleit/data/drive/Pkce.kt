package dev.nucleus.scheduleit.data.drive

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

internal object Pkce {
    private val urlEncoder: Base64.Encoder = Base64.getUrlEncoder().withoutPadding()

    fun newVerifier(): String {
        val bytes = ByteArray(64).also { SecureRandom().nextBytes(it) }
        return urlEncoder.encodeToString(bytes)
    }

    fun challenge(verifier: String): String {
        val sha = MessageDigest.getInstance("SHA-256")
            .digest(verifier.toByteArray(Charsets.US_ASCII))
        return urlEncoder.encodeToString(sha)
    }
}
