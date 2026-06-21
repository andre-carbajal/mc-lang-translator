package net.andrecarbajal.mclangtranslator.providers

import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

object HttpJsonClient {
    private val client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build()

    fun get(url: String, headers: Map<String, String> = emptyMap()): String {
        val builder = HttpRequest.newBuilder(URI.create(url)).GET()
        headers.forEach { (name, value) -> builder.header(name, value) }
        return send(builder.build())
    }

    fun post(
        url: String,
        body: String,
        contentType: String = "application/json",
        headers: Map<String, String> = emptyMap(),
    ): String {
        val builder = HttpRequest.newBuilder(URI.create(url))
            .header("Content-Type", contentType)
            .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
        headers.forEach { (name, value) -> builder.header(name, value) }
        return send(builder.build())
    }

    private fun send(request: HttpRequest): String {
        val response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))
        if (response.statusCode() !in 200..299) {
            throw TranslationException("HTTP ${response.statusCode()}: ${response.body().take(500)}")
        }
        return response.body()
    }

    fun encode(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8)
}

