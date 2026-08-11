package org.circle13.antara.feature.dashboard

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.circle13.antara.core.database.NeighborEntity

@Composable
fun DashboardScreen(
    neighbors: List<NeighborEntity>,
    onSelectPeer: (NeighborEntity) -> Unit,
    onOpenQrPairing: () -> Unit,
    modifier: Modifier = Modifier
) {
    var filterTab by remember { mutableIntStateOf(0) } // 0 = Verified Friends, 1 = All Discovered Radio Hops
    var searchQuery by remember { mutableStateOf("") }

    val verifiedContacts = remember(neighbors) {
        neighbors.filter { it.isVerifiedContact }
    }

    val activeRelayNodes = remember(neighbors) {
        neighbors.filter { !it.isVerifiedContact }
    }

    val displayedList = remember(neighbors, filterTab, searchQuery) {
        val baseList = if (filterTab == 0) verifiedContacts else activeRelayNodes
        if (searchQuery.isBlank()) {
            baseList
        } else {
            baseList.filter {
                it.username.contains(searchQuery, ignoreCase = true) ||
                it.fullName.contains(searchQuery, ignoreCase = true) ||
                it.nodeId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp)
    ) {
        // Title & Pair QR Button Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Mesh Network Nodes",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                )
                Text(
                    text = "${verifiedContacts.size} Verified Friends • ${activeRelayNodes.size} Active Radio Relays",
                    color = Color(0xFF8E8E93),
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onOpenQrPairing,
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4AF37),
                    contentColor = Color.Black
                ),
                modifier = Modifier.height(36.dp)
            ) {
                Text(text = "+ Pair QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Filter Tabs: Verified Contacts vs Active Radio Relays
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0C), RoundedCornerShape(10.dp))
                .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(10.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (filterTab == 0) Color(0xFF1C1C1E) else Color.Transparent)
                    .clickable { filterTab = 0 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Verified Contacts (${verifiedContacts.size})",
                    color = if (filterTab == 0) Color(0xFFD4AF37) else Color(0xFF8E8E93),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (filterTab == 1) Color(0xFF1C1C1E) else Color.Transparent)
                    .clickable { filterTab = 1 }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Routing Relays (${activeRelayNodes.size})",
                    color = if (filterTab == 1) Color(0xFFD4AF37) else Color(0xFF8E8E93),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "🔍", fontSize = 14.sp)
            Spacer(modifier = Modifier.width(10.dp))
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (searchQuery.isEmpty()) {
                        Text(text = "Filter by name, handle, or node ID...", color = Color(0xFF8E8E93), fontSize = 14.sp)
                    }
                    inner()
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (displayedList.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = if (filterTab == 0) "🔑" else "📡", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (filterTab == 0) "No verified contacts paired yet. Tap '+ Pair QR' to pair with a friend."
                               else "Scanning BLE and Wi-Fi Direct radios for physical mesh relays...",
                        color = Color(0xFF8E8E93),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(displayedList) { neighbor ->
                    NeighborCardItem(neighbor = neighbor, onClick = { onSelectPeer(neighbor) })
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun NeighborCardItem(
    neighbor: NeighborEntity,
    onClick: () -> Unit
) {
    val isVerified = neighbor.isVerifiedContact

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0C), RoundedCornerShape(14.dp))
            .border(
                width = 1.dp,
                color = if (isVerified) Color(0xFFD4AF37) else Color(0xFF1C1C1E),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon / Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF1C1C1E))
                .border(1.dp, if (isVerified) Color(0xFFD4AF37) else Color(0xFF2C2C2E), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = if (isVerified) "👤" else "📡", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isVerified) neighbor.fullName else "Node [${neighbor.nodeId.take(8)}]",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(
                            if (isVerified) Color(0xFF1C1C1E) else Color(0xFF0A0A0C),
                            RoundedCornerShape(4.dp)
                        )
                        .border(
                            1.dp,
                            if (isVerified) Color(0xFFD4AF37) else Color(0xFF1C1C1E),
                            RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (isVerified) "VERIFIED PAIR" else neighbor.connectionType,
                        color = if (isVerified) Color(0xFFD4AF37) else Color(0xFF8E8E93),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "@${neighbor.username} • SHA-256: node_${neighbor.nodeId.take(6)}",
                color = Color(0xFF8E8E93),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "LQI Trust: ${(neighbor.trustScore * 100).toInt()}% • RSSI: ${neighbor.rssi} dBm",
                color = Color(0xFF34C759),
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${neighbor.batteryLevel}%",
                color = if (neighbor.batteryLevel > 20) Color(0xFF34C759) else Color(0xFFFF453A),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Direct Hop",
                color = Color(0xFF8E8E93),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
