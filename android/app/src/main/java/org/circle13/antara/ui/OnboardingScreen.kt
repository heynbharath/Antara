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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.circle13.antara.core.network.CryptoManager

@Composable
fun OnboardingScreen(
    onRequestPermissions: () -> Unit,
    onRequestBatteryOptimizationExemption: () -> Unit,
    onCompleteOnboarding: (fullName: String, username: String, nodeId: String, publicKeyHex: String) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var fullName by remember { mutableStateOf("Alex Miller") }
    var username by remember { mutableStateOf("alex_m") }

    val keypair = remember { CryptoManager.generateKeyPair() }
    var nodeId by remember { mutableStateOf(keypair.first) }
    var publicKeyHex by remember { mutableStateOf(keypair.second) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // 2-Step Progress Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(2) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == step) 10.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == step) Color(0xFFD4AF37)
                            else if (index < step) Color.White
                            else Color(0xFF1C1C1E)
                        )
                )
                if (index < 1) {
                    Spacer(modifier = Modifier.width(12.dp))
                }
            }
        }

        // Center Step Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when (step) {
                0 -> WelcomeStep()
                1 -> IdentityStep(
                    fullName = fullName,
                    onFullNameChange = { fullName = it },
                    username = username,
                    onUsernameChange = { username = it },
                    nodeId = nodeId,
                    onRegenerate = {
                        val newKp = CryptoManager.generateKeyPair()
                        nodeId = newKp.first
                        publicKeyHex = newKp.second
                    }
                )
            }
        }

        // Bottom Action Button
        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (step < 1) {
                        step++
                    } else {
                        onRequestPermissions()
                        onRequestBatteryOptimizationExemption()
                        onCompleteOnboarding(
                            fullName.ifBlank { "Antara Node" },
                            username.ifBlank { "node_user" },
                            nodeId,
                            publicKeyHex
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD4AF37),
                    contentColor = Color.Black
                )
            ) {
                Text(
                    text = when (step) {
                        0 -> "Get Started →"
                        else -> "Initialize & Enter Antara Mesh"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun WelcomeStep() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF0A0A0C), CircleShape)
                .border(1.dp, Color(0xFFD4AF37), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "⚡", fontSize = 36.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Antara Protocol",
            style = TextStyle(
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Zero-Knowledge Disconnected P2P Mesh Network",
            color = Color(0xFFD4AF37),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Communicate securely over physical BLE and Wi-Fi Direct radios without cellular, internet, or central servers. Packets route dynamically across intermediate nodes using Double Ratchet E2EE encryption.",
            color = Color(0xFF8E8E93),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun IdentityStep(
    fullName: String,
    onFullNameChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    nodeId: String,
    onRegenerate: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Cryptographic Identity",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your identity is anchored to an EC keypair generated on this hardware. No phone number, email, or central account required.",
            color = Color(0xFF8E8E93),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Full Name Input
        Text(text = "Full Name", color = Color(0xFF8E8E93), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            BasicTextField(
                value = fullName,
                onValueChange = onFullNameChange,
                textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Handle / Username Input
        Text(text = "Handle Tag", color = Color(0xFF8E8E93), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(text = "@", color = Color(0xFFD4AF37), fontSize = 15.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(4.dp))
            BasicTextField(
                value = username,
                onValueChange = onUsernameChange,
                textStyle = TextStyle(color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Fingerprint Card
        Text(text = "SHA-256 Public Key Fingerprint", color = Color(0xFF8E8E93), fontSize = 12.sp, fontWeight = FontWeight.Medium)
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                .padding(14.dp)
        ) {
            Text(
                text = "node_$nodeId",
                color = Color(0xFF34C759),
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "SECURE LOCAL KEYSTORE", color = Color(0xFF8E8E93), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = "Regenerate Key ↻",
                    color = Color(0xFF0A84FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { onRegenerate() }
                )
            }
        }
    }
}
