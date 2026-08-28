package com.ninjaconfig.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
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
import kotlin.random.Random

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

@Composable
fun ConnectScreen(
    selectedConfig: VpnConfig?,
    connectionState: ConnectionState,
    onToggleConnect: () -> Unit,
    onMenuClick: () -> Unit,
    onServerClick: () -> Unit
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
        ServerInfoRow(config = selectedConfig, onClick = onServerClick)
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun TopBar(onMenuClick: () -> Unit) {
    var tapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Text(
            "CT VPN",
            color = AccentWhite,
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier
                .align(Alignment.Center)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {
                    val now = System.currentTimeMillis()
                    tapCount = if (now - lastTapTime > 1200) 1 else tapCount + 1
                    lastTapTime = now
                    if (tapCount >= 5) {
                        tapCount = 0
                        onMenuClick()
                    }
                }
        )
    }
}

@Composable
private fun ConnectRing(state: ConnectionState, elapsedSeconds: Int, onClick: () -> Unit) {
    val isConnected = state == ConnectionState.CONNECTED
    val ringColor = NeonGreen

    val infinite = rememberInfiniteTransition(label = "ring")
    val rotation by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        // Faint scattered world-map-style dot backdrop behind the ring
        DotFieldBackground(modifier = Modifier.fillMaxSize())

        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 14.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2f - 10.dp.toPx()
            val center = Offset(size.width / 2f, size.height / 2f)
            val topLeft = Offset(center.x - radius, center.y - radius)
            val arcSize = Size(radius * 2, radius * 2)

            // Dim full background track
            drawArc(
                color = OutlinePill.copy(alpha = 0.4f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = topLeft,
                size = arcSize
            )

            if (isConnected) {
                val sweep = 300f
                val startAngle = rotation - 90f

                // Soft outer glow: several widening, fading passes behind the main arc
                listOf(3.2f to 0.06f, 2.4f to 0.10f, 1.7f to 0.16f).forEach { (widthMul, alpha) ->
                    drawArc(
                        color = ringColor.copy(alpha = alpha),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth * widthMul, cap = StrokeCap.Round),
                        topLeft = topLeft,
                        size = arcSize
                    )
                }

                // Sharp bright arc on top, fading tail like the reference design
                drawArc(
                    brush = Brush.sweepGradient(
                        listOf(ringColor.copy(alpha = 0.1f), ringColor, ringColor)
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )

                // Glowing dots at both ends of the spinning arc
                listOf(startAngle, startAngle + sweep).forEach { angleDeg ->
                    val rad = Math.toRadians(angleDeg.toDouble())
                    val dotX = center.x + radius * cos(rad).toFloat()
                    val dotY = center.y + radius * sin(rad).toFloat()
                    val dotCenter = Offset(dotX, dotY)
                    drawCircle(color = ringColor.copy(alpha = 0.25f), radius = strokeWidth * 1.8f, center = dotCenter)
                    drawCircle(color = ringColor, radius = strokeWidth * 0.85f, center = dotCenter)
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(CircleShape)
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }, onClick = onClick)
                .padding(40.dp)
        ) {
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Shield,
                    contentDescription = null,
                    tint = if (isConnected) AccentWhite.copy(alpha = 0.85f) else TextSecondary,
                    modifier = Modifier.size(72.dp)
                )
                if (isConnected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = NeonGreen,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            Text(
                when (state) {
                    ConnectionState.CONNECTED -> "CONNECTED"
                    ConnectionState.CONNECTING -> "CONNECTING..."
                    ConnectionState.DISCONNECTED -> "DISCONNECTED"
                },
                color = if (isConnected) NeonGreen else TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                letterSpacing = 1.5.sp
            )
            if (isConnected) {
                Spacer(Modifier.height(4.dp))
                Text(formatElapsed(elapsedSeconds), color = AccentWhite, fontSize = 14.sp)
            }
        }
    }
}

/** A quiet scatter of dots behind the ring, echoing a world-map texture without needing map data. */
@Composable
private fun DotFieldBackground(modifier: Modifier = Modifier) {
    val dots = remember {
        val rnd = Random(42)
        List(140) {
            Triple(rnd.nextFloat(), rnd.nextFloat(), 0.6f + rnd.nextFloat() * 0.8f)
        }
    }
    Canvas(modifier = modifier) {
        val minDim = size.minDimension
        dots.forEach { (fx, fy, sizeMul) ->
            val x = fx * size.width
            val y = fy * size.height
            val distFromCenter = kotlin.math.hypot(x - size.width / 2f, y - size.height / 2f)
            if (distFromCenter < minDim / 2f * 1.05f) {
                drawCircle(
                    color = TextSecondary.copy(alpha = 0.12f),
                    radius = 1.4.dp.toPx() * sizeMul,
                    center = Offset(x, y)
                )
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
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(if (connected) NeonGreenDim.copy(alpha = 0.35f) else CardDarker),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Shield,
                contentDescription = null,
                tint = if (connected) NeonGreen else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            if (connected) "اتصال شما امن است" else "برای اتصال امن، وصل شوید",
            color = AccentWhite,
            fontSize = 14.sp,
            modifier = Modifier.weight(1f)
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(NeonGreenDim.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun ServerInfoRow(config: VpnConfig?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(38.dp).clip(CircleShape).background(CardDarker),
            contentAlignment = Alignment.Center
        ) {
            val code = config?.countryCode?.trim()
            if (!code.isNullOrBlank() && code.length == 2) {
                coil.compose.AsyncImage(
                    model = "https://flagcdn.com/w160/${code.lowercase()}.png",
                    contentDescription = config?.countryName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Text("🌐", fontSize = 18.sp)
            }
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(config?.countryName ?: "در حال انتخاب سرور", color = AccentWhite, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            if (config?.label?.isNotBlank() == true) {
                Text(config.label, color = TextSecondary, fontSize = 12.sp)
            }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
    }
}

