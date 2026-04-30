package com.p2pmessenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.p2pmessenger.data.model.Message
import com.p2pmessenger.data.repository.MessageRepository
import com.p2pmessenger.data.repository.PeerRepository
import com.p2pmessenger.p2p.WebRTCManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val peerRepository: PeerRepository,
    private val webRTCManager: WebRTCManager
) : ViewModel() {
    
    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()
    
    private val _connectionState = MutableStateFlow<WebRTCManager.ConnectionState>(WebRTCManager.ConnectionState.Disconnected)
    val connectionState: StateFlow<WebRTCManager.ConnectionState> = _connectionState.asStateFlow()
    
    fun loadMessages(peerId: String) {
        viewModelScope.launch {
            messageRepository.getMessagesForPeer(peerId).collect { messages ->
                _messages.value = messages
            }
        }
    }
    
    fun sendMessage(peerId: String, content: String) {
        viewModelScope.launch {
            messageRepository.sendMessage(peerId, content)
        }
    }
    
    fun markAsRead(peerId: String) {
        viewModelScope.launch {
            messageRepository.markMessagesAsRead(peerId)
        }
    }
    
    fun connectToPeer(peerId: String) {
        viewModelScope.launch {
            _connectionState.value = WebRTCManager.ConnectionState.Connecting
            // WebRTC connection logic would go here
        }
    }
    
    fun disconnect() {
        webRTCManager.disconnect()
        _connectionState.value = WebRTCManager.ConnectionState.Disconnected
    }
}
