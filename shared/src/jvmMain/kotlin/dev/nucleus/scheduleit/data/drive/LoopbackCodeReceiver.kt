package dev.nucleus.scheduleit.data.drive

import com.sun.net.httpserver.HttpServer
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.util.concurrent.CompletableFuture

internal class LoopbackCodeReceiver {

    data class Started(
        val port: Int,
        val result: CompletableFuture<String>,
        val close: () -> Unit,
    )

    fun start(expectedState: String): Started {
        val future = CompletableFuture<String>()
        // Binding explicitly to 127.0.0.1 (not 0.0.0.0) avoids the Windows Defender Firewall prompt.
        val address = InetSocketAddress(InetAddress.getByName("127.0.0.1"), 0)
        val server = HttpServer.create(address, 0)
        server.createContext("/callback") { exchange ->
            val params = parseQuery(exchange.requestURI.rawQuery.orEmpty())
            val body = """
                <html><body style="font-family:sans-serif;padding:2em;text-align:center">
                <h2>Authorization complete</h2>
                <p>You can close this tab and return to ScheduleIt.</p>
                </body></html>
            """.trimIndent().toByteArray()
            exchange.responseHeaders.add("Content-Type", "text/html; charset=utf-8")
            exchange.sendResponseHeaders(200, body.size.toLong())
            exchange.responseBody.use { it.write(body) }

            val err = params["error"]
            val state = params["state"]
            val code = params["code"]
            when {
                err != null -> future.completeExceptionally(IllegalStateException("OAuth error: $err"))
                state != expectedState -> future.completeExceptionally(IllegalStateException("State mismatch"))
                code.isNullOrEmpty() -> future.completeExceptionally(IllegalStateException("Missing authorization code"))
                else -> future.complete(code)
            }
        }
        server.executor = null
        server.start()
        return Started(
            port = server.address.port,
            result = future,
            close = { runCatching { server.stop(0) } },
        )
    }

    private fun parseQuery(query: String): Map<String, String> {
        if (query.isEmpty()) return emptyMap()
        return query.split("&").mapNotNull { part ->
            val idx = part.indexOf('=')
            if (idx <= 0) return@mapNotNull null
            val key = part.substring(0, idx)
            val value = URLDecoder.decode(part.substring(idx + 1), "UTF-8")
            key to value
        }.toMap()
    }
}
