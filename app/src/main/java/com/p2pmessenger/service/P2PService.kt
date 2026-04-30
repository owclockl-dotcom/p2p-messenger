package com.p2pmessenger.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.p2pmessenger.MainActivity
import com.p2pmessenger.R
import com.p2pmessenger.p2p.WebRTCManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class P2PService : Service() {
    
    @Inject
    lateinit var webRTCManager: WebRTCManager
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val binder = LocalBinder()
    
    private val CHANNEL_ID = "P2PMessenger"
    private val NOTIFICATION_ID = 1
    
    inner class LocalBinder : Binder() {
        fun getService(): P2PService = this@P2PService
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification())
        
        // Listen for incoming messages
        serviceScope.launch {
            webRTCManager.receivedMessages.collectLatest { message ->
                // Handle incoming message
                // Show notification, save to database, etc.
            }
        }
        
        serviceScope.launch {
            webRTCManager.connectionState.collectLatest { state ->
                when (state) {
                    WebRTCManager.ConnectionState.Connected -> {
                        // Update notification to show connected
                    }
                    WebRTCManager.ConnectionState.Disconnected -> {
                        // Update notification to show disconnected
                    }
                    else -> {}
                }
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder {
        return binder
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        webRTCManager.disconnect()
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "P2P Messenger Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Keeps P2P connection alive"
                setShowBadge(false)
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("P2P Messenger")
            .setContentText("P2P service running")
            .setSmallIcon(R.drawable.ic_notification_icon)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
