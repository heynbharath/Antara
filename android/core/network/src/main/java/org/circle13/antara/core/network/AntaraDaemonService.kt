package org.circle13.antara.core.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class AntaraDaemonService : Service() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    private var wakeLock: PowerManager.WakeLock? = null
    private var discoveryService: DiscoveryService? = null
    private var connectionManager: ConnectionManagerImpl? = null

    private val channelId = "antara_mesh_daemon"
    private val notificationId = 1313

    override fun onCreate() {
        super.onCreate()
        
        // Initialize wakelock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Antara::MeshWakelock")

        // Initialize BLE Adapter
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        discoveryService = DiscoveryServiceImpl(bluetoothManager.adapter)
        connectionManager = ConnectionManagerImpl()

        createNotificationChannel()
        startForegroundService()
        
        // Start scanning in the background
        startMeshNetwork()
    }

    private fun startMeshNetwork() {
        // Start background BLE advertising
        val mockToken = ByteArray(16) { 0 } // Ephemeral Discovery Token placeholder
        discoveryService?.startAdvertising(mockToken, capabilities = 0)

        // Start scanning and pipe matched peers to connection manager
        discoveryService?.startScanning()?.onEach { event ->
            // Matched a peer beacon! Negotiate connection state
            connectionManager?.updateLinkState(event.peerAddressHash, ConnectionState.CONNECTED_BLE)
        }?.launchIn(scope)
    }

    private fun startForegroundService() {
        val notification = buildNotification("Antara Network is active")
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                notificationId, 
                notification, 
                ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
            )
        } else {
            startForeground(notificationId, notification)
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Antara Mesh Network")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Antara Mesh Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }

    // Helper method for transient locks during Wi-Fi direct sync
    fun acquireTransientLock() {
        wakeLock?.acquire(10 * 60 * 1000L /* 10 minutes max */)
    }

    fun releaseTransientLock() {
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        discoveryService?.stopAdvertising()
        discoveryService?.stopScanning()
        releaseTransientLock()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
