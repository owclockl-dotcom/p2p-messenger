package com.p2pmessenger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey
    val id: String,
    val peerId: String,
    val content: String,
    val isOutgoing: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val isEncrypted: Boolean = true,
    val isRead: Boolean = false,
    val status: MessageStatus = MessageStatus.SENT,
    // Telegram features
    val replyToId: String? = null,
    val messageType: MessageType = MessageType.TEXT,
    val mediaUrl: String? = null,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val voiceDuration: Int? = null,
    val isEdited: Boolean = false,
    val editedAt: Long? = null,
    val senderName: String? = null, // For group chats
    val isForwarded: Boolean = false,
    val originalSender: String? = null
)

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED
}

enum class MessageType {
    TEXT,
    PHOTO,
    VIDEO,
    VOICE,
    FILE,
    LOCATION,
    CONTACT,
    STICKER
}

data class ChatPreview(
    val peerId: String,
    val peerName: String,
    val lastMessage: String,
    val lastMessageTime: Long,
    val unreadCount: Int,
    val isOnline: Boolean,
    val avatarColor: Long,
    val isGroup: Boolean = false,
    val memberCount: Int = 0
)

// Group Chat Models (Telegram-style)
@Entity(tableName = "groups")
data class GroupChat(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String? = null,
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val creatorId: String,
    val isEncrypted: Boolean = true, // P2P encrypted group
    val memberCount: Int = 0,
    val maxMembers: Int = 200,
    val isPublic: Boolean = false,
    val inviteLink: String? = null
)

@Entity(tableName = "group_members")
data class GroupMember(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val groupId: String,
    val peerId: String,
    val role: GroupRole = GroupRole.MEMBER,
    val joinedAt: Long = System.currentTimeMillis(),
    val isOnline: Boolean = false,
    val lastSeen: Long? = null
)

enum class GroupRole {
    CREATOR,
    ADMIN,
    MEMBER
}

data class GroupMessage(
    val message: Message,
    val senderName: String,
    val senderAvatarColor: Long
)
