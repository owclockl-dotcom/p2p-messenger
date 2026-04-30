package com.p2pmessenger.data.database

import androidx.room.*
import com.p2pmessenger.data.model.Message
import com.p2pmessenger.data.model.ChatPreview
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDao {
    
    @Query("SELECT * FROM messages WHERE peerId = :peerId ORDER BY timestamp DESC")
    fun getMessagesForPeer(peerId: String): Flow<List<Message>>
    
    @Query("""
        SELECT 
            p.id as peerId,
            p.name as peerName,
            m.content as lastMessage,
            m.timestamp as lastMessageTime,
            (SELECT COUNT(*) FROM messages WHERE peerId = p.id AND isRead = 0 AND isOutgoing = 0) as unreadCount,
            p.isOnline as isOnline,
            p.avatarColor as avatarColor
        FROM peers p
        LEFT JOIN messages m ON p.id = m.peerId
        WHERE m.id IN (SELECT MAX(id) FROM messages WHERE peerId = p.id)
        ORDER BY m.timestamp DESC
    """)
    fun getChatPreviews(): Flow<List<ChatPreview>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)
    
    @Update
    suspend fun updateMessage(message: Message)
    
    @Query("UPDATE messages SET isRead = 1 WHERE peerId = :peerId AND isOutgoing = 0")
    suspend fun markPeerMessagesAsRead(peerId: String)
    
    @Query("DELETE FROM messages WHERE peerId = :peerId")
    suspend fun deleteMessagesForPeer(peerId: String)
    
    @Query("DELETE FROM messages WHERE id = :messageId")
    suspend fun deleteMessage(messageId: String)
}
