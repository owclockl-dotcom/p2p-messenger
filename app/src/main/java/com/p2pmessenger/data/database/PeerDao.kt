package com.p2pmessenger.data.database

import androidx.room.*
import com.p2pmessenger.data.model.Peer
import com.p2pmessenger.data.model.ConnectionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerDao {
    
    @Query("SELECT * FROM peers ORDER BY lastSeen DESC")
    fun getAllPeers(): Flow<List<Peer>>
    
    @Query("SELECT * FROM peers WHERE id = :peerId")
    suspend fun getPeerById(peerId: String): Peer?
    
    @Query("SELECT * FROM peers WHERE isOnline = 1")
    fun getOnlinePeers(): Flow<List<Peer>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPeer(peer: Peer)
    
    @Update
    suspend fun updatePeer(peer: Peer)
    
    @Query("UPDATE peers SET isOnline = :isOnline, lastSeen = :lastSeen WHERE id = :peerId")
    suspend fun updatePeerStatus(peerId: String, isOnline: Boolean, lastSeen: Long)
    
    @Query("UPDATE peers SET connectionStatus = :status WHERE id = :peerId")
    suspend fun updateConnectionStatus(peerId: String, status: ConnectionStatus)
    
    @Delete
    suspend fun deletePeer(peer: Peer)
    
    @Query("DELETE FROM peers WHERE id = :peerId")
    suspend fun deletePeerById(peerId: String)
}
