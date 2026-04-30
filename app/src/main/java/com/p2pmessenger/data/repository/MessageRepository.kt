package com.p2pmessenger.data.repository

import com.p2pmessenger.crypto.EncryptionManager
import com.p2pmessenger.data.database.MessageDao
import com.p2pmessenger.data.model.Message
import com.p2pmessenger.data.model.MessageStatus
import com.p2pmessenger.p2p.WebRTCManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MessageRepository @Inject constructor(
    private val messageDao: MessageDao,
    private val encryptionManager: EncryptionManager,
    private val webRTCManager: WebRTCManager
) {
    
    fun getMessagesForPeer(peerId: String): Flow<List<Message>> {
        return messageDao.getMessagesForPeer(peerId)
    }
    
    fun getChatPreviews(): Flow<List<com.p2pmessenger.data.model.ChatPreview>> {
        return messageDao.getChatPreviews()
    }
    
    suspend fun sendMessage(peerId: String, content: String): Message {
        val messageId = UUID.randomUUID().toString()
        
        // Encrypt message
        val encryptedContent = encryptionManager.encryptMessage(peerId, content)
        
        val message = Message(
            id = messageId,
            peerId = peerId,
            content = encryptedContent,
            isOutgoing = true,
            status = MessageStatus.SENDING
        )
        
        messageDao.insertMessage(message)
        
        // Send via WebRTC
        val sent = webRTCManager.sendMessage(encryptedContent)
        
        val updatedMessage = message.copy(
            status = if (sent) MessageStatus.SENT else MessageStatus.FAILED
        )
        messageDao.updateMessage(updatedMessage)
        
        return updatedMessage
    }
    
    suspend fun receiveMessage(peerId: String, encryptedContent: String): Message {
        val messageId = UUID.randomUUID().toString()
        
        // Decrypt message
        val decryptedContent = encryptionManager.decryptMessage(peerId, encryptedContent)
        
        val message = Message(
            id = messageId,
            peerId = peerId,
            content = decryptedContent,
            isOutgoing = false,
            status = MessageStatus.DELIVERED
        )
        
        messageDao.insertMessage(message)
        return message
    }
    
    suspend fun markMessagesAsRead(peerId: String) {
        messageDao.markPeerMessagesAsRead(peerId)
    }
    
    suspend fun deleteMessage(messageId: String) {
        messageDao.deleteMessage(messageId)
    }
    
    suspend fun clearChat(peerId: String) {
        messageDao.deleteMessagesForPeer(peerId)
    }
}
