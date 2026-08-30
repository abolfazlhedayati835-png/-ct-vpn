package com.ninjaconfig.app.ui.screens

import android.content.Intent
import android.graphics.BlurMaskFilter
import android.net.TrafficStats
import android.net.Uri
import android.os.Process
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.PaintingStyle
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.ninjaconfig.app.data.VpnConfig
import com.ninjaconfig.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

enum class ConnectionState { DISCONNECTED, CONNECTING, CONNECTED }

private const val TELEGRAM_CHANNEL_URL = "https://t.me/CTVPN_Free"

@Composable
fun ConnectScreen(
    selectedConfig: VpnConfig?,
    connectionState: ConnectionState,
    downloadHistory: List<Float>,
    uploadHistory: List<Float>,
    totalDownloadBytes: Long,
    totalUploadBytes: Long,
    onToggleConnect: () -> Unit,
    onMenuClick: () -> Unit,
    onServerClick: () -> Unit
) {
    val context = LocalContext.current
    var showTelegramPrompt by remember { mutableStateOf(false) }

    // Show the Telegram-channel prompt a moment after each successful connect,
    // like an ad placement - not on app open, not while just browsing servers.
    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTED) {
            delay(1500)
            showTelegramPrompt = true
        } else {
            showTelegramPrompt = false
        }
    }

    if (showTelegramPrompt) {
        TelegramPromptDialog(
            onJoin = {
                showTelegramPrompt = false
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(TELEGRAM_CHANNEL_URL))
                context.startActivity(intent)
            },
            onDismiss = { showTelegramPrompt = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
    ) {
        DotMatrixBackground(
            modifier = Modifier.fillMaxSize(),
            dotColor = NeonGreenDim
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(20.dp))
            TopBar(onMenuClick = onMenuClick)
            Spacer(Modifier.height(30.dp))
            AnimatedRingWithShield(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(horizontal = 20.dp),
                state = connectionState,
                onClick = onToggleConnect
            )
            Spacer(Modifier.height(30.dp))
            SecureBanner(state = connectionState)
            Spacer(Modifier.height(14.dp))
            ServerInfoRow(config = selectedConfig, onClick = onServerClick)
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SpeedCard(
                    modifier = Modifier.weight(1f),
                    title = "DOWNLOAD",
                    totalBytes = totalDownloadBytes,
                    history = downloadHistory,
                    icon = Icons.Filled.ArrowDownward
                )
                SpeedCard(
                    modifier = Modifier.weight(1f),
                    title = "UPLOAD",
                    totalBytes = totalUploadBytes,
                    history = uploadHistory,
                    icon = Icons.Filled.ArrowUpward
                )
            }
            Spacer(Modifier.height(20.dp))
        }
    }
}

@Composable
private fun TelegramPromptDialog(onJoin: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(22.dp))
                .background(CardDark)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Send, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "عضو کانال تلگرام CT VPN شوید",
                color = AccentWhite,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "تو کانال ما آموزش تنظیمات، خبر سرورهای جدید و پشتیبانی مستقیم داریم.",
                color = TextSecondary,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    "فعلاً نه",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardDarker)
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 14.dp)
                )
                Text(
                    "بله، عضو می‌شوم",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NeonGreen)
                        .clickable(onClick = onJoin)
                        .padding(vertical = 14.dp)
                )
            }
        }
    }
}

fun currentRxTxBytes(): Pair<Long, Long> {
    val uid = Process.myUid()
    val rx = TrafficStats.getUidRxBytes(uid).let { if (it < 0) TrafficStats.getTotalRxBytes() else it }
    val tx = TrafficStats.getUidTxBytes(uid).let { if (it < 0) TrafficStats.getTotalTxBytes() else it }
    return rx to tx
}

/** Splits a byte count into a display number + unit, climbing KB -> MB -> GB like other VPN apps. */
private fun formatBytesSplit(bytes: Long): Pair<String, String> {
    val kb = bytes / 1024.0
    return when {
        kb < 1024.0 -> "%.1f".format(kb) to "KB"
        kb < 1024.0 * 1024.0 -> "%.1f".format(kb / 1024.0) to "MB"
        else -> "%.2f".format(kb / 1024.0 / 1024.0) to "GB"
    }
}

@Composable
private fun SpeedCard(
    modifier: Modifier = Modifier,
    title: String,
    totalBytes: Long,
    history: List<Float>,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(CardDark)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(NeonGreen.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(14.dp))
            }
        }
        Spacer(Modifier.height(6.dp))
        val (numberText, unitText) = formatBytesSplit(totalBytes)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                numberText,
                color = AccentWhite,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(4.dp))
            Text(unitText, color = TextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 3.dp))
        }
        Spacer(Modifier.height(8.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(24.dp)) {
            if (history.size >= 2) {
                val maxVal = (history.maxOrNull() ?: 1f).coerceAtLeast(1f)
                val stepX = size.width / (history.size - 1).coerceAtLeast(1)
                val path = Path()
                history.forEachIndexed { index, value ->
                    val x = index * stepX
                    val y = size.height - (value / maxVal) * size.height
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                drawPath(path, color = NeonGreen, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
            }
        }
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
private fun AnimatedRingWithShield(
    modifier: Modifier = Modifier,
    state: ConnectionState,
    onClick: () -> Unit
) {
    val isConnected = state == ConnectionState.CONNECTED
    val neonGreen = NeonGreen
    val dimGray = Color(0xFF4A5568)

    val infiniteTransition = rememberInfiniteTransition(label = "rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick
        ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val r = size.minDimension / 2 - 18.dp.toPx()
            val sw = 6.5f.dp.toPx()

            if (isConnected) {
                drawIntoCanvas { canvas ->
                    val glowPaint = Paint().apply {
                        color = neonGreen.copy(alpha = 0.35f)
                        style = PaintingStyle.Stroke
                        strokeWidth = sw * 2.2f
                        strokeCap = StrokeCap.Round
                        asFrameworkPaint().maskFilter = BlurMaskFilter(22f, BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.save()
                    canvas.translate(cx, cy)
                    canvas.rotate(rotation)
                    canvas.translate(-cx, -cy)
                    canvas.drawArc(cx - r, cy - r, cx + r, cy + r, -90f, 275f, false, glowPaint)
                    canvas.restore()
                }

                drawIntoCanvas { canvas ->
                    val mainPaint = Paint().apply {
                        color = neonGreen
                        style = PaintingStyle.Stroke
                        strokeWidth = sw
                        strokeCap = StrokeCap.Round
                        asFrameworkPaint().maskFilter = BlurMaskFilter(6f, BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.save()
                    canvas.translate(cx, cy)
                    canvas.rotate(rotation)
                    canvas.translate(-cx, -cy)
                    canvas.drawArc(cx - r, cy - r, cx + r, cy + r, -90f, 275f, false, mainPaint)
                    canvas.restore()
                }

                drawIntoCanvas { canvas ->
                    val tipAngle = Math.toRadians((rotation - 90).toDouble())
                    val tipX = cx + r * cos(tipAngle).toFloat()
                    val tipY = cy + r * sin(tipAngle).toFloat()
                    val tipPaint = Paint().apply {
                        color = Color.White.copy(alpha = 0.9f)
                        asFrameworkPaint().maskFilter = BlurMaskFilter(12f, BlurMaskFilter.Blur.NORMAL)
                    }
                    canvas.drawCircle(Offset(tipX, tipY), 5.5f.dp.toPx(), tipPaint)
                }
            } else {
                drawArc(
                    color = dimGray.copy(alpha = 0.5f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = sw, cap = StrokeCap.Round),
                    topLeft = Offset(cx - r, cy - r),
                    size = androidx.compose.ui.geometry.Size(r * 2, r * 2)
                )
            }
        }

        ShieldIcon(
            modifier = Modifier.size(88.dp),
            color = if (isConnected) neonGreen else dimGray,
            showCheck = isConnected
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.offset(y = 72.dp)
        ) {
            Text(
                text = when (state) {
                    ConnectionState.CONNECTED -> "CONNECTED"
                    ConnectionState.CONNECTING -> "CONNECTING..."
                    ConnectionState.DISCONNECTED -> "DISCONNECTED"
                },
                color = if (isConnected) neonGreen else TextSecondary,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )
        }
    }
}

@Composable
private fun ShieldIcon(modifier: Modifier = Modifier, color: Color, showCheck: Boolean) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val p = w * 0.06f
        val stroke = 3.2f.dp.toPx()

        val shieldPath = Path().apply {
            moveTo(w / 2, p)
            lineTo(w - p, h * 0.18f)
            lineTo(w - p, h * 0.52f)
            cubicTo(w - p, h * 0.78f, w * 0.72f, h * 0.92f, w / 2, h - p)
            cubicTo(w * 0.28f, h * 0.92f, p, h * 0.78f, p, h * 0.52f)
            lineTo(p, h * 0.18f)
            close()
        }

        drawPath(
            path = shieldPath,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        if (showCheck) {
            val checkPath = Path().apply {
                moveTo(w * 0.32f, h * 0.48f)
                lineTo(w * 0.45f, h * 0.62f)
                lineTo(w * 0.68f, h * 0.38f)
            }
            drawPath(
                path = checkPath,
                color = color,
                style = Stroke(width = stroke * 1.3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

@Composable
private fun MiniShieldIcon(modifier: Modifier = Modifier, color: Color) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val p = w * 0.08f
        val stroke = 2.2f.dp.toPx()

        val path = Path().apply {
            moveTo(w / 2, p)
            lineTo(w - p, h * 0.2f)
            lineTo(w - p, h * 0.5f)
            cubicTo(w - p, h * 0.75f, w * 0.7f, h * 0.9f, w / 2, h - p)
            cubicTo(w * 0.3f, h * 0.9f, p, h * 0.75f, p, h * 0.5f)
            lineTo(p, h * 0.2f)
            close()
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
private fun SecureBanner(state: ConnectionState) {
    val connected = state == ConnectionState.CONNECTED
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardDark)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .background(NeonGreen.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            MiniShieldIcon(modifier = Modifier.size(22.dp), color = if (connected) NeonGreen else TextSecondary)
        }
        Spacer(Modifier.width(14.dp))
        Text(
            if (connected) "اتصال شما امن است" else "برای اتصال امن، وصل شوید",
            color = AccentWhite,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun ServerInfoRow(config: VpnConfig?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(CardDark)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(CardDarker),
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
        Text(
            config?.countryName ?: "در حال انتخاب سرور",
            color = AccentWhite,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = AccentWhite.copy(alpha = 0.45f), modifier = Modifier.size(26.dp))
    }
}

@Composable
private fun DotMatrixBackground(modifier: Modifier = Modifier, dotColor: Color) {
    Canvas(modifier = modifier) {
        val spacing = 13.dp.toPx()
        val dotRadius = 1.1f

        val rows = (size.height / spacing).toInt() + 2
        val cols = (size.width / spacing).toInt() + 2

        for (row in 0 until rows) {
            for (col in 0 until cols) {
                val x = col * spacing
                val y = row * spacing

                val v1 = kotlin.math.sin(x * 0.0075f) * kotlin.math.cos(y * 0.0085f)
                val v2 = kotlin.math.sin(x * 0.014f + 1.5f) * kotlin.math.cos(y * 0.011f + 2.5f)
                val v3 = kotlin.math.sin((x + y) * 0.005f)
                val value = (v1 + v2 * 0.6f + v3 * 0.4f) / 2f

                if (value > 0.12f) {
                    val alpha = (0.15f + value * 0.4f).coerceIn(0.15f, 0.55f)
                    drawCircle(
                        color = dotColor.copy(alpha = alpha),
                        radius = dotRadius,
                        center = Offset(x, y)
                    )
                }
            }
        }
    }
}
