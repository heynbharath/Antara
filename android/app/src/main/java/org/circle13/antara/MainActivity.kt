package org.circle13.antara

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.circle13.antara.core.database.MessageEntity
import org.circle13.antara.core.database.NeighborEntity
import org.circle13.antara.core.network.AntaraDaemonService
import org.circle13.antara.feature.chat.ChatScreen
import org.circle13.antara.feature.dashboard.DashboardScreen
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        startMeshDaemonService()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()
        startMeshDaemonService()

        setContent {
            AntaraApp()
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
        } else {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missingPermissions = permissionsToRequest.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(missingPermissions.toTypedArray())
        }
    }

    private fun startMeshDaemonService() {
        try {
            val intent = Intent(this, AntaraDaemonService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(this, intent)
            } else {
                startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

sealed interface Screen {
    data object Dashboard : Screen
    data class Chat(val neighbor: NeighborEntity) : Screen
}

@Composable
fun AntaraApp() {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Dashboard) }

    // Seed mock neighbor nodes
    val neighbors = remember {
        mutableStateListOf(
            NeighborEntity(
                nodeId = "a1b2c3d4e5f678901234567890abcdef",
                rssi = -64,
                batteryLevel = 88,
                trustScore = 0.95,
                queueDepth = 0.0,
                lastSeen = System.currentTimeMillis()
            ),
            NeighborEntity(
                nodeId = "f9e8d7c6b5a432109876543210fedcba",
                rssi = -78,
                batteryLevel = 45,
                trustScore = 0.82,
                queueDepth = 0.0,
                lastSeen = System.currentTimeMillis() - 12000
            )
        )
    }

    // Messages database mock per thread
    val threadMessages = remember {
        mutableMapOf<String, MutableList<MessageEntity>>(
            "a1b2c3d4e5f678901234567890abcdef" to mutableStateListOf(
                MessageEntity(
                    messageId = UUID.randomUUID().toString(),
                    threadId = "a1b2c3d4e5f678901234567890abcdef",
                    timestamp = System.currentTimeMillis() - 60000,
                    body = "Node active in mesh domain.",
                    vectorClockJson = "{}",
                    parentsJson = "[]",
                    senderIdentity = "a1b2c3d4e5f678901234567890abcdef"
                )
            )
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF000000)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Navigation Bar when inside Chat
            when (val screen = currentScreen) {
                is Screen.Chat -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF0A0A0C))
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "← Back",
                            color = Color(0xFF0A84FF),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { currentScreen = Screen.Dashboard }
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Node [${screen.neighbor.nodeId.take(8)}]",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Screen.Dashboard -> { /* Dashboard has its own header */ }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (val screen = currentScreen) {
                    is Screen.Dashboard -> {
                        DashboardScreen(
                            neighbors = neighbors,
                            onSelectPeer = { neighbor ->
                                currentScreen = Screen.Chat(neighbor)
                            }
                        )
                    }
                    is Screen.Chat -> {
                        val messagesList = threadMessages.getOrPut(screen.neighbor.nodeId) {
                            mutableStateListOf()
                        }
                        ChatScreen(
                            threadTitle = "Encrypted Mesh Channel",
                            messages = messagesList,
                            onSendMessage = { newText ->
                                val newMessage = MessageEntity(
                                    messageId = UUID.randomUUID().toString(),
                                    threadId = screen.neighbor.nodeId,
                                    timestamp = System.currentTimeMillis(),
                                    body = newText,
                                    vectorClockJson = "{}",
                                    parentsJson = "[]",
                                    senderIdentity = "local"
                                )
                                messagesList.add(newMessage)
                            }
                        )
                    }
                }
            }
        }
    }
}
