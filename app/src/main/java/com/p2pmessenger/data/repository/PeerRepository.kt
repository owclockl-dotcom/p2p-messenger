package com.p2pmessenger.data.repository

import com.p2pmessenger.crypto.EncryptionManager
import com.p2pmessenger.data.database.PeerDao
import com.p2pmessenger.data.model.Peer
import com.p2pmessenger.data.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PeerRepository @Inject constructor(
    private val peerDao: PeerDao,
    private val encryptionManager: EncryptionManager
) {
    
    fun getAllPeers(): Flow<List<Peer>> {
        return peerDao.getAllPeers()
    }
    
    fun getOnlinePeers(): Flow<List<Peer>> {
        return peerDao.getOnlinePeers()
    }
    
    suspend fun getPeerById(peerId: String): Peer? {
        return peerDao.getPeerById(peerId)
    }
    
    suspend fun addPeer(peerId: String, name: String, publicKey: String): Peer {
        val peer = Peer(
            id = peerId,
            name = name,
            publicKey = publicKey,
            avatarColor = generateAvatarColor(name),
            isOnline = false,
            connectionStatus = ConnectionStatus.DISCONNECTED
        )
        peerDao.insertPeer(peer)
        return peer
    }
    
    suspend fun updatePeerStatus(peerId: String, isOnline: Boolean) {
        peerDao.updatePeerStatus(peerId, isOnline, System.currentTimeMillis())
    }
    
    suspend fun updateConnectionStatus(peerId: String, status: ConnectionStatus) {
        peerDao.updateConnectionStatus(peerId, status)
    }
    
    suspend fun deletePeer(peerId: String) {
        peerDao.deletePeerById(peerId)
    }
    
    fun getMyPeerId(): String {
        return encryptionManager.getPeerId()
    }
    
    fun getMyPublicKey(): String {
        return encryptionManager.getPublicKey()
    }
    
    private fun generateAvatarColor(name: String): Long {
        val hash = name.hashCode()
        val hue = (hash and 0xFFFFFF) % 360
        return android.graphics.Color.HSVToColor(floatArrayOf(hue.toFloat(), 0.7f, 0.9f)).toLong()
    }
}
