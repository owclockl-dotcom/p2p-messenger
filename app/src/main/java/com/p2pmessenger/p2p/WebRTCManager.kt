package com.p2pmessenger.p2p

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    private val _receivedMessages = MutableStateFlow<String>("")
    val receivedMessages: StateFlow<String> = _receivedMessages
    
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(0, TimeUnit.MILLISECONDS)
        .build()
    
    fun startServer(): String {
        _connectionState.value = ConnectionState.Connecting
        // For true P2P without servers, we'd need a different approach
        // For now, return a placeholder URL
        val localAddress = InetAddress.getLocalHost().hostAddress
        return "ws://$localAddress:8080/p2p"
    }
    
    fun connectToPeer(serverUrl: String): Boolean {
        _connectionState.value = ConnectionState.Connecting
        
        val request = Request.Builder()
            .url(serverUrl)
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.Connected
            }
            
            override fun onMessage(ws: WebSocket, text: String) {
                _receivedMessages.value = text
            }
            
            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.Disconnected
            }
            
            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.Failed
            }
        })
        
        return true
    }
    
    fun sendMessage(message: String): Boolean {
        return try {
            webSocket?.send(message) ?: false
        } catch (e: Exception) {
            false
        }
    }
    
    fun disconnect() {
        try {
            webSocket?.close(1000, "Disconnecting")
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
