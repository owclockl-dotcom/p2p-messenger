package com.p2pmessenger.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.p2pmessenger.data.model.Message
import com.p2pmessenger.data.model.Peer

@Database(
    entities = [Message::class, Peer::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun peerDao(): PeerDao
}
