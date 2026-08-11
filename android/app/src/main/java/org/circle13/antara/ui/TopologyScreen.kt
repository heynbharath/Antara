package org.circle13.antara.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.circle13.antara.core.database.NeighborEntity

@Composable
fun TopologyScreen(
    localNodeId: String,
    neighbors: List<NeighborEntity>,
    dtnQueueCount: Int
) {
    val verifiedContacts = neighbors.filter { it.isVerifiedContact }
    val relayNodes = neighbors.filter { !it.isVerifiedContact }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "AODV Mesh Topology Map",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dynamic Physical Radio Routing & Store-and-Forward Hops",
                color = Color(0xFF8E8E93),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Active Mesh Topology Diagram
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0A0C), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "AODV ROUTING GRAPH MATRIX",
                    color = Color(0xFFD4AF37),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Local Node Node
                TopologyNodeCard(
                    title = "LOCAL NODE (YOU)",
                    subtitle = "node_${localNodeId.take(8)}",
                    isLocal = true,
                    tag = "MASTER"
                )

                Spacer(modifier = Modifier.height(12.dp))
                TopologyLinkLine(text = "Physical Radio Discovery (BLE / Wi-Fi Direct)")
                Spacer(modifier = Modifier.height(12.dp))

                // 1-Hop Active Peers
                if (neighbors.isEmpty()) {
                    Text(
                        text = "Scanning for physical radio hops in range...",
                        color = Color(0xFF8E8E93),
                        fontSize = 12.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        neighbors.take(2).forEach { peer ->
                            TopologyNodeCard(
                                title = if (peer.isVerifiedContact) peer.username else "Relay [${peer.nodeId.take(4)}]",
                                subtitle = "${peer.connectionType} • ${peer.rssi} dBm",
                                isLocal = false,
                                tag = if (peer.isVerifiedContact) "VERIFIED" else "RELAY"
                            )
                        }
                    }
                }

                if (dtnQueueCount > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    TopologyLinkLine(text = "Store-and-Forward DTN Buffer Pipeline")
                    Spacer(modifier = Modifier.height(12.dp))

                    TopologyNodeCard(
                        title = "DTN Storage Proxy",
                        subtitle = "$dtnQueueCount Packets Queued in Local SQLite Buffer",
                        isLocal = false,
                        tag = "BUFFER"
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Active Routing Table Entries
        item {
            Text(
                text = "AODV Route Discovery Table",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (neighbors.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Text(text = "No active routing paths resolved. Radios actively broadcasting presence...", color = Color(0xFF8E8E93), fontSize = 13.sp)
                }
            } else {
                neighbors.forEach { peer ->
                    AodvRouteRow(
                        destination = if (peer.isVerifiedContact) "${peer.fullName} (@${peer.username})" else "Relay Hop node_${peer.nodeId.take(8)}",
                        nextHop = "Direct ${peer.connectionType}",
                        hops = 1,
                        metric = "LQI: ${(peer.trustScore * 100).toInt()}% • RSSI: ${peer.rssi} dBm"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Delay-Tolerant Network (DTN) Storage Buffer
        item {
            Text(
                text = "Delay-Tolerant Network (DTN) Pipeline",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Store-and-Forward Queue", color = Color.White, fontSize = 14.sp)
                    Text(text = "$dtnQueueCount Packets", color = Color(0xFFD4AF37), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Packets targeting offline nodes are held securely in local SQLCipher storage until a routing node moves within physical radio range.",
                    color = Color(0xFF8E8E93),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun TopologyNodeCard(
    title: String,
    subtitle: String,
    isLocal: Boolean,
    tag: String
) {
    Column(
        modifier = Modifier
            .background(
                color = if (isLocal) Color(0xFF1C1C1E) else Color(0xFF0A0A0C),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 1.dp,
                color = if (isLocal) Color(0xFFD4AF37) else Color(0xFF2C2C2E),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isLocal) Color(0xFFD4AF37) else Color(0xFF34C759))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            color = Color(0xFF8E8E93),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun TopologyLinkLine(text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "│", color = Color(0xFFD4AF37), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(text = text, color = Color(0xFF8E8E93), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
        Text(text = "▼", color = Color(0xFFD4AF37), fontSize = 10.sp)
    }
}

@Composable
private fun AodvRouteRow(
    destination: String,
    nextHop: String,
    hops: Int,
    metric: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = destination, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Route: $nextHop • $hops Hops", color = Color(0xFF8E8E93), fontSize = 12.sp)
        }
        Text(text = metric, color = Color(0xFF34C759), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}
