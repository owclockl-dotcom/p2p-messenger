package com.p2pmessenger.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peers")
data class Peer(
    @PrimaryKey
    val id: String,
    val name: String,
    val publicKey: String,
    val avatarColor: Long,
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val connectionStatus: ConnectionStatus = ConnectionStatus.DISCONNECTED
)

enum class ConnectionStatus {
    CONNECTED,
    CONNECTING,
    DISCONNECTED
}
