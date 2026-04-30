package com.p2pmessenger.data.database

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "p2p_messenger_database"
        )
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideMessageDao(database: AppDatabase): MessageDao {
        return database.messageDao()
    }
    
    @Provides
    @Singleton
    fun providePeerDao(database: AppDatabase): PeerDao {
        return database.peerDao()
    }
    
    @Provides
    @Singleton
    fun provideGroupChatDao(database: AppDatabase): GroupChatDao {
        return database.groupChatDao()
    }
    
    @Provides
    @Singleton
    fun provideGroupMemberDao(database: AppDatabase): GroupMemberDao {
        return database.groupMemberDao()
    }
}
