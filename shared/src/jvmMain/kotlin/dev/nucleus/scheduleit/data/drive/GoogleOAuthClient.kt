package dev.nucleus.scheduleit.data.drive

import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Parameters
import io.ktor.http.isSuccess
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class TokenResponse(
    val access_token: String,
    val expires_in: Long,
    val refresh_token: String? = null,
    val scope: String? = null,
    val token_type: String? = null,
    val id_token: String? = null,
)

@Serializable
internal data class UserInfo(
    val email: String? = null,
)

internal class GoogleOAuthClient(
    private val http: HttpClient,
    private val clientId: String,
    private val clientSecret: String,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    suspend fun exchangeCode(code: String, codeVerifier: String, redirectUri: String): TokenResponse {
        val response = http.submitForm(
            url = TOKEN_URL,
            formParameters = Parameters.build {
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("code", code)
                append("code_verifier", codeVerifier)
                append("grant_type", "authorization_code")
                append("redirect_uri", redirectUri)
            },
        )
        val text = response.bodyAsText()
        require(response.status.isSuccess()) { "Token exchange failed: ${response.status} $text" }
        return json.decodeFromString(TokenResponse.serializer(), text)
    }

    suspend fun refresh(refreshToken: String): TokenResponse {
        val response = http.submitForm(
            url = TOKEN_URL,
            formParameters = Parameters.build {
                append("client_id", clientId)
                append("client_secret", clientSecret)
                append("refresh_token", refreshToken)
                append("grant_type", "refresh_token")
            },
        )
        val text = response.bodyAsText()
        require(response.status.isSuccess()) { "Token refresh failed: ${response.status} $text" }
        return json.decodeFromString(TokenResponse.serializer(), text)
    }

    suspend fun fetchUserEmail(accessToken: String): String? = runCatching {
        val response = http.get(USERINFO_URL) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
        }
        if (!response.status.isSuccess()) return@runCatching null
        json.decodeFromString(UserInfo.serializer(), response.bodyAsText()).email
    }.getOrNull()

    companion object {
        const val TOKEN_URL = "https://oauth2.googleapis.com/token"
        const val USERINFO_URL = "https://openidconnect.googleapis.com/v1/userinfo"
    }
}
