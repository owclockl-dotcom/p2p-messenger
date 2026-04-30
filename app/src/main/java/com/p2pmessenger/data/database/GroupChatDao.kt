package com.p2pmessenger.data.database

import androidx.room.*
import com.p2pmessenger.data.model.GroupChat
import com.p2pmessenger.data.model.GroupMember
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupChatDao {
    @Query("SELECT * FROM groups ORDER BY createdAt DESC")
    fun getAllGroups(): Flow<List<GroupChat>>
    
    @Query("SELECT * FROM groups WHERE id = :groupId")
    suspend fun getGroupById(groupId: String): GroupChat?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: GroupChat)
    
    @Update
    suspend fun updateGroup(group: GroupChat)
    
    @Delete
    suspend fun deleteGroup(group: GroupChat)
    
    @Query("SELECT * FROM groups WHERE inviteLink = :inviteLink LIMIT 1")
    suspend fun getGroupByInviteLink(inviteLink: String): GroupChat?
}

@Dao
interface GroupMemberDao {
    @Query("SELECT * FROM group_members WHERE groupId = :groupId ORDER BY role DESC, joinedAt ASC")
    fun getMembersByGroup(groupId: String): Flow<List<GroupMember>>
    
    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND peerId = :peerId LIMIT 1")
    suspend fun getMember(groupId: String, peerId: String): GroupMember?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: GroupMember)
    
    @Update
    suspend fun updateMember(member: GroupMember)
    
    @Delete
    suspend fun deleteMember(member: GroupMember)
    
    @Query("DELETE FROM group_members WHERE groupId = :groupId AND peerId = :peerId")
    suspend fun removeMember(groupId: String, peerId: String)
    
    @Query("SELECT COUNT(*) FROM group_members WHERE groupId = :groupId")
    suspend fun getMemberCount(groupId: String): Int
}
