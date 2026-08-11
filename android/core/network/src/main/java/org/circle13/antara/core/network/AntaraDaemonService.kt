package org.circle13.antara.core.network

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
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
        
        try {
            // Initialize wakelock safely
            val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
            wakeLock = powerManager?.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Antara::MeshWakelock")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            // Initialize BLE Adapter safely
            val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            discoveryService = DiscoveryServiceImpl(bluetoothManager?.adapter)
            connectionManager = ConnectionManagerImpl()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForegroundServiceSafely()
        startMeshNetworkSafely()
        return START_STICKY
    }

    private fun startMeshNetworkSafely() {
        try {
            val mockToken = ByteArray(16) { 0 }
            discoveryService?.startAdvertising(mockToken, capabilities = 0)

            discoveryService?.startScanning()?.onEach { event ->
                connectionManager?.updateLinkState(event.peerAddressHash, ConnectionState.CONNECTED_BLE)
            }?.launchIn(scope)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun startForegroundServiceSafely() {
        try {
            val notification = buildNotification("Antara Mesh Network Engine is active")
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                try {
                    startForeground(
                        notificationId, 
                        notification, 
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                    )
                } catch (e: Exception) {
                    startForeground(notificationId, notification)
                }
            } else {
                startForeground(notificationId, notification)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun buildNotification(text: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Antara Mesh Protocol")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_sys_data_bluetooth)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val serviceChannel = NotificationChannel(
                    channelId,
                    "Antara Mesh Channel",
                    NotificationManager.IMPORTANCE_LOW
                )
                val manager = getSystemService(NotificationManager::class.java)
                manager?.createNotificationChannel(serviceChannel)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun acquireTransientLock() {
        try {
            wakeLock?.acquire(10 * 60 * 1000L)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun releaseTransientLock() {
        try {
            if (wakeLock?.isHeld == true) {
                wakeLock?.release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            discoveryService?.stopAdvertising()
            discoveryService?.stopScanning()
            releaseTransientLock()
            scope.cancel()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
