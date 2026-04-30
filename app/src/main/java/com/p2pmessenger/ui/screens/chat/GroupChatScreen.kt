package com.p2pmessenger.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p2pmessenger.data.model.GroupChat
import com.p2pmessenger.data.model.GroupMember
import com.p2pmessenger.data.model.GroupRole
import com.p2pmessenger.data.model.Peer
import com.p2pmessenger.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupChatScreen(
    group: GroupChat,
    members: List<GroupMember>,
    onBack: () -> Unit,
    onAddMember: () -> Unit,
    onLeaveGroup: () -> Unit,
    onDeleteGroup: () -> Unit,
    onMemberClick: (String) -> Unit,
    onEditGroup: () -> Unit,
    currentUserRole: GroupRole
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Info", "Members (${members.size})", "Media", "Files")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = group.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${members.size} members • ${if (group.isEncrypted) "Encrypted" else "Public"}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (currentUserRole == GroupRole.CREATOR || currentUserRole == GroupRole.ADMIN) {
                        IconButton(onClick = onEditGroup) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }
                    }
                    IconButton(onClick = { /* More */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Group Header
            GroupHeader(
                group = group,
                isCreator = currentUserRole == GroupRole.CREATOR
            )
            
            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Background,
                contentColor = Primary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 13.sp) }
                    )
                }
            }
            
            // Tab content
            when (selectedTab) {
                0 -> GroupInfoTab(
                    group = group,
                    onLeaveGroup = onLeaveGroup,
                    onDeleteGroup = if (currentUserRole == GroupRole.CREATOR) onDeleteGroup else null
                )
                1 -> MembersTab(
                    members = members,
                    onMemberClick = onMemberClick,
                    onAddMember = if (currentUserRole != GroupRole.MEMBER) onAddMember else null,
                    currentUserRole = currentUserRole
                )
                2 -> MediaTab()
                3 -> FilesTab()
            }
        }
    }
}

@Composable
fun GroupHeader(group: GroupChat, isCreator: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Group Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Primary),
            contentAlignment = Alignment.Center
        ) {
            if (group.avatarUrl != null) {
                // Load image
            } else {
                Text(
                    text = group.name.take(2).uppercase(),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Group name
        Text(
            text = group.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        // Description
        group.description?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Stats
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatItem("${group.memberCount}", "members")
            StatItem("${group.maxMembers}", "max")
            if (group.isEncrypted) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Encrypted",
                    tint = Success,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        if (isCreator && group.inviteLink != null) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(containerColor = Surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = Primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Invite Link",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = group.inviteLink.take(30) + "...",
                            fontSize = 14.sp,
                            color = Color.White,
                            maxLines = 1
                        )
                    }
                    IconButton(onClick = { /* Copy */ }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
fun GroupInfoTab(
    group: GroupChat,
    onLeaveGroup: () -> Unit,
    onDeleteGroup: (() -> Unit)?
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        item {
            InfoSection(title = "Group Type") {
                InfoRow(
                    icon = Icons.Default.Security,
                    label = "Encryption",
                    value = if (group.isEncrypted) "End-to-end encrypted" else "Standard"
                )
                InfoRow(
                    icon = Icons.Default.Visibility,
                    label = "Visibility",
                    value = if (group.isPublic) "Public" else "Private"
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            InfoSection(title = "Actions") {
                ActionButton(
                    icon = Icons.Default.ExitToApp,
                    label = "Leave Group",
                    color = Error,
                    onClick = onLeaveGroup
                )
                
                onDeleteGroup?.let {
                    ActionButton(
                        icon = Icons.Default.Delete,
                        label = "Delete Group",
                        color = Error,
                        onClick = it
                    )
                }
            }
        }
    }
}

@Composable
fun MembersTab(
    members: List<GroupMember>,
    onMemberClick: (String) -> Unit,
    onAddMember: (() -> Unit)?,
    currentUserRole: GroupRole
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp)
    ) {
        if (onAddMember != null) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Primary.copy(alpha = 0.1f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onAddMember)
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Add Member",
                            fontSize = 16.sp,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        items(members.sortedBy { it.role.ordinal }) { member ->
            MemberItem(
                member = member,
                onClick = { onMemberClick(member.peerId) },
                canManage = currentUserRole == GroupRole.CREATOR || 
                           (currentUserRole == GroupRole.ADMIN && member.role == GroupRole.MEMBER)
            )
        }
    }
}

@Composable
fun MemberItem(
    member: GroupMember,
    onClick: () -> Unit,
    canManage: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Primary.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = member.peerId.take(1).uppercase(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = member.peerId, // Would show actual name
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = member.role.name.lowercase().replaceFirstChar { it.uppercase() },
                    fontSize = 12.sp,
                    color = when (member.role) {
                        GroupRole.CREATOR -> Primary
                        GroupRole.ADMIN -> Success
                        GroupRole.MEMBER -> TextSecondary
                    }
                )
            }
            
            if (member.isOnline) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Online)
                )
            }
            
            if (canManage) {
                IconButton(onClick = { /* Manage */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = null, tint = TextSecondary)
                }
            }
        }
    }
}

@Composable
fun MediaTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.PhotoLibrary,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Shared Media",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Photos and videos will appear here",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun FilesTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.InsertDriveFile,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Shared Files",
                fontSize = 18.sp,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Documents and files will appear here",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun InfoSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            text = title.uppercase(),
            fontSize = 13.sp,
            color = TextSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = Surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = TextSecondary)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 12.sp, color = TextSecondary)
            Text(text = value, fontSize = 15.sp, color = Color.White)
        }
    }
}

@Composable
fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = color)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontSize = 16.sp, color = color)
    }
}
