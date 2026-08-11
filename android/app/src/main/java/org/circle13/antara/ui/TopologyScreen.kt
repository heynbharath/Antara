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

@Composable
fun TopologyScreen(
    localNodeId: String,
    activePeersCount: Int
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Mesh Network Topology",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Live AODV Routing Graph & Physical Radio Hops",
                color = Color(0xFF8E8E93),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Visual Graph Topology Canvas / Diagram Container
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
                    text = "ACTIVE MESH TOPOLOGY MAP",
                    color = Color(0xFFD4AF37),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Topology Graph Representation
                // Top Level: Local Node
                NodeBubble(
                    title = "YOU (Local Node)",
                    subtitle = "ed25519:${localNodeId.take(8)}",
                    isLocal = true
                )

                Spacer(modifier = Modifier.height(12.dp))
                LinkLine(text = "1-Hop Direct Radio (BLE / Wi-Fi Direct)")
                Spacer(modifier = Modifier.height(12.dp))

                // 1-Hop Neighbors Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    NodeBubble(title = "Node Alpha", subtitle = "BLE • -64 dBm", isLocal = false)
                    NodeBubble(title = "Node Beta", subtitle = "Wi-Fi Direct • -72 dBm", isLocal = false)
                }

                Spacer(modifier = Modifier.height(12.dp))
                LinkLine(text = "2-Hop Store & Forward Relay")
                Spacer(modifier = Modifier.height(12.dp))

                // 2-Hop Relay Destination
                NodeBubble(
                    title = "Library Gateway Relay",
                    subtitle = "Hop 2 • DTN Buffer Queue: 3 packets",
                    isLocal = false
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Active Routing Table Entries
        item {
            Text(
                text = "Active AODV Routes",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            RouteCard(
                destination = "Library Relay [node_9a12]",
                nextHop = "Node Alpha [node_a1b2]",
                hops = 2,
                metric = "LQI: 95% • Delay 14ms"
            )
            Spacer(modifier = Modifier.height(8.dp))

            RouteCard(
                destination = "Eng Hall [node_f9e8]",
                nextHop = "Node Beta [node_f9e8]",
                hops = 1,
                metric = "LQI: 82% • Direct Link"
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // DTN Buffer Overview
        item {
            Text(
                text = "Delay-Tolerant Network (DTN) Storage",
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
                    Text(text = "Store-and-Forward Buffer", color = Color.White, fontSize = 14.sp)
                    Text(text = "3 / 50 Packets", color = Color(0xFFD4AF37), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Packets queued for nodes currently out of physical radio range. Transmits automatically when a proxy node moves into range.",
                    color = Color(0xFF8E8E93),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun NodeBubble(
    title: String,
    subtitle: String,
    isLocal: Boolean
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
private fun LinkLine(text: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "│",
            color = Color(0xFFD4AF37),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = text,
            color = Color(0xFF8E8E93),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = "▼",
            color = Color(0xFFD4AF37),
            fontSize = 10.sp
        )
    }
}

@Composable
private fun RouteCard(
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
            Text(text = "Next Hop: $nextHop • $hops Hops", color = Color(0xFF8E8E93), fontSize = 12.sp)
        }
        Text(text = metric, color = Color(0xFF34C759), fontSize = 11.sp, fontFamily = FontFamily.Monospace)
    }
}
