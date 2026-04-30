package com.p2pmessenger.ui.screens.chat

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.p2pmessenger.data.model.Message
import com.p2pmessenger.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    peerId: String,
    peerName: String = "Peer",
    onBack: () -> Unit,
    onVoiceCall: () -> Unit = {},
    onVideoCall: () -> Unit = {},
    onSendMessage: (String) -> Unit = {},
    onSendFile: (Uri, String) -> Unit = { _, _ -> }
) {
    var messageText by remember { mutableStateOf("") }
    val messages = remember { mutableListOf<Message>() }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val fileName = getFileName(it)
            onSendFile(it, fileName)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Peer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            "online",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onVoiceCall) {
                        Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = Primary)
                    }
                    IconButton(onClick = onVideoCall) {
                        Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = Primary)
                    }
                    IconButton(onClick = { /* More options */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Background,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Messages list
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(vertical = 8.dp),
                reverseLayout = true
            ) {
                items(messages.reversed()) { message ->
                    MessageBubble(message = message)
                }
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
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "End-to-end encrypted",
                        fontSize = 12.sp,
                        color = Success
                    )
                }
            }
            
            // Input field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                    Icon(Icons.Default.AttachFile, contentDescription = "Attach file", tint = Primary)
                }
                
                // Voice message button
                IconButton(onClick = { /* Voice recording */ }) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice message", tint = Primary)
                }
                
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message…") },
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        containerColor = Surface,
                        focusedBorderColor = SurfaceVariant,
                        unfocusedBorderColor = Divider
                    )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Orange send button like SoundCloud
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (messageText.isNotBlank()) Primary else SurfaceVariant)
                        .clickable(enabled = messageText.isNotBlank()) {
                            if (messageText.isNotBlank()) {
                                coroutineScope.launch {
                                    onSendMessage(messageText)
                                    messageText = ""
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (message.isOutgoing) Arrangement.End else Arrangement.Start
    ) {
        Column(
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Card(
                shape = if (message.isOutgoing) {
                    RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
                } else {
                    RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
                },
                colors = CardDefaults.cardColors(
                    containerColor = if (message.isOutgoing) Primary else SurfaceVariant
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.clickable { }
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = message.content,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                }
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = if (message.isOutgoing) Arrangement.End else Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = timeFormat.format(Date(message.timestamp)),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                
                if (message.isOutgoing) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = when (message.status) {
                            com.p2pmessenger.data.model.MessageStatus.SENT -> Icons.Default.Done
                            com.p2pmessenger.data.model.MessageStatus.DELIVERED -> Icons.Default.DoneAll
                            com.p2pmessenger.data.model.MessageStatus.READ -> Icons.Default.DoneAll
                            com.p2pmessenger.data.model.MessageStatus.FAILED -> Icons.Default.Error
                            com.p2pmessenger.data.model.MessageStatus.SENDING -> Icons.Default.Schedule
                        },
                        contentDescription = null,
                        tint = if (message.status == com.p2pmessenger.data.model.MessageStatus.READ) 
                            Primary else TextSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
