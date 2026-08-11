package org.circle13.antara.ui

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.circle13.antara.core.network.UserIdentity

@Composable
fun IdentityScreen(
    identity: UserIdentity,
    onOpenQrPairing: () -> Unit,
    onRequestBatteryOptimizationExemption: () -> Unit,
    onResetIdentity: () -> Unit
) {
    var isLowPowerScan by remember { mutableStateOf(true) }
    var isWifiDirectEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Node Identity & Security",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Local Cryptographic Credentials & Security Configuration",
                color = Color(0xFF8E8E93),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Profile Identity Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0A0C), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(16.dp))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1C1C1E))
                        .border(1.dp, Color(0xFFD4AF37), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🔑", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = identity.fullName,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "@${identity.username}",
                    color = Color(0xFFD4AF37),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "node_${identity.nodeId}",
                    color = Color(0xFF8E8E93),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onOpenQrPairing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD4AF37),
                        contentColor = Color.Black
                    )
                ) {
                    Text(text = "Show QR Identity / Pair Friends", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Security Configuration Stack
        item {
            Text(
                text = "Cryptographic Security Stack",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            SecurityDetailCard(
                title = "Double Ratchet E2EE Engine",
                subtitle = "X25519 Ephemeral Key Exchange + AES-256-GCM",
                status = "ACTIVE"
            )
            Spacer(modifier = Modifier.height(8.dp))

            SecurityDetailCard(
                title = "SQLCipher Database Encryption",
                subtitle = "antara_secure.db key backed by Android Keystore",
                status = "ENCRYPTED"
            )
            Spacer(modifier = Modifier.height(8.dp))

            SecurityDetailCard(
                title = "Zero-Knowledge Onboarding",
                subtitle = "Zero server logs, zero phone numbers or email tracking",
                status = "VERIFIED"
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Hardware & Reliability Controls
        item {
            Text(
                text = "Radio & Battery Reliability Controls",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Unrestricted Battery Mode", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(text = "Exempt app from Android Doze mode to preserve background BLE scanner", color = Color(0xFF8E8E93), fontSize = 12.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onRequestBatteryOptimizationExemption,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1C1C1E), contentColor = Color(0xFFD4AF37))
                ) {
                    Text(text = "Configure", fontSize = 11.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Low-Power BLE Radio Scanning", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(text = "Optimizes battery consumption during continuous scans", color = Color(0xFF8E8E93), fontSize = 12.sp)
                }
                Switch(
                    checked = isLowPowerScan,
                    onCheckedChange = { isLowPowerScan = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD4AF37), checkedTrackColor = Color(0xFF1C1C1E))
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
        }

        // Reset Identity Button
        item {
            Button(
                onClick = onResetIdentity,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1C1C1E),
                    contentColor = Color(0xFFFF453A)
                )
            ) {
                Text(text = "Clear Local Database & Reset Identity", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SecurityDetailCard(
    title: String,
    subtitle: String,
    status: String
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
            Text(text = title, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = Color(0xFF8E8E93), fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = status,
            color = Color(0xFF34C759),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
