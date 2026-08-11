package org.circle13.antara.feature.chat

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.circle13.antara.core.database.MessageEntity

@Composable
fun ChatScreen(
    threadTitle: String,
    messages: List<MessageEntity>,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF000000))
            .padding(16.dp)
    ) {
        // Quiet Header with Double Ratchet Encryption Badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = threadTitle,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.SansSerif
                    )
                )
                Text(
                    text = "Double Ratchet E2EE • Store-and-Forward Mesh",
                    color = Color(0xFFD4AF37),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Box(
                modifier = Modifier
                    .background(Color(0xFF0A0A0C), RoundedCornerShape(6.dp))
                    .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "🔒 E2EE",
                    color = Color(0xFF34C759),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Message Feed
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            reverseLayout = true
        ) {
            items(messages.reversed()) { message ->
                MessageBubble(message)
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Input Field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0A0A0C), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF1C1C1E), RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = inputText,
                onValueChange = { inputText = it },
                textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "Send encrypted mesh message...",
                                color = Color(0xFF8E8E93),
                                fontSize = 15.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )

            Spacer(modifier = Modifier.width(8.dp))

            TextButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendMessage(inputText)
                        inputText = ""
                    }
                }
            ) {
                Text(
                    text = "Send ⚡",
                    color = Color(0xFFD4AF37),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }
    }
}

@Composable
fun MessageBubble(message: MessageEntity) {
    val isLocalSender = message.senderIdentity == "local"

    // Simulate lifecycle status (Queued -> Sent -> Delivered)
    val statusText = if (isLocalSender) "Delivered • ACK" else "Relayed via Hop 1"
    val statusColor = if (isLocalSender) Color(0xFF34C759) else Color(0xFF8E8E93)

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isLocalSender) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .background(
                    color = if (isLocalSender) Color(0xFF1C1C1E) else Color(0xFF0A0A0C),
                    shape = RoundedCornerShape(14.dp)
                )
                .border(
                    width = 1.dp,
                    color = if (isLocalSender) Color(0xFFD4AF37) else Color(0xFF1C1C1E),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(14.dp)
        ) {
            Text(
                text = message.body,
                color = Color.White,
                fontSize = 15.sp,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = statusText,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
