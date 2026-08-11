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
fun TelemetryScreen() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Protocol Telemetry & KPIs",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Real-time Network Diagnostics & Radio Performance",
                color = Color(0xFF8E8E93),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Key Performance Indicators (4 Cards in 2x2 Grid)
        item {
            Text(
                text = "Campus Pilot KPIs",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Mesh Density",
                    value = "18 Nodes",
                    target = "Target: ≥15",
                    isGood = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Delivery Latency",
                    value = "1.8 sec",
                    target = "Target: ≤90s (2 Hops)",
                    isGood = true
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "Battery Drain",
                    value = "1.2%/hr",
                    target = "Target: ≤1.8%/hr",
                    isGood = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                KpiCard(
                    modifier = Modifier.weight(1f),
                    title = "DTN Delivery",
                    value = "98.4%",
                    target = "Target: ≥95%",
                    isGood = true
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Physical Radio Hardware State
        item {
            Text(
                text = "Physical Radio Hardware Engine",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            RadioStateItem(
                name = "Bluetooth LE GATT Service",
                type = "Advertising & Scanning",
                status = "ACTIVE • 100ms Interval",
                isActive = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            RadioStateItem(
                name = "Wi-Fi Direct (P2P Group)",
                type = "High-Bandwidth Bulk Sync",
                status = "IDLE (Standby)",
                isActive = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            RadioStateItem(
                name = "Multicast UDP Socket",
                type = "Local LAN Broadcast (Port 13130)",
                status = "LISTENING • 0.0.0.0",
                isActive = true
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Protocol Counters
        item {
            Text(
                text = "Traffic & Cryptography Stats",
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
                StatRow(label = "Packets Relayed", value = "142 Packets")
                Spacer(modifier = Modifier.height(10.dp))
                StatRow(label = "Total Data Transferred", value = "2.8 MB")
                Spacer(modifier = Modifier.height(10.dp))
                StatRow(label = "AODV Route Table Entries", value = "4 Routes")
                Spacer(modifier = Modifier.height(10.dp))
                StatRow(label = "Double Ratchet Sessions", value = "2 Active Keys")
                Spacer(modifier = Modifier.height(10.dp))
                StatRow(label = "SQLCipher Database Size", value = "4.2 MB Encrypted")
            }
        }
    }
}

@Composable
private fun KpiCard(
    modifier: Modifier,
    title: String,
    value: String,
    target: String,
    isGood: Boolean
) {
    Column(
        modifier = modifier
            .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
            .padding(14.dp)
    ) {
        Text(text = title, color = Color(0xFF8E8E93), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = target,
            color = if (isGood) Color(0xFF34C759) else Color(0xFFFF453A),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun RadioStateItem(
    name: String,
    type: String,
    status: String,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(if (isActive) Color(0xFF34C759) else Color(0xFF8E8E93))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = type, color = Color(0xFF8E8E93), fontSize = 12.sp)
        }
        Text(
            text = status,
            color = Color(0xFFD4AF37),
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF8E8E93), fontSize = 13.sp)
        Text(text = value, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Monospace)
    }
}
