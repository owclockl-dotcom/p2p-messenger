package com.p2pmessenger.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.p2pmessenger.ui.screens.chat.ChatScreen
import com.p2pmessenger.ui.screens.chatlist.ChatListScreen
import com.p2pmessenger.ui.screens.qrcode.QRCodeScreen
import com.p2pmessenger.ui.screens.settings.SettingsScreen

sealed class Screen(val route: String) {
    object ChatList : Screen("chat_list")
    object Chat : Screen("chat/{peerId}") {
        fun createRoute(peerId: String) = "chat/$peerId"
    }
    object QRCode : Screen("qr_code")
    object Settings : Screen("settings")
}

@Composable
fun P2PNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.ChatList.route
    ) {
        composable(Screen.ChatList.route) {
            ChatListScreen(
                onChatClick = { peerId ->
                    navController.navigate(Screen.Chat.createRoute(peerId))
                },
                onQRCodeClick = {
                    navController.navigate(Screen.QRCode.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Chat.route) { backStackEntry ->
            val peerId = backStackEntry.arguments?.getString("peerId") ?: ""
            ChatScreen(
                peerId = peerId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.QRCode.route) {
            QRCodeScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
