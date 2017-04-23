package org.jetbrains.ktor.websocket

import org.jetbrains.ktor.application.*
import org.jetbrains.ktor.cio.*
import org.jetbrains.ktor.content.*
import org.jetbrains.ktor.http.*
import org.jetbrains.ktor.request.*
import org.jetbrains.ktor.util.*
import java.io.*
import java.security.*

class WebSocketUpgrade(call: ApplicationCall, val protocol: String? = null, val configure: suspend WebSocket.() -> Unit) : FinalContent.ProtocolUpgrade() {
    private val key = call.request.header(HttpHeaders.SecWebSocketKey) ?: throw IllegalArgumentException("It should be ${HttpHeaders.SecWebSocketKey} header")

    override val status: HttpStatusCode?
        get() = HttpStatusCode.SwitchingProtocols

    override val headers: ValuesMap
        get() = ValuesMap.build(true) {
            append(HttpHeaders.Upgrade, "websocket")
            append(HttpHeaders.Connection, "Upgrade")
            append(HttpHeaders.SecWebSocketAccept, encodeBase64(sha1("${key.trim()}258EAFA5-E914-47DA-95CA-C5AB0DC85B11")))
            if (protocol != null) {
                append(HttpHeaders.SecWebSocketProtocol, protocol)
            }
            // TODO extensions
        }

    override suspend fun upgrade(call: ApplicationCall, input: ReadChannel, output: WriteChannel, channel: Closeable): Closeable {
        val webSocket = WebSocketImpl(call, input, output, channel)

        configure(webSocket)

        webSocket.init()

        return webSocket
    }

    private fun sha1(s: String) = MessageDigest.getInstance("SHA1").digest(s.toByteArray(Charsets.ISO_8859_1))
}