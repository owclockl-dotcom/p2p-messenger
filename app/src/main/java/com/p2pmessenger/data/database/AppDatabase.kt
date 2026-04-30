package com.p2pmessenger.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.p2pmessenger.data.model.GroupChat
import com.p2pmessenger.data.model.GroupMember
import com.p2pmessenger.data.model.Message
import com.p2pmessenger.data.model.Peer

@Database(
    entities = [Message::class, Peer::class, GroupChat::class, GroupMember::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun messageDao(): MessageDao
    abstract fun peerDao(): PeerDao
    abstract fun groupChatDao(): GroupChatDao
    abstract fun groupMemberDao(): GroupMemberDao
    
    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns to messages table
                db.execSQL("ALTER TABLE messages ADD COLUMN replyToId TEXT")
                db.execSQL("ALTER TABLE messages ADD COLUMN messageType TEXT NOT NULL DEFAULT 'TEXT'")
                db.execSQL("ALTER TABLE messages ADD COLUMN mediaUrl TEXT")
                db.execSQL("ALTER TABLE messages ADD COLUMN fileName TEXT")
                db.execSQL("ALTER TABLE messages ADD COLUMN fileSize INTEGER")
                db.execSQL("ALTER TABLE messages ADD COLUMN voiceDuration INTEGER")
                db.execSQL("ALTER TABLE messages ADD COLUMN isEdited INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN editedAt INTEGER")
                db.execSQL("ALTER TABLE messages ADD COLUMN senderName TEXT")
                db.execSQL("ALTER TABLE messages ADD COLUMN isForwarded INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE messages ADD COLUMN originalSender TEXT")
                
                // Create groups table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS groups (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        description TEXT,
                        avatarUrl TEXT,
                        createdAt INTEGER NOT NULL,
                        creatorId TEXT NOT NULL,
                        isEncrypted INTEGER NOT NULL DEFAULT 1,
                        memberCount INTEGER NOT NULL DEFAULT 0,
                        maxMembers INTEGER NOT NULL DEFAULT 200,
                        isPublic INTEGER NOT NULL DEFAULT 0,
                        inviteLink TEXT
                    )
                """)
                
                // Create group_members table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS group_members (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        groupId TEXT NOT NULL,
                        peerId TEXT NOT NULL,
                        role TEXT NOT NULL DEFAULT 'MEMBER',
                        joinedAt INTEGER NOT NULL,
                        isOnline INTEGER NOT NULL DEFAULT 0,
                        lastSeen INTEGER
                    )
                """)
            }
        }
    }
}
