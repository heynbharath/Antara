package org.circle13.antara.feature.dashboard

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.circle13.antara.core.database.NeighborEntity

// Ultra-Premium Color Palette
val LuxuryBlack = Color(0xFF050505)
val SurfaceDark = Color(0xFF121212)
val GoldAccent = Color(0xFFD4AF37)
val GlassWhite = Color.White.copy(alpha = 0.05f)
val GlassBorder = Color.White.copy(alpha = 0.1f)
val TextPrimary = Color(0xFFF5F5F7)
val TextSecondary = Color(0xFF86868B)

@Composable
fun DashboardScreen(
    neighbors: List<NeighborEntity>,
    onSelectPeer: (NeighborEntity) -> Unit,
    onOpenQrPairing: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LuxuryBlack)
    ) {
        // Subtle ambient mesh background glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(GoldAccent.copy(alpha = 0.15f), Color.Transparent),
                        radius = 600f
                    )
                )
        )

        Column(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(64.dp)) // Safe area inset

            // Luxury Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Antara",
                        color = GoldAccent,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp,
                        fontFamily = FontFamily.SansSerif
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Spatial Mesh",
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light,
                        letterSpacing = (-1).sp
                    )
                }

                // Apple-like glass button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassWhite)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenQrPairing
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Pair Node",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            // UWB Radar Visualization
            UwbRadarPulse(neighborsCount = neighbors.size)

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "ACTIVE NODES",
                color = TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (neighbors.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(SurfaceDark)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Awaiting ultra-wideband discovery...",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(neighbors) { neighbor ->
                        PremiumNodeCard(neighbor = neighbor, onClick = { onSelectPeer(neighbor) })
                    }
                }
            }
        }
    }
}

@Composable
fun PremiumNodeCard(neighbor: NeighborEntity, onClick: () -> Unit) {
    val isVerified = neighbor.isVerifiedContact

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SurfaceDark)
            .border(
                width = 1.dp,
                color = if (isVerified) GoldAccent.copy(alpha = 0.3f) else GlassBorder,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(if (isVerified) GoldAccent.copy(alpha = 0.1f) else Color.Transparent)
                .border(1.dp, if (isVerified) GoldAccent else GlassBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = neighbor.username.take(1).uppercase(),
                color = if (isVerified) GoldAccent else TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = neighbor.fullName,
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.5).sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF34C759).copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${(neighbor.trustScore * 100).toInt()}% TRUST",
                        color = Color(0xFF34C759),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "@${neighbor.username}",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
        }

        // Distance / UWB indicator
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "1.2m", // Placeholder for UWB precision
                color = TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "UWB Range",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
fun UwbRadarPulse(neighborsCount: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "Radar")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp),
        contentAlignment = Alignment.Center
    ) {
        // Expanding ring
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .border(1.dp, GoldAccent.copy(alpha = alpha), CircleShape)
        )
        // Center Core
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(GoldAccent.copy(alpha = 0.4f), Color.Transparent)))
                .border(2.dp, GoldAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$neighborsCount",
                color = LuxuryBlack,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
