package com.p2pmessenger.ui.screens.call

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.p2pmessenger.ui.theme.*
import com.p2pmessenger.p2p.WebRTCManager

@Composable
fun CallScreen(
    peerName: String,
    isVideo: Boolean,
    callState: WebRTCManager.CallState,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    onEnd: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleVideo: () -> Unit,
    onFlipCamera: () -> Unit,
    isMuted: Boolean = false,
    isSpeakerOn: Boolean = false,
    isVideoEnabled: Boolean = true
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isVideo && callState is WebRTCManager.CallState.Connected)
                        Color.Black
                    else
                        Primary.copy(alpha = 0.15f)
                )
        )
        
        // Video preview or avatar
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            
            // Avatar circle
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(Surface)
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Peer name
            Text(
                text = peerName,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Call status
            Text(
                text = when (callState) {
                    is WebRTCManager.CallState.Idle -> "Call ended"
                    is WebRTCManager.CallState.Calling -> if (isVideo) "Video calling..." else "Calling..."
                    is WebRTCManager.CallState.Incoming -> "Incoming ${if (isVideo) "video " else ""}call"
                    is WebRTCManager.CallState.Connected -> formatDuration(0) // Would use real timer
                },
                fontSize = 16.sp,
                color = TextSecondary
            )
            
            Spacer(modifier = Modifier.weight(2f))
            
            // Call controls based on state
            when (callState) {
                is WebRTCManager.CallState.Incoming -> {
                    IncomingCallControls(
                        onAccept = onAccept,
                        onReject = onReject
                    )
                }
                is WebRTCManager.CallState.Calling -> {
                    CallingControls(onEnd = onEnd)
                }
                is WebRTCManager.CallState.Connected -> {
                    ActiveCallControls(
                        isVideo = isVideo,
                        onEnd = onEnd,
                        onToggleMute = onToggleMute,
                        onToggleSpeaker = onToggleSpeaker,
                        onToggleVideo = onToggleVideo,
                        onFlipCamera = onFlipCamera,
                        isMuted = isMuted,
                        isSpeakerOn = isSpeakerOn,
                        isVideoEnabled = isVideoEnabled
                    )
                }
                else -> {}
            }
            
            Spacer(modifier = Modifier.height(48.dp))
        }
        
        // Back button for active calls
        if (callState is WebRTCManager.CallState.Connected) {
            IconButton(
                onClick = onEnd,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "End",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun IncomingCallControls(
    onAccept: () -> Unit,
    onReject: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Reject button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Error)
                    .clickable { onReject() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "Reject",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Decline",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
        
        // Accept button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Success)
                    .clickable { onAccept() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Call,
                    contentDescription = "Accept",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Accept",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
fun CallingControls(onEnd: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Error)
                .clickable { onEnd() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CallEnd,
                contentDescription = "Cancel",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Cancel",
            color = TextSecondary,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ActiveCallControls(
    isVideo: Boolean,
    onEnd: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    onToggleVideo: () -> Unit,
    onFlipCamera: () -> Unit,
    isMuted: Boolean,
    isSpeakerOn: Boolean,
    isVideoEnabled: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Top row controls
        if (isVideo) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Flip camera
                ControlButton(
                    icon = Icons.Default.FlipCamera,
                    label = "Flip",
                    onClick = onFlipCamera,
                    isActive = false
                )
                
                // Toggle video
                ControlButton(
                    icon = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    label = "Video",
                    onClick = onToggleVideo,
                    isActive = isVideoEnabled
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Bottom row controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mute
            ControlButton(
                icon = if (isMuted) Icons.Default.MicOff else Icons.Default.Mic,
                label = if (isMuted) "Unmute" else "Mute",
                onClick = onToggleMute,
                isActive = !isMuted
            )
            
            // End call (center, larger)
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Error)
                    .clickable { onEnd() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CallEnd,
                    contentDescription = "End",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Speaker
            ControlButton(
                icon = if (isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                label = "Speaker",
                onClick = onToggleSpeaker,
                isActive = isSpeakerOn
            )
        }
    }
}

@Composable
fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    isActive: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(if (isActive) SurfaceVariant else Surface)
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Color.White else TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 12.sp
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "%02d:%02d".format(mins, secs)
}
