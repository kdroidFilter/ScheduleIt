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
            val isError = params["error"] != null ||
                params["state"] != expectedState ||
                params["code"].isNullOrEmpty()
            val body = renderResponseHtml(isError = isError, errorMessage = params["error"]).toByteArray()
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

    private fun renderResponseHtml(isError: Boolean, errorMessage: String?): String {
        val accent = if (isError) "#e53935" else "#42a5f5"
        val accentShadow = if (isError) "rgba(229,57,53,0.35)" else "rgba(66,165,245,0.35)"
        val title = if (isError) "Authorization failed" else "You're all set"
        val subtitle = if (isError) {
            "ScheduleIt could not complete the connection${errorMessage?.let { ": $it" } ?: "."}"
        } else {
            "ScheduleIt is now connected to your Google Drive."
        }
        val hint = if (isError) {
            "You can close this tab and try again from the app."
        } else {
            "You can close this tab and go back to ScheduleIt."
        }
        val icon = if (isError) {
            "<path d=\"M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z\" fill=\"#fff\"/>"
        } else {
            "<path d=\"M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z\" fill=\"#fff\"/>"
        }
        return """
<!doctype html>
<html lang="en">
<head>
<meta charset="utf-8">
<title>ScheduleIt — $title</title>
<style>
  :root { color-scheme: light dark; }
  * { box-sizing: border-box; }
  html, body { margin: 0; padding: 0; height: 100%; }
  body {
    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
    background: linear-gradient(135deg, #f5f7fa 0%, #e4e9f0 100%);
    color: #1a1a1a;
    display: flex; align-items: center; justify-content: center;
    min-height: 100vh; padding: 24px;
  }
  @media (prefers-color-scheme: dark) {
    body { background: linear-gradient(135deg, #1f2229 0%, #2a2f3a 100%); color: #f4f4f4; }
    .card { background: #2c313a; box-shadow: 0 10px 40px rgba(0,0,0,0.45); }
    .hint { color: #a8acb6; }
  }
  .card {
    background: #ffffff;
    border-radius: 16px;
    padding: 40px 36px;
    max-width: 420px; width: 100%;
    text-align: center;
    box-shadow: 0 10px 40px rgba(20,30,60,0.15);
    animation: pop 0.45s cubic-bezier(0.2, 0.9, 0.3, 1.2);
  }
  .badge {
    width: 72px; height: 72px;
    border-radius: 50%;
    background: $accent;
    display: inline-flex; align-items: center; justify-content: center;
    margin: 0 auto 22px;
    box-shadow: 0 8px 22px $accentShadow;
  }
  .badge svg { width: 40px; height: 40px; }
  h1 { font-size: 22px; margin: 0 0 10px; font-weight: 600; letter-spacing: -0.01em; }
  p { font-size: 15px; line-height: 1.5; margin: 0 0 8px; }
  .hint { color: #6b7280; font-size: 13px; margin-top: 18px; }
  .brand { margin-top: 28px; font-size: 12px; letter-spacing: 0.08em; text-transform: uppercase; opacity: 0.55; }
  @keyframes pop {
    from { opacity: 0; transform: translateY(8px) scale(0.98); }
    to   { opacity: 1; transform: translateY(0)   scale(1); }
  }
</style>
</head>
<body>
  <main class="card">
    <div class="badge" aria-hidden="true">
      <svg viewBox="0 0 24 24">$icon</svg>
    </div>
    <h1>$title</h1>
    <p>$subtitle</p>
    <p class="hint">$hint</p>
    <div class="brand">ScheduleIt</div>
  </main>
  <script>
    setTimeout(function () { try { window.close(); } catch (e) {} }, 2500);
  </script>
</body>
</html>
        """.trimIndent()
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
