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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredNeighbors = neighbors.filter {
        it.nodeId.contains(searchQuery, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp)
    ) {
        // Title & Active Radio Scanning Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Nearby Nodes",
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                )
                Text(
                    text = "${neighbors.size} active mesh peers in radio range",
                    color = Color(0xFF8E8E93),
                    fontSize = 12.sp
                )
            }

            // Radio Scanner Active Pill
            Row(
                modifier = Modifier
                    .background(Color(0xFF0A0A0C), RoundedCornerShape(20.dp))
                    .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF34C759))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BLE Scanning",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Field
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
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search Node ID hash or handle...",
                                color = Color(0xFF8E8E93),
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredNeighbors.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "📡", fontSize = 32.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Searching silently for radio signals...",
                        color = Color(0xFF8E8E93),
                        fontSize = 15.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(filteredNeighbors) { neighbor ->
                    NeighborItem(neighbor = neighbor, onClick = { onSelectPeer(neighbor) })
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun NeighborItem(
    neighbor: NeighborEntity,
    onClick: () -> Unit
) {
    val isBle = neighbor.rssi > -70
    val transportLabel = if (isBle) "BLE GATT" else "Wi-Fi Direct"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0C), RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Node Avatar / Type
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFF1C1C1E))
                .border(1.dp, Color(0xFFD4AF37), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📱", fontSize = 20.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Node [${neighbor.nodeId.take(8)}]",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1C1C1E), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = transportLabel,
                        color = Color(0xFFD4AF37),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Trust LQI: ${(neighbor.trustScore * 100).toInt()}% • RSSI: ${neighbor.rssi} dBm",
                color = Color(0xFF8E8E93),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Distance & Battery
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${neighbor.batteryLevel}%",
                color = if (neighbor.batteryLevel > 20) Color(0xFF34C759) else Color(0xFFFF453A),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "1 Hop",
                color = Color(0xFF8E8E93),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
