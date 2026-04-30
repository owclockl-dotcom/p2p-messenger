package com.p2pmessenger.p2p

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.io.File
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebRTCManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    // Connection state
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    // Messages
    private val _receivedMessages = MutableStateFlow<MessageData?>(null)
    val receivedMessages: StateFlow<MessageData?> = _receivedMessages.asStateFlow()
    
    // Call state
    private val _callState = MutableStateFlow<CallState>(CallState.Idle)
    val callState: StateFlow<CallState> = _callState.asStateFlow()
    
    private var webSocket: WebSocket? = null
    private var currentPeerId: String? = null
    
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .writeTimeout(0, TimeUnit.MILLISECONDS)
        .pingInterval(30, TimeUnit.SECONDS)
        .build()
    
    // Connection management
    fun connectToPeer(peerId: String, serverUrl: String): Boolean {
        currentPeerId = peerId
        _connectionState.value = ConnectionState.Connecting
        
        val request = Request.Builder()
            .url(serverUrl)
            .header("X-Peer-ID", peerId)
            .build()
        
        webSocket = client.newWebSocket(request, createWebSocketListener())
        return true
    }
    
    private fun createWebSocketListener(): WebSocketListener {
        return object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                _connectionState.value = ConnectionState.Connected
            }
            
            override fun onMessage(ws: WebSocket, text: String) {
                handleIncomingMessage(text)
            }
            
            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                _connectionState.value = ConnectionState.Disconnected
            }
            
            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                _connectionState.value = ConnectionState.Failed
            }
        }
    }
    
    private fun handleIncomingMessage(text: String) {
        try {
            val json = JSONObject(text)
            val type = json.getString("type")
            
            when (type) {
                "message" -> {
                    _receivedMessages.value = MessageData(
                        peerId = json.getString("from"),
                        content = json.getString("content"),
                        timestamp = json.getLong("timestamp"),
                        type = MessageType.TEXT
                    )
                }
                "file" -> {
                    _receivedMessages.value = MessageData(
                        peerId = json.getString("from"),
                        content = json.getString("fileName"),
                        timestamp = json.getLong("timestamp"),
                        type = MessageType.FILE,
                        fileData = json.optString("fileData")
                    )
                }
                "call_offer" -> {
                    _callState.value = CallState.Incoming(
                        peerId = json.getString("from"),
                        isVideo = json.optBoolean("video", false)
                    )
                }
                "call_answer" -> {
                    _callState.value = CallState.Connected(
                        peerId = json.getString("from"),
                        isVideo = json.optBoolean("video", false)
                    )
                }
                "call_end" -> {
                    _callState.value = CallState.Idle
                }
            }
        } catch (e: Exception) {
            // Handle raw text messages
            _receivedMessages.value = MessageData(
                peerId = "unknown",
                content = text,
                timestamp = System.currentTimeMillis(),
                type = MessageType.TEXT
            )
        }
    }
    
    // Messaging
    fun sendMessage(content: String): Boolean {
        val message = JSONObject().apply {
            put("type", "message")
            put("content", content)
            put("timestamp", System.currentTimeMillis())
        }
        return webSocket?.send(message.toString()) ?: false
    }
    
    // File sharing
    fun sendFile(fileUri: Uri, fileName: String): Boolean {
        // In real implementation, read file and send in chunks
        val message = JSONObject().apply {
            put("type", "file")
            put("fileName", fileName)
            put("timestamp", System.currentTimeMillis())
        }
        return webSocket?.send(message.toString()) ?: false
    }
    
    // Voice/Video calls
    fun startCall(peerId: String, isVideo: Boolean): Boolean {
        val message = JSONObject().apply {
            put("type", "call_offer")
            put("to", peerId)
            put("video", isVideo)
        }
        _callState.value = CallState.Calling(peerId, isVideo)
        return webSocket?.send(message.toString()) ?: false
    }
    
    fun answerCall(accept: Boolean, isVideo: Boolean): Boolean {
        val currentCall = _callState.value as? CallState.Incoming
            ?: return false
            
        val message = JSONObject().apply {
            put("type", "call_answer")
            put("to", currentCall.peerId)
            put("accepted", accept)
            put("video", isVideo)
        }
        
        if (accept) {
            _callState.value = CallState.Connected(currentCall.peerId, isVideo)
        } else {
            _callState.value = CallState.Idle
        }
        
        return webSocket?.send(message.toString()) ?: false
    }
    
    fun endCall(): Boolean {
        val message = JSONObject().apply {
            put("type", "call_end")
        }
        _callState.value = CallState.Idle
        return webSocket?.send(message.toString()) ?: false
    }
    
    // Disconnect
    fun disconnect() {
        try {
            webSocket?.close(1000, "Disconnecting")
        } catch (e: Exception) {
            // Ignore
        }
        webSocket = null
        _connectionState.value = ConnectionState.Disconnected
        _callState.value = CallState.Idle
    }
    
    // Get local IP for P2P
    fun getLocalAddress(): String {
        return try {
            InetAddress.getLocalHost().hostAddress
        } catch (e: Exception) {
            "127.0.0.1"
        }
    }
    
    // Data classes
    data class MessageData(
        val peerId: String,
        val content: String,
        val timestamp: Long,
        val type: MessageType,
        val fileData: String? = null
    )
    
    enum class MessageType {
        TEXT, FILE, IMAGE, VOICE
    }
    
    // States
    sealed class ConnectionState {
        object Connecting : ConnectionState()
        object Connected : ConnectionState()
        object Disconnected : ConnectionState()
        object Failed : ConnectionState()
    }
    
    sealed class CallState {
        object Idle : CallState()
        data class Calling(val peerId: String, val isVideo: Boolean) : CallState()
        data class Incoming(val peerId: String, val isVideo: Boolean) : CallState()
        data class Connected(val peerId: String, val isVideo: Boolean) : CallState()
    }
}
