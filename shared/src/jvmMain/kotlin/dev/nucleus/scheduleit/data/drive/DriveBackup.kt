package dev.nucleus.scheduleit.data.drive

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal class DriveBackup(
    private val http: HttpClient,
    private val json: Json = Json { ignoreUnknownKeys = true },
) {
    /**
     * First upload: multipart create with metadata + content in one request.
     * Returns the new file id.
     */
    suspend fun create(accessToken: String, fileName: String, content: ByteArray): String {
        val boundary = "==BACKUP_BOUNDARY_${System.nanoTime()}=="
        val metadata = """{"name":"$fileName","mimeType":"application/json","parents":["appDataFolder"]}"""
        val prefix = buildString {
            append("--$boundary\r\n")
            append("Content-Type: application/json; charset=UTF-8\r\n\r\n")
            append(metadata).append("\r\n")
            append("--$boundary\r\n")
            append("Content-Type: application/json\r\n\r\n")
        }.toByteArray()
        val suffix = "\r\n--$boundary--\r\n".toByteArray()
        val body = prefix + content + suffix

        val response = http.post(CREATE_URL) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.parse("multipart/related; boundary=$boundary"))
            setBody(body)
        }
        val text = response.bodyAsText()
        require(response.status.isSuccess()) { "Drive create failed: ${response.status} $text" }
        return json.parseToJsonElement(text).jsonObject["id"]!!.jsonPrimitive.content
    }

    /** Subsequent uploads: PATCH the media (content only). */
    suspend fun update(accessToken: String, fileId: String, content: ByteArray) {
        val response = http.patch("$UPDATE_URL$fileId?uploadType=media") {
            headers {
                append(HttpHeaders.Authorization, "Bearer $accessToken")
            }
            contentType(ContentType.Application.Json)
            setBody(content)
        }
        require(response.status.isSuccess()) {
            "Drive update failed: ${response.status} ${response.bodyAsText()}"
        }
    }

    data class DriveFileRef(val id: String, val modifiedEpochSec: Long)

    /** Returns the most recent file matching [fileName] in the appDataFolder, or null. */
    suspend fun findLatest(accessToken: String, fileName: String): DriveFileRef? {
        val q = "name = '${fileName.replace("'", "\\'")}' and trashed = false"
        val response = http.get(LIST_URL) {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            url {
                parameters.append("spaces", "appDataFolder")
                parameters.append("q", q)
                parameters.append("fields", "files(id,modifiedTime)")
                parameters.append("orderBy", "modifiedTime desc")
                parameters.append("pageSize", "1")
            }
        }
        if (!response.status.isSuccess()) return null
        val first = json.parseToJsonElement(response.bodyAsText())
            .jsonObject["files"]?.jsonArray
            ?.firstOrNull()
            ?.jsonObject
            ?: return null
        val id = first["id"]?.jsonPrimitive?.content ?: return null
        val modifiedSec = first["modifiedTime"]?.jsonPrimitive?.content
            ?.let { runCatching { java.time.Instant.parse(it).epochSecond }.getOrNull() }
            ?: (System.currentTimeMillis() / 1000)
        return DriveFileRef(id, modifiedSec)
    }

    suspend fun download(accessToken: String, fileId: String): String {
        val response = http.get("$LIST_URL/$fileId") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            url { parameters.append("alt", "media") }
        }
        require(response.status.isSuccess()) {
            "Drive download failed: ${response.status} ${response.bodyAsText()}"
        }
        return response.bodyAsText()
    }

    companion object {
        const val CREATE_URL = "https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart"
        const val UPDATE_URL = "https://www.googleapis.com/upload/drive/v3/files/"
        const val LIST_URL = "https://www.googleapis.com/drive/v3/files"
    }
}
