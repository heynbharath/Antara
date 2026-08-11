package org.circle13.antara

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import org.circle13.antara.core.database.MessageEntity
import org.circle13.antara.core.database.NeighborEntity
import org.circle13.antara.core.network.AntaraDaemonService
import org.circle13.antara.feature.chat.ChatScreen
import org.circle13.antara.feature.dashboard.DashboardScreen
import org.circle13.antara.ui.IdentityScreen
import org.circle13.antara.ui.OnboardingScreen
import org.circle13.antara.ui.TelemetryScreen
import org.circle13.antara.ui.TopologyScreen
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
            AntaraMainContainer(
                onRequestPermissions = { checkAndRequestPermissions() }
            )
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

enum class NavigationTab {
    NODES,
    MESSAGES,
    TOPOLOGY,
    TELEMETRY,
    IDENTITY
}

@Composable
fun AntaraMainContainer(
    onRequestPermissions: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("antara_prefs", Context.MODE_PRIVATE) }

    var isOnboardingCompleted by remember {
        mutableStateOf(prefs.getBoolean("onboarding_completed", false))
    }
    var username by remember {
        mutableStateOf(prefs.getString("username", "Student_Node") ?: "Student_Node")
    }
    var nodeId by remember {
        mutableStateOf(prefs.getString("node_id", "a1b2c3d4e5f678901234567890abcdef") ?: "a1b2c3d4e5f678901234567890abcdef")
    }

    if (!isOnboardingCompleted) {
        OnboardingScreen(
            onRequestPermissions = onRequestPermissions,
            onCompleteOnboarding = { newUsername, newNodeId ->
                username = newUsername
                nodeId = newNodeId
                prefs.edit()
                    .putBoolean("onboarding_completed", true)
                    .putString("username", newUsername)
                    .putString("node_id", newNodeId)
                    .apply()
                isOnboardingCompleted = true
            }
        )
    } else {
        AntaraAppContent(
            username = username,
            nodeId = nodeId,
            onResetIdentity = {
                prefs.edit().clear().apply()
                isOnboardingCompleted = false
            }
        )
    }
}

@Composable
fun AntaraAppContent(
    username: String,
    nodeId: String,
    onResetIdentity: () -> Unit
) {
    var activeTab by remember { mutableStateOf(NavigationTab.NODES) }
    var activeChatPeer by remember { mutableStateOf<NeighborEntity?>(null) }

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
            ),
            NeighborEntity(
                nodeId = "9a8b7c6d5e4f32100123456789abcdef",
                rssi = -55,
                batteryLevel = 92,
                trustScore = 0.99,
                queueDepth = 0.0,
                lastSeen = System.currentTimeMillis() - 5000
            )
        )
    }

    // Messages storage per thread
    val threadMessages = remember {
        mutableMapOf<String, MutableList<MessageEntity>>(
            "a1b2c3d4e5f678901234567890abcdef" to mutableStateListOf(
                MessageEntity(
                    messageId = UUID.randomUUID().toString(),
                    threadId = "a1b2c3d4e5f678901234567890abcdef",
                    timestamp = System.currentTimeMillis() - 60000,
                    body = "Node active in campus mesh domain.",
                    vectorClockJson = "{}",
                    parentsJson = "[]",
                    senderIdentity = "a1b2c3d4e5f678901234567890abcdef"
                )
            ),
            "9a8b7c6d5e4f32100123456789abcdef" to mutableStateListOf(
                MessageEntity(
                    messageId = UUID.randomUUID().toString(),
                    threadId = "9a8b7c6d5e4f32100123456789abcdef",
                    timestamp = System.currentTimeMillis() - 30000,
                    body = "Library relay node connected via BLE.",
                    vectorClockJson = "{}",
                    parentsJson = "[]",
                    senderIdentity = "9a8b7c6d5e4f32100123456789abcdef"
                )
            )
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF000000)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Global Status Banner
            TopStatusBanner(
                username = username,
                nodeId = nodeId,
                activePeersCount = neighbors.size
            )

            // Back button when inside Chat view
            if (activeChatPeer != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0A0C))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "← Back to Mesh",
                        color = Color(0xFFD4AF37),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { activeChatPeer = null }
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Node [${activeChatPeer?.nodeId?.take(8)}]",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Main Content Body
            Box(modifier = Modifier.weight(1f)) {
                if (activeChatPeer != null) {
                    val peer = activeChatPeer!!
                    val messagesList = threadMessages.getOrPut(peer.nodeId) { mutableStateListOf() }
                    ChatScreen(
                        threadTitle = "Node [${peer.nodeId.take(8)}]",
                        messages = messagesList,
                        onSendMessage = { newText ->
                            val newMessage = MessageEntity(
                                messageId = UUID.randomUUID().toString(),
                                threadId = peer.nodeId,
                                timestamp = System.currentTimeMillis(),
                                body = newText,
                                vectorClockJson = "{}",
                                parentsJson = "[]",
                                senderIdentity = "local"
                            )
                            messagesList.add(newMessage)
                        }
                    )
                } else {
                    when (activeTab) {
                        NavigationTab.NODES -> DashboardScreen(
                            neighbors = neighbors,
                            onSelectPeer = { selectedPeer -> activeChatPeer = selectedPeer }
                        )
                        NavigationTab.MESSAGES -> DashboardScreen(
                            neighbors = neighbors,
                            onSelectPeer = { selectedPeer -> activeChatPeer = selectedPeer }
                        )
                        NavigationTab.TOPOLOGY -> TopologyScreen(
                            localNodeId = nodeId,
                            activePeersCount = neighbors.size
                        )
                        NavigationTab.TELEMETRY -> TelemetryScreen()
                        NavigationTab.IDENTITY -> IdentityScreen(
                            username = username,
                            nodeId = nodeId,
                            onResetIdentity = onResetIdentity
                        )
                    }
                }
            }

            // Bottom Navigation Bar
            if (activeChatPeer == null) {
                BottomNavigationBar(
                    activeTab = activeTab,
                    onTabSelected = { activeTab = it }
                )
            }
        }
    }
}

@Composable
fun TopStatusBanner(
    username: String,
    nodeId: String,
    activePeersCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0C))
            .border(1.dp, Color(0xFF1C1C1E))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF34C759))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "ANTARA MESH",
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$username • ",
                color = Color(0xFF8E8E93),
                fontSize = 12.sp
            )
            Text(
                text = "ed25519:${nodeId.take(6)}",
                color = Color(0xFFD4AF37),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    activeTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0C))
            .border(1.dp, Color(0xFF1C1C1E))
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavTabItem(
            label = "Nodes",
            icon = "📡",
            isSelected = activeTab == NavigationTab.NODES,
            onClick = { onTabSelected(NavigationTab.NODES) }
        )
        NavTabItem(
            label = "Messages",
            icon = "💬",
            isSelected = activeTab == NavigationTab.MESSAGES,
            onClick = { onTabSelected(NavigationTab.MESSAGES) }
        )
        NavTabItem(
            label = "Topology",
            icon = "🌐",
            isSelected = activeTab == NavigationTab.TOPOLOGY,
            onClick = { onTabSelected(NavigationTab.TOPOLOGY) }
        )
        NavTabItem(
            label = "Telemetry",
            icon = "⚡",
            isSelected = activeTab == NavigationTab.TELEMETRY,
            onClick = { onTabSelected(NavigationTab.TELEMETRY) }
        )
        NavTabItem(
            label = "Identity",
            icon = "🔐",
            isSelected = activeTab == NavigationTab.IDENTITY,
            onClick = { onTabSelected(NavigationTab.IDENTITY) }
        )
    }
}

@Composable
fun NavTabItem(
    label: String,
    icon: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = icon,
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            color = if (isSelected) Color(0xFFD4AF37) else Color(0xFF8E8E93),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
        )
    }
}
