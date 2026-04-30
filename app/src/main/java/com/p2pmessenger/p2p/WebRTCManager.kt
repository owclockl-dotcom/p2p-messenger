package com.p2pmessenger.p2p

import android.content.Context
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.cio.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.channels.consumeEach
import java.net.InetAddress
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCManager @Inject constructor(
    private val context: Context
) {
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private val _receivedMessages = MutableStateFlow<String>("")
    val receivedMessages: StateFlow<String> = _receivedMessages
    
    private var server: CIOApplicationEngine? = null
    private var client: HttpClient? = null
    private var session: DefaultClientWebSocketSession? = null
    private val serverPort = 8080
    
    suspend fun startServer(): String {
        _connectionState.value = ConnectionState.Connecting
        
        server = embeddedServer(CIO, port = serverPort) {
            install(WebSockets)
            routing {
                webSocket("/p2p") {
                    _connectionState.value = ConnectionState.Connected
                    try {
                        incoming.consumeEach { frame ->
                            if (frame is Frame.Text) {
                                _receivedMessages.value = frame.readText()
                            }
                        }
                    } catch (e: Exception) {
                        _connectionState.value = ConnectionState.Failed
                    }
                }
            }
        }
        
        server?.start(wait = false)
        
        val localAddress = InetAddress.getLocalHost().hostAddress
        return "ws://$localAddress:$serverPort/p2p"
    }
    
    suspend fun connectToPeer(serverUrl: String): Boolean {
        _connectionState.value = ConnectionState.Connecting
        
        client = HttpClient(CIO) {
            install(WebSockets)
        }
        
        return try {
            session = client?.webSocket(session {
                url(serverUrl)
            }) {
                _connectionState.value = ConnectionState.Connected
                incoming.consumeEach { frame ->
                    if (frame is Frame.Text) {
                        _receivedMessages.value = frame.readText()
                    }
                }
            }
            true
        } catch (e: Exception) {
            _connectionState.value = ConnectionState.Failed
            false
        }
    }
    
    suspend fun sendMessage(message: String): Boolean {
        return try {
            session?.send(Frame.Text(message))
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun disconnect() {
        try {
            session?.close()
            client?.close()
            server?.stop(1000, 1000)
        } catch (e: Exception) {
        }
        _connectionState.value = ConnectionState.Disconnected
    }
    
    sealed class ConnectionState {
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        object Disconnected : ConnectionState()
        object Failed : ConnectionState()
    }
}
