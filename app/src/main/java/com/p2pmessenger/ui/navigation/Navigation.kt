package com.p2pmessenger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.p2pmessenger.ui.screens.auth.AuthScreen
import com.p2pmessenger.ui.screens.call.CallScreen
import com.p2pmessenger.ui.screens.chat.AdvancedChatScreen
import com.p2pmessenger.ui.screens.chat.GroupChatScreen
import com.p2pmessenger.ui.screens.contacts.ContactsScreen
import com.p2pmessenger.ui.screens.chatlist.ChatListScreen
import com.p2pmessenger.ui.screens.qrcode.QRCodeScreen
import com.p2pmessenger.ui.screens.settings.SettingsScreen
import com.p2pmessenger.p2p.WebRTCManager

sealed class Screen(val route: String) {
    object Auth : Screen("auth")
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat/{peerId}") {
        fun createRoute(peerId: String) = "chat/$peerId"
    }
    object GroupChat : Screen("group/{groupId}") {
        fun createRoute(groupId: String) = "group/$groupId"
    }
    object Contacts : Screen("contacts")
    object QRCode : Screen("qrcode")
    object Settings : Screen("settings")
    object Call : Screen("call/{peerId}/{isVideo}") {
        fun createRoute(peerId: String, isVideo: Boolean) = "call/$peerId/$isVideo"
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    webRTCManager: WebRTCManager,
    startDestination: String = Screen.Auth.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Auth Screen
        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.ChatList.route) {
                        popUpTo(Screen.Auth.route) { inclusive = true }
                    }
                }
            )
        }
        
        // Chat List Screen
        composable(Screen.ChatList.route) {
            ChatListScreen(
                onChatClick = { peerId ->
                    navController.navigate(Screen.Chat.createRoute(peerId))
                },
                onQRCodeClick = {
                    navController.navigate(Screen.QRCode.route)
                },
                onContactsClick = {
                    navController.navigate(Screen.Contacts.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        // Chat Screen
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("peerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val peerId = backStackEntry.arguments?.getString("peerId") ?: ""
            val callState by webRTCManager.callState.collectAsState()
            
            AdvancedChatScreen(
                peerId = peerId,
                peerName = peerId.take(8), // Would get from repository
                onBack = { navController.popBackStack() },
                onVoiceCall = {
                    navController.navigate(Screen.Call.createRoute(peerId, false))
                },
                onVideoCall = {
                    navController.navigate(Screen.Call.createRoute(peerId, true))
                },
                onSendMessage = { content, replyToId ->
                    webRTCManager.sendMessage(content)
                },
                onSendFile = { uri, fileName, type ->
                    webRTCManager.sendFile(uri, fileName)
                },
                onDeleteMessage = { /* Delete from DB */ },
                onReplyMessage = { /* Set reply */ },
                onSearch = { /* Search */ }
            )
        }
        
        // Group Chat Screen
        composable(
            route = Screen.GroupChat.route,
            arguments = listOf(navArgument("groupId") { type = NavType.StringType })
        ) { backStackEntry ->
            val groupId = backStackEntry.arguments?.getString("groupId") ?: ""
            // Would load group from repository
            /*
            GroupChatScreen(
                group = group,
                members = members,
                onBack = { navController.popBackStack() },
                onAddMember = { /* Add member */ },
                onLeaveGroup = { /* Leave */ },
                onDeleteGroup = { /* Delete */ },
                onMemberClick = { memberId ->
                    navController.navigate(Screen.Chat.createRoute(memberId))
                },
                onEditGroup = { /* Edit */ },
                currentUserRole = GroupRole.MEMBER
            )
            */
        }
        
        // Contacts Screen
        composable(Screen.Contacts.route) {
            ContactsScreen(
                onBack = { navController.popBackStack() },
                onContactClick = { peerId ->
                    navController.navigate(Screen.Chat.createRoute(peerId))
                },
                onAddContact = { /* Show add dialog */ }
            )
        }
        
        // QR Code Screen
        composable(Screen.QRCode.route) {
            QRCodeScreen(
                onBack = { navController.popBackStack() }
            )
        }
        
        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Call Screen
        composable(
            route = Screen.Call.route,
            arguments = listOf(
                navArgument("peerId") { type = NavType.StringType },
                navArgument("isVideo") { type = NavType.BoolType }
            )
        ) { backStackEntry ->
            val peerId = backStackEntry.arguments?.getString("peerId") ?: ""
            val isVideo = backStackEntry.arguments?.getBoolean("isVideo") ?: false
            val callState by webRTCManager.callState.collectAsState()
            
            CallScreen(
                peerName = peerId.take(8),
                isVideo = isVideo,
                callState = callState,
                onAccept = { webRTCManager.answerCall(true, isVideo) },
                onReject = { 
                    webRTCManager.answerCall(false, isVideo)
                    navController.popBackStack()
                },
                onEnd = {
                    webRTCManager.endCall()
                    navController.popBackStack()
                },
                onToggleMute = { /* Toggle mute */ },
                onToggleSpeaker = { /* Toggle speaker */ },
                onToggleVideo = { /* Toggle video */ },
                onFlipCamera = { /* Flip camera */ }
            )
        }
    }
}
