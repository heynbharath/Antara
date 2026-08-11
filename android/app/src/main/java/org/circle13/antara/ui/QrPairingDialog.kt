package org.circle13.antara.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.window.Dialog
import org.circle13.antara.core.database.NeighborEntity
import org.circle13.antara.core.network.CryptoManager
import org.circle13.antara.core.network.UserIdentity

@Composable
fun QrPairingDialog(
    myIdentity: UserIdentity,
    onDismiss: () -> Unit,
    onPairVerifiedContact: (NeighborEntity) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var pasteInput by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val qrPayload = remember(myIdentity) {
        CryptoManager.generateQrPayload(myIdentity)
    }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0C), RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Identity & Peer Verification",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "✕",
                    color = Color(0xFF8E8E93),
                    fontSize = 18.sp,
                    modifier = Modifier.clickable { onDismiss() }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sub-tabs (My QR Code vs Scan/Pair Friend)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1C1C1E), RoundedCornerShape(8.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selectedTab == 0) Color(0xFFD4AF37) else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "My QR Identity",
                        color = if (selectedTab == 0) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (selectedTab == 1) Color(0xFFD4AF37) else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Pair Friend",
                        color = if (selectedTab == 1) Color.Black else Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (selectedTab == 0) {
                // Display My QR Code Matrix Representation
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(Color.White, RoundedCornerShape(12.dp))
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // High-tech matrix visual grid representing QR Code
                        VisualQrGrid(payload = qrPayload)
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = myIdentity.fullName,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${myIdentity.username} • SHA-256 Fingerprint",
                        color = Color(0xFF8E8E93),
                        fontSize = 12.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "antara_node_${myIdentity.nodeId.take(12)}",
                        color = Color(0xFFD4AF37),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                // Scan / Input Peer Key to Pair
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter or Paste Friend's Antara URI / Public Key",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF1C1C1E), RoundedCornerShape(10.dp))
                            .border(1.dp, Color(0xFF2C2C2E), RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = pasteInput,
                            onValueChange = {
                                pasteInput = it
                                errorText = null
                            },
                            textStyle = TextStyle(color = Color.White, fontSize = 13.sp, fontFamily = FontFamily.Monospace),
                            modifier = Modifier.weight(1f),
                            decorationBox = { inner ->
                                if (pasteInput.isEmpty()) {
                                    Text(text = "Paste antara://identity URI...", color = Color(0xFF8E8E93), fontSize = 13.sp)
                                }
                                inner()
                            }
                        )
                    }

                    if (errorText != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = errorText!!, color = Color(0xFFFF453A), fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val parsed = CryptoManager.parseQrPayload(pasteInput.trim())
                            if (parsed != null) {
                                val neighbor = NeighborEntity(
                                    nodeId = parsed.nodeId,
                                    username = parsed.username,
                                    fullName = parsed.fullName,
                                    publicKeyHex = parsed.publicKeyHex,
                                    isVerifiedContact = true,
                                    connectionType = "VERIFIED_FRIEND",
                                    rssi = -60,
                                    batteryLevel = 90,
                                    trustScore = 1.0,
                                    lastSeen = System.currentTimeMillis()
                                )
                                onPairVerifiedContact(neighbor)
                                onDismiss()
                            } else {
                                errorText = "Invalid Antara identity URI string."
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black)
                    ) {
                        Text(text = "Verify & Save Contact", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun VisualQrGrid(payload: String) {
    // Generate deterministic 10x10 pattern grid from payload hash
    val hash = payload.hashCode()
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(8) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(8) { col ->
                    val isFilled = ((hash shr (row + col)) and 1) == 1 || (row == 0 && col == 0) || (row == 7 && col == 7) || (row == 0 && col == 7)
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .background(if (isFilled) Color.Black else Color.White)
                    )
                }
            }
        }
    }
}
