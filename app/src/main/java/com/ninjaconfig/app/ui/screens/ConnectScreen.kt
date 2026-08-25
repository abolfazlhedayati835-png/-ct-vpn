package com.ninjaconfig.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninjaconfig.app.data.VpnConfig
import com.ninjaconfig.app.data.countryCodeToFlagEmoji
import com.ninjaconfig.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

@Composable
fun ConnectScreen(
    selectedConfig: VpnConfig?,
    connectionState: ConnectionState,
    onToggleConnect: () -> Unit,
    onMenuClick: () -> Unit
) {
    var elapsedSeconds by remember { mutableStateOf(0) }

    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            elapsedSeconds = 0
            while (true) {
                delay(1000)
                elapsedSeconds++
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(20.dp))
        TopBar(onMenuClick = onMenuClick)
        Spacer(Modifier.height(30.dp))
        ConnectRing(state = connectionState, elapsedSeconds = elapsedSeconds, onClick = onToggleConnect)
        Spacer(Modifier.height(30.dp))
        SecureBanner(state = connectionState)
        Spacer(Modifier.height(14.dp))
        ServerInfoRow(config = selectedConfig)
        Spacer(Modifier.height(14.dp))
        SpeedCards()
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun TopBar(onMenuClick: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onMenuClick, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(Icons.Filled.Menu, contentDescription = "Menu", tint = AccentWhite)
        }
        Text(
            "CT VPN",
            color = AccentWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@Composable
private fun ConnectRing(state: ConnectionState, elapsedSeconds: Int, onClick: () -> Unit) {
    val isConnected = state == ConnectionState.CONNECTED
    val ringColor = NeonGreen

    val infinite = rememberInfiniteTransition(label = "ring")
    val glowAlpha by infinite.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 30.dp),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f
            val center = Offset(size.width / 2f, size.height / 2f)

            // Dim full background track
            drawArc(
                color = OutlinePill.copy(alpha = 0.5f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2)
            )

            if (isConnected) {
                val sweep = 360f
                val startAngle = -90f
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(ringColor.copy(alpha = 0.25f), ringColor, ringColor.copy(alpha = 0.25f))
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    alpha = glowAlpha
                )

                // Glowing dots at both ends of the arc, matching the reference design
                listOf(startAngle, startAngle + sweep).forEach { angleDeg ->
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val dotX = center.x + radius * cos(rad).toFloat()
                    val dotY = center.y + radius * sin(rad).toFloat()
                    drawCircle(
                        color = ringColor,
                        radius = strokeWidth * 0.9f,
                        center = Offset(dotX, dotY),
                        alpha = glowAlpha
                    )
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(onClick = onClick)
                .padding(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(if (isConnected) NeonGreenDim.copy(alpha = 0.4f) else CardDark),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    tint = if (isConnected) NeonGreen else TextSecondary,
                    modifier = Modifier.size(40.dp)
                )
                if (isConnected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                when (state) {
                    ConnectionState.CONNECTED -> "CONNECTED"
                    ConnectionState.CONNECTING -> "CONNECTING..."
                    ConnectionState.DISCONNECTED -> "DISCONNECTED"
                },
                color = if (isConnected) NeonGreen else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            if (isConnected) {
                Spacer(Modifier.height(4.dp))
                Text(formatElapsed(elapsedSeconds), color = AccentWhite, fontSize = 13.sp)
            }
        }
    }
}

private fun formatElapsed(totalSeconds: Int): String {
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return "%02d:%02d:%02d".format(h, m, s)
}

@Composable
private fun SecureBanner(state: ConnectionState) {
    val connected = state == ConnectionState.CONNECTED
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Filled.Shield,
            contentDescription = null,
            tint = if (connected) NeonGreen else TextSecondary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            if (connected) "اتصال شما امن است" else "برای اتصال امن، وصل شوید",
            color = AccentWhite,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}

@Composable
private fun ServerInfoRow(config: VpnConfig?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(CardDarker),
            contentAlignment = Alignment.Center
        ) {
            Text(config?.countryCode?.let { countryCodeToFlagEmoji(it) } ?: "🌐", fontSize = 18.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(config?.countryName ?: "در حال انتخاب سرور", color = AccentWhite, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            if (config?.label?.isNotBlank() == true) {
                Text(config.label, color = TextSecondary, fontSize = 12.sp)
            }
        }
        Icon(Icons.Filled.SignalCellularAlt, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
    }
}

@Composable
private fun SpeedCards() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        SpeedCard(label = "دانلود", value = "—", icon = Icons.Filled.ArrowDownward, modifier = Modifier.weight(1f))
        SpeedCard(label = "آپلود", value = "—", icon = Icons.Filled.ArrowUpward, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun SpeedCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(label, color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Box(
                modifier = Modifier.size(26.dp).clip(CircleShape).background(NeonGreenDim),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = AccentWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Text("Mbps", color = TextSecondary, fontSize = 12.sp)
        }
    }
}
