package com.p2pmessenger.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p2pmessenger.data.model.Message
import com.p2pmessenger.data.model.MessageStatus
import com.p2pmessenger.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBarDefaults

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdvancedChatScreen(
    peerId: String,
    peerName: String = "Peer",
    isGroup: Boolean = false,
    memberCount: Int = 0,
    onBack: () -> Unit,
    onVoiceCall: () -> Unit = {},
    onVideoCall: () -> Unit = {},
    onSendMessage: (String, String?) -> Unit = { _, _ -> },
    onSendFile: (Uri, String, MessageType) -> Unit = { _, _, _ -> },
    onDeleteMessage: (String) -> Unit = {},
    onReplyMessage: (String) -> Unit = {},
    onSearch: () -> Unit = {},
    messages: List<Message> = emptyList()
) {
    var messageText by remember { mutableStateOf("") }
    var replyToMessage by remember { mutableStateOf<Message?>(null) }
    var selectedMessage by remember { mutableStateOf<Message?>(null) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // File pickers
    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { onSendFile(it, "image.jpg", MessageType.IMAGE) }
    }
    
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { 
            val name = getDisplayName(context, it) ?: "file"
            onSendFile(it, name, MessageType.FILE) 
        }
    }
    
    Scaffold(
        topBar = {
            if (showSearchBar) {
                SearchTopBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onClose = { showSearchBar = false },
                    onSearch = { /* Search messages */ }
                )
            } else {
                ChatTopBar(
                    peerName = peerName,
                    isGroup = isGroup,
                    memberCount = memberCount,
                    onBack = onBack,
                    onVoiceCall = onVoiceCall,
                    onVideoCall = onVideoCall,
                    onSearch = { showSearchBar = true },
                    selectedMessage = selectedMessage,
                    onDeleteSelected = {
                        selectedMessage?.let { onDeleteMessage(it.id) }
                        selectedMessage = null
                    },
                    onReplySelected = {
                        selectedMessage?.let { 
                            replyToMessage = it
                            selectedMessage = null
                        }
                    }
                )
            }
        },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Messages list with animations
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                reverseLayout = true
            ) {
                items(
                    items = messages.filter { 
                        it.content.contains(searchQuery, ignoreCase = true) 
                    },
                    key = { it.id }
                ) { message ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + scaleIn(initialScale = 0.8f),
                        exit = fadeOut()
                    ) {
                        AdvancedMessageBubble(
                            message = message,
                            isSelected = selectedMessage?.id == message.id,
                            onClick = {
                                if (selectedMessage != null) {
                                    selectedMessage = if (selectedMessage?.id == message.id) null else message
                                }
                            },
                            onLongClick = {
                                selectedMessage = message
                            },
                            onReply = { onReplyMessage(message.id) }
                        )
                    }
                }
            }
            
            // Reply preview
            if (replyToMessage != null) {
                ReplyPreview(
                    message = replyToMessage!!,
                    onCancel = { replyToMessage = null }
                )
            }
            
            // Emoji picker (simplified)
            if (showEmojiPicker) {
                EmojiPicker(
                    onEmojiSelected = { emoji ->
                        messageText += emoji
                        showEmojiPicker = false
                    },
                    onClose = { showEmojiPicker = false }
                )
            }
            
            // Encryption info
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Success,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isGroup) "End-to-end encrypted group" else "End-to-end encrypted",
                        fontSize = 11.sp,
                        color = Success
                    )
                }
            }
            
            // Input area
            ChatInputArea(
                messageText = messageText,
                onMessageChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        onSendMessage(messageText, replyToMessage?.id)
                        messageText = ""
                        replyToMessage = null
                    }
                },
                onAttachmentClick = { imagePicker.launch("image/*") },
                onFileClick = { filePicker.launch("*/*") },
                onCameraClick = { /* Camera */ },
                onEmojiToggle = { showEmojiPicker = !showEmojiPicker },
                isRecording = isRecording,
                onStartRecording = { isRecording = true },
                onStopRecording = { 
                    isRecording = false
                    // Send voice message
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    peerName: String,
    isGroup: Boolean,
    memberCount: Int,
    onBack: () -> Unit,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit,
    onSearch: () -> Unit,
    selectedMessage: Message?,
    onDeleteSelected: () -> Unit,
    onReplySelected: () -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = peerName.firstOrNull()?.toString() ?: "?",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = peerName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isGroup) "$memberCount members" else "online",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            if (selectedMessage != null) {
                IconButton(onClick = onReplySelected) {
                    Icon(Icons.Default.Reply, contentDescription = "Reply", tint = Primary)
                }
                IconButton(onClick = onDeleteSelected) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Error)
                }
                IconButton(onClick = { /* Forward */ }) {
                    Icon(Icons.Default.Forward, contentDescription = "Forward")
                }
            } else {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
                IconButton(onClick = onVoiceCall) {
                    Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = Primary)
                }
                IconButton(onClick = onVideoCall) {
                    Icon(Icons(Icons.Default.Videocam, contentDescription = "Video Call", tint = Primary)
                }
                IconButton(onClick = { /* More */ }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More")
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Background,
            titleContentColor = Color.White
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTopBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    onSearch: () -> Unit
) {
    TopAppBar(
        title = {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search in chat...") },
                singleLine = true,
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    containerColor = Surface,
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = Color.Transparent
                ),
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                }
            )
        },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Background
        )
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AdvancedMessageBubble(
    message: Message,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onReply: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    
    val backgroundColor = when {
        isSelected -> SurfaceVariant.copy(alpha = 0.5f)
        message.isOutgoing -> Primary
        else -> SurfaceVariant
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .alpha(if (isSelected) 0.8f else 1f),
        horizontalArrangement = if (message.isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = if (message.isOutgoing) {
                RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
            } else {
                RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
            },
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 2.dp else 0.dp),
            modifier = Modifier
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .widthIn(max = 280.dp)
            ) {
                // Reply info
                if (message.replyToId != null) {
                    ReplyInfo(replyToId = message.replyToId)
                }
                
                // Message content
                SelectionContainer {
                    Text(
                        text = message.content,
                        fontSize = 15.sp,
                        color = Color.White,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                
                // Time and status
                Row(
                    modifier = Modifier.align(Alignment.End),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = timeFormat.format(Date(message.timestamp)),
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    if (message.isOutgoing) {
                        Spacer(modifier = Modifier.width(4.dp))
                        MessageStatusIcon(status = message.status)
                    }
                }
            }
        }
    }
}

@Composable
fun MessageStatusIcon(status: MessageStatus) {
    val icon = when (status) {
        MessageStatus.SENDING -> Icons.Default.Schedule
        MessageStatus.SENT -> Icons.Default.Done
        MessageStatus.DELIVERED -> Icons.Default.DoneAll
        MessageStatus.READ -> Icons.Default.DoneAll
        MessageStatus.FAILED -> Icons.Default.Error
    }
    
    val tint = when (status) {
        MessageStatus.READ -> Primary
        MessageStatus.FAILED -> Error
        else -> TextSecondary
    }
    
    Icon(
        imageVector = icon,
        contentDescription = status.name,
        tint = tint,
        modifier = Modifier.size(14.dp)
    )
}

@Composable
fun ReplyInfo(replyToId: String?) {
    if (replyToId == null) return
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(24.dp)
                    .background(Primary)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "Replying to message",
                    fontSize = 12.sp,
                    color = Primary
                )
                Text(
                    text = replyToId.take(20) + "...",
                    fontSize = 13.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun ReplyPreview(
    message: Message,
    onCancel: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(32.dp)
                    .background(Primary)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Replying to",
                    fontSize = 12.sp,
                    color = Primary
                )
                Text(
                    text = message.content.take(50),
                    fontSize = 14.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onCancel) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = TextSecondary)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInputArea(
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSend: () -> Unit,
    onAttachmentClick: () -> Unit,
    onFileClick: () -> Unit,
    onCameraClick: () -> Unit,
    onEmojiToggle: () -> Unit,
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit
) {
    var showAttachMenu by remember { mutableStateOf(false) }
    
    Column {
        // Attach menu
        if (showAttachMenu) {
            AttachMenu(
                onPhoto = onAttachmentClick,
                onFile = onFileClick,
                onCamera = onCameraClick,
                onDismiss = { showAttachMenu = false }
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Attach button
            IconButton(onClick = { showAttachMenu = !showAttachMenu }) {
                Icon(
                    imageVector = if (showAttachMenu) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Attach",
                    tint = Primary
                )
            }
            
            // Emoji button
            IconButton(onClick = onEmojiToggle) {
                Icon(
                    imageVector = Icons.Default.EmojiEmotions,
                    contentDescription = "Emoji",
                    tint = Primary
                )
            }
            
            // Text field or voice
            if (messageText.isEmpty()) {
                // Voice record button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Surface)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { 
                                    onStartRecording()
                                    tryAwaitRelease()
                                    onStopRecording()
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Hold to record voice",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            } else {
                // Text input
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageChange,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message") },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 5,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Surface,
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = Divider,
                        focusedTextColor = Color.White
                    )
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Send button
            if (messageText.isNotBlank()) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Primary)
                        .clickable { onSend() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun AttachMenu(
    onPhoto: () -> Unit,
    onFile: () -> Unit,
    onCamera: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AttachOption(
                icon = Icons.Default.Photo,
                label = "Gallery",
                color = Primary,
                onClick = onPhoto
            )
            AttachOption(
                icon = Icons.Default.Camera,
                label = "Camera",
                color = Success,
                onClick = onCamera
            )
            AttachOption(
                icon = Icons.Default.InsertDriveFile,
                label = "File",
                color = Accent,
                onClick = onFile
            )
            AttachOption(
                icon = Icons.Default.LocationOn,
                label = "Location",
                color = Error,
                onClick = { /* Location */ }
            )
        }
    }
}

@Composable
fun AttachOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}

@Composable
fun EmojiPicker(
    onEmojiSelected: (String) -> Unit,
    onClose: () -> Unit
) {
    // Simplified emoji picker
    val emojis = listOf("😀", "😂", "🥰", "😎", "🤔", "👍", "👎", "🔥", "❤️", "🎉")
    
    Card(
        colors = CardDefaults.cardColors(containerColor = Surface),
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Emoji",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }
            
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                emojis.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 28.sp,
                        modifier = Modifier.clickable { onEmojiSelected(emoji) }
                    )
                }
            }
        }
    }
}

// Simple FlowRow implementation
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    // Simplified - use actual FlowRow from accompanist in production
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement
    ) {
        content()
    }
}

enum class MessageType {
    TEXT, IMAGE, VIDEO, FILE, VOICE, LOCATION
}

private fun getDisplayName(context: android.content.Context, uri: Uri): String? {
    // Implementation would query ContentResolver
    return "file_${System.currentTimeMillis()}"
}
