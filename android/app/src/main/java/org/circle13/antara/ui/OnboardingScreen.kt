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
import java.security.SecureRandom

@Composable
fun OnboardingScreen(
    onRequestPermissions: () -> Unit,
    onCompleteOnboarding: (username: String, nodeId: String) -> Unit
) {
    var step by remember { mutableIntStateOf(0) }
    var username by remember { mutableStateOf("Student_Node") }
    var generatedNodeId by remember {
        mutableStateOf(generateMockNodeId())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Step Progress Indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(3) { index ->
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
                if (index < 2) {
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
                    username = username,
                    onUsernameChange = { username = it },
                    nodeId = generatedNodeId,
                    onRegenerate = { generatedNodeId = generateMockNodeId() }
                )
                2 -> PermissionsStep(onRequestPermissions = onRequestPermissions)
            }
        }

        // Bottom Action Button
        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    if (step < 2) {
                        step++
                    } else {
                        onCompleteOnboarding(username.ifBlank { "Anonymous Node" }, generatedNodeId)
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
                        1 -> "Confirm Identity →"
                        else -> "Enter Antara Mesh"
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
        // Icon / Emblem
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF0A0A0C), CircleShape)
                .border(1.dp, Color(0xFFD4AF37), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "⚡",
                fontSize = 36.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Antara",
            style = TextStyle(
                color = Color.White,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Zero-Knowledge Offline P2P Mesh Network",
            color = Color(0xFFD4AF37),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Communicate across campus or disconnected areas without Internet, Cellular, or Wi-Fi infrastructure. Messages hop securely across peer devices using end-to-end encryption.",
            color = Color(0xFF8E8E93),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun IdentityStep(
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

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Your local node identity is derived from an Ed25519 keypair generated on this device. No email, phone number, or central server required.",
            color = Color(0xFF8E8E93),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Username Field
        Text(
            text = "Display Handle",
            color = Color(0xFF8E8E93),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = username,
                onValueChange = onUsernameChange,
                textStyle = TextStyle(color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Fingerprint Card
        Text(
            text = "Ed25519 Public Key Fingerprint",
            color = Color(0xFF8E8E93),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(6.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "ed25519:$nodeId",
                color = Color(0xFF34C759),
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 18.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "STATUS: ACTIVE KEY",
                    color = Color(0xFF8E8E93),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Regenerate ↻",
                    color = Color(0xFF0A84FF),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clip(RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
private fun PermissionsStep(
    onRequestPermissions: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Radio & Discovery Permissions",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Antara operates directly over physical radios. To discover nearby peer nodes without internet, the Android operating system requires radio access permissions.",
            color = Color(0xFF8E8E93),
            fontSize = 14.sp,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        PermissionItem(
            title = "Bluetooth LE Scan & Advertise",
            description = "Discovers peer devices in range & broadcasts ephemeral presence beacons.",
            icon = "📡"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionItem(
            title = "Location (Radio Access)",
            description = "Required by Android OS for BLE and Wi-Fi Direct hardware scanning.",
            icon = "📍"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PermissionItem(
            title = "Background Mesh Daemon",
            description = "Keeps low-power BLE scanner active when app is minimized.",
            icon = "⚡"
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onRequestPermissions,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1C1C1E),
                contentColor = Color.White
            )
        ) {
            Text(text = "Grant System Permissions")
        }
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    icon: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = Color(0xFF8E8E93),
                fontSize = 12.sp
            )
        }
    }
}

private fun generateMockNodeId(): String {
    val random = SecureRandom()
    val bytes = ByteArray(16)
    random.nextBytes(bytes)
    return bytes.joinToString("") { "%02x".format(it) }
}
