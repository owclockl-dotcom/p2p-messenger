package com.p2pmessenger.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.p2pmessenger.data.repository.MessageRepository
import com.p2pmessenger.data.repository.PeerRepository
import com.p2pmessenger.data.model.ChatPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatListViewModel @Inject constructor(
    private val messageRepository: MessageRepository,
    private val peerRepository: PeerRepository
) : ViewModel() {
    
    private val _chats = MutableStateFlow<List<ChatPreview>>(emptyList())
    val chats: StateFlow<List<ChatPreview>> = _chats.asStateFlow()
    
    init {
        loadChats()
    }
    
    private fun loadChats() {
        viewModelScope.launch {
            messageRepository.getChatPreviews().collect { chatPreviews ->
                _chats.value = chatPreviews
            }
        }
    }
    
    fun getMyPeerId(): String {
        return peerRepository.getMyPeerId()
    }
    
    fun getMyPublicKey(): String {
        return peerRepository.getMyPublicKey()
    }
}
