package io.github.xjet.core

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

/** Immutable HTTP request for the framework-level network layer. */
data class HttpRequest(
    val method: String = "GET",
    val url: String,
    val headers: Map<String, String> = emptyMap(),
    val body: ByteArray? = null,
    val connectTimeoutMillis: Int = 10_000,
    val readTimeoutMillis: Int = 10_000,
)

/** Response returned by any [HttpProvider]. */
data class HttpResponse(
    val status: Int,
    val headers: Map<String, List<String>> = emptyMap(),
    val body: ByteArray = ByteArray(0),
) {
    val bodyText: String get() = body.toString(Charsets.UTF_8)
    val isSuccess: Boolean get() = status in 200..299
}

/**
 * Network SPI (contract). Swap in Retrofit/OkHttp/your client by implementing
 * this and re-registering, e.g. `XJet.override(HttpProvider::class.java, ..)`.
 * The built-in default uses java.net.HttpURLConnection and needs no third-party.
 */
interface HttpProvider {
    suspend fun execute(request: HttpRequest): HttpResponse
}

/** Dependency-free default [HttpProvider] built on java.net.HttpURLConnection. */
class JdkHttpProvider : HttpProvider {

    override suspend fun execute(request: HttpRequest): HttpResponse = withContext(Dispatchers.IO) {
        val conn = (URL(request.url).openConnection() as HttpURLConnection).apply {
            requestMethod = request.method
            connectTimeout = request.connectTimeoutMillis
            readTimeout = request.readTimeoutMillis
            request.headers.forEach { (key, value) -> setRequestProperty(key, value) }
            if (!request.headers.containsKey("Accept")) setRequestProperty("Accept", "application/json")
            if (request.body != null) {
                doOutput = true
                setRequestProperty("Content-Length", request.body!!.size.toString())
                outputStream.use { it.write(request.body) }
            }
        }
        try {
            val status = conn.responseCode
            val input = if (status in 200..299) conn.inputStream else conn.errorStream
            val body = readAll(input)
            val headers = conn.headerFields?.mapKeys { it.key ?: "" } ?: emptyMap()
            HttpResponse(status, headers, body)
        } finally {
            conn.disconnect()
        }
    }

    private fun readAll(input: InputStream?): ByteArray {
        if (input == null) return ByteArray(0)
        return input.use { stream ->
            val out = ByteArrayOutputStream()
            val buffer = ByteArray(8192)
            while (true) {
                val read = stream.read(buffer)
                if (read < 0) break
                out.write(buffer, 0, read)
            }
            out.toByteArray()
        }
    }
}

/** Convenience suspend helpers over [XJet.http]. */
suspend fun XJet.getText(url: String, headers: Map<String, String> = emptyMap()): HttpResponse =
    http().execute(HttpRequest(method = "GET", url = url, headers = headers))

suspend fun XJet.postJson(url: String, json: String, headers: Map<String, String> = emptyMap()): HttpResponse {
    val hdrs = headers + ("Content-Type" to "application/json; charset=utf-8")
    return http().execute(HttpRequest(method = "POST", url = url, headers = hdrs, body = json.toByteArray(Charsets.UTF_8)))
}
