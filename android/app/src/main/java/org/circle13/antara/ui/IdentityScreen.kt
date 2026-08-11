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

@Composable
fun IdentityScreen(
    username: String,
    nodeId: String,
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
                text = "Identity & Security",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Local Node Credentials & Cryptographic Engine",
                color = Color(0xFF8E8E93),
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Identity Profile Header Card
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
                    text = username,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "ed25519:$nodeId",
                    color = Color(0xFFD4AF37),
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Simulated QR Verification Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFF1C1C1E), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "QR Identity Verification Ready",
                        color = Color(0xFF34C759),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Cryptographic Engine Status
        item {
            Text(
                text = "Security Stack",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            SecuritySettingItem(
                title = "Double Ratchet Protocol",
                subtitle = "X25519 Ephemeral Key Exchange + AES-256-GCM",
                status = "ACTIVE"
            )
            Spacer(modifier = Modifier.height(8.dp))

            SecuritySettingItem(
                title = "SQLCipher Database Encryption",
                subtitle = "antara_secure.db key derived from Android Keystore",
                status = "ENCRYPTED"
            )
            Spacer(modifier = Modifier.height(8.dp))

            SecuritySettingItem(
                title = "Zero-Knowledge Onboarding",
                subtitle = "No central servers, email, or phone registration used",
                status = "VERIFIED"
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Background Radio Controls
        item {
            Text(
                text = "Hardware Radio Controls",
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
                    Text(text = "Low-Power BLE Scanning", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(text = "Saves battery during continuous background scanning", color = Color(0xFF8E8E93), fontSize = 12.sp)
                }
                Switch(
                    checked = isLowPowerScan,
                    onCheckedChange = { isLowPowerScan = it },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD4AF37), checkedTrackColor = Color(0xFF1C1C1E))
                )
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
                    Text(text = "Wi-Fi Direct Transport", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    Text(text = "Enables high-bandwidth bulk message sync", color = Color(0xFF8E8E93), fontSize = 12.sp)
                }
                Switch(
                    checked = isWifiDirectEnabled,
                    onCheckedChange = { isWifiDirectEnabled = it },
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
                Text(text = "Wipe Local Cache & Reset Identity", fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SecuritySettingItem(
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
