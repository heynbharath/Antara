package org.circle13.antara

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.circle13.antara.core.database.AntaraRoomDatabase
import org.circle13.antara.core.database.MessageEntity
import org.circle13.antara.core.database.NeighborEntity
import org.circle13.antara.core.network.AntaraDaemonService
import org.circle13.antara.core.network.UserIdentity
import org.circle13.antara.feature.chat.ChatScreen
import org.circle13.antara.feature.dashboard.DashboardScreen
import org.circle13.antara.ui.IdentityScreen
import org.circle13.antara.ui.OnboardingScreen
import org.circle13.antara.ui.QrPairingDialog
import org.circle13.antara.ui.TelemetryScreen
import org.circle13.antara.ui.TopologyScreen
import java.util.UUID

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ ->
        val btConnectGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        } else true
        
        if (btConnectGranted) {
            startMeshDaemonServiceSafely()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AntaraMainContainer(
                onRequestPermissions = { checkAndRequestPermissions() },
                onRequestBatteryOptimizationExemption = { requestBatteryOptimizationExemption() }
            )
        }
    }

    private fun checkAndRequestPermissions() {
        try {
            val permissionsToRequest = mutableListOf<String>()

            // Always request Location for physical BLE & Wi-Fi Direct scanning
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissionsToRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)

            // Bluetooth permissions for Android 12+ (API 31+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN)
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT)
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            }

            // Wi-Fi Direct & Notification permissions for Android 13+ (API 33+)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissionsToRequest.add(Manifest.permission.NEARBY_WIFI_DEVICES)
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            val missingPermissions = permissionsToRequest.filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }

            if (missingPermissions.isNotEmpty()) {
                requestPermissionLauncher.launch(missingPermissions.toTypedArray())
            } else {
                startMeshDaemonServiceSafely()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun requestBatteryOptimizationExemption() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
                if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(packageName)) {
                    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
            } catch (e: Exception) {
                try {
                    val fallbackIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(fallbackIntent)
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun startMeshDaemonServiceSafely() {
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
    onRequestPermissions: () -> Unit,
    onRequestBatteryOptimizationExemption: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("antara_prefs_v2", Context.MODE_PRIVATE) }

    var isOnboardingCompleted by remember {
        mutableStateOf(prefs.getBoolean("onboarding_completed", false))
    }
    var fullName by remember {
        mutableStateOf(prefs.getString("full_name", "Alex Miller") ?: "Alex Miller")
    }
    var username by remember {
        mutableStateOf(prefs.getString("username", "alex_m") ?: "alex_m")
    }
    var nodeId by remember {
        mutableStateOf(prefs.getString("node_id", "8f3a91b2c4d5e6f7a8b9c0d1e2f3a4b5") ?: "8f3a91b2c4d5e6f7a8b9c0d1e2f3a4b5")
    }
    var publicKeyHex by remember {
        mutableStateOf(prefs.getString("public_key_hex", "") ?: "")
    }

    val userIdentity = remember(fullName, username, nodeId, publicKeyHex) {
        UserIdentity(
            nodeId = nodeId,
            publicKeyHex = publicKeyHex,
            username = username,
            fullName = fullName
        )
    }

    if (!isOnboardingCompleted) {
        OnboardingScreen(
            onRequestPermissions = onRequestPermissions,
            onCompleteOnboarding = { newFullName, newUsername, newNodeId, newPubKey ->
                fullName = newFullName
                username = newUsername
                nodeId = newNodeId
                publicKeyHex = newPubKey
                prefs.edit()
                    .putBoolean("onboarding_completed", true)
                    .putString("full_name", newFullName)
                    .putString("username", newUsername)
                    .putString("node_id", newNodeId)
                    .putString("public_key_hex", newPubKey)
                    .apply()
                isOnboardingCompleted = true
                
                // Force a clean restart to ensure all services and permissions initialize correctly from a fresh state
                val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
                if (intent != null) {
                    intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP or android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    context.startActivity(intent)
                    (context as? android.app.Activity)?.finish()
                }
            }
        )
    } else {
        LaunchedEffect(Unit) {
            onRequestPermissions()
        }
        
        AntaraAppContent(
            identity = userIdentity,
            onRequestBatteryOptimizationExemption = onRequestBatteryOptimizationExemption,
            onResetIdentity = {
                prefs.edit().clear().apply()
                isOnboardingCompleted = false
            }
        )
    }
}

@Composable
fun AntaraAppContent(
    identity: UserIdentity,
    onRequestBatteryOptimizationExemption: () -> Unit,
    onResetIdentity: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val database = remember { AntaraRoomDatabase.getDatabase(context) }

    val neighborsState by database.neighborDao().getAllNeighborsFlow().collectAsState(initial = emptyList(), context = Dispatchers.IO)
    val dtnCountState by database.dtnPacketDao().getPacketCountFlow().collectAsState(initial = 0, context = Dispatchers.IO)

    var activeTab by remember { mutableStateOf(NavigationTab.NODES) }
    var activeChatPeer by remember { mutableStateOf<NeighborEntity?>(null) }
    var isQrDialogVisible by remember { mutableStateOf(false) }

    if (isQrDialogVisible) {
        QrPairingDialog(
            myIdentity = identity,
            onDismiss = { isQrDialogVisible = false },
            onPairVerifiedContact = { newContact ->
                coroutineScope.launch {
                    try {
                        database.neighborDao().insertOrUpdateNeighbor(newContact)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF000000)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Global Status Banner
            TopStatusBanner(
                identity = identity,
                activePeersCount = neighborsState.size
            )

            // Back button when inside Chat view
            if (activeChatPeer != null) {
                val peer = activeChatPeer!!
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
                        text = if (peer.isVerifiedContact) peer.fullName else "Node [${peer.nodeId.take(8)}]",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Main Body Content
            Box(modifier = Modifier.weight(1f)) {
                if (activeChatPeer != null) {
                    val peer = activeChatPeer!!
                    val messagesState by database.messageDao().getMessagesForThreadFlow(peer.nodeId).collectAsState(initial = emptyList(), context = Dispatchers.IO)

                    ChatScreen(
                        threadTitle = if (peer.isVerifiedContact) "${peer.fullName} (@${peer.username})" else "Node [${peer.nodeId.take(8)}]",
                        messages = messagesState,
                        onSendMessage = { text ->
                            coroutineScope.launch {
                                try {
                                    val msg = MessageEntity(
                                        messageId = UUID.randomUUID().toString(),
                                        threadId = peer.nodeId,
                                        timestamp = System.currentTimeMillis(),
                                        body = text,
                                        senderIdentity = "local"
                                    )
                                    database.messageDao().insertMessage(msg)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    )
                } else {
                    when (activeTab) {
                        NavigationTab.NODES, NavigationTab.MESSAGES -> DashboardScreen(
                            neighbors = neighborsState,
                            onSelectPeer = { peer -> activeChatPeer = peer },
                            onOpenQrPairing = { isQrDialogVisible = true }
                        )
                        NavigationTab.TOPOLOGY -> TopologyScreen(
                            localNodeId = identity.nodeId,
                            neighbors = neighborsState,
                            dtnQueueCount = dtnCountState
                        )
                        NavigationTab.TELEMETRY -> TelemetryScreen(
                            neighbors = neighborsState,
                            dtnQueueCount = dtnCountState
                        )
                        NavigationTab.IDENTITY -> IdentityScreen(
                            identity = identity,
                            onOpenQrPairing = { isQrDialogVisible = true },
                            onRequestBatteryOptimizationExemption = onRequestBatteryOptimizationExemption,
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
    identity: UserIdentity,
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
                text = "@${identity.username} • ",
                color = Color(0xFF8E8E93),
                fontSize = 12.sp
            )
            Text(
                text = "node_${identity.nodeId.take(6)}",
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
