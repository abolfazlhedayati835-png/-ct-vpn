package com.ninjaconfig.app.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninjaconfig.app.data.VpnConfig
import com.ninjaconfig.app.ui.theme.AccentWhite
import com.ninjaconfig.app.ui.theme.TextSecondary
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.graphics.Bitmap

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfigDetailSheet(config: VpnConfig, onDismiss: () -> Unit) {
    val context = LocalContext.current
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(config.countryName, color = AccentWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(config.protocol.uppercase(), color = TextSecondary)
            Spacer(Modifier.height(16.dp))

            val qrBitmap = remember(config.configLink) { generateQrBitmap(config.configLink) }
            qrBitmap?.let {
                Image(bitmap = it.asImageBitmap(), contentDescription = "QR", modifier = Modifier.size(220.dp))
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = { copyToClipboard(context, config.configLink) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("کپی لینک کانفیگ")
            }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = {
                    // Best-effort: try to hand the link off to a VPN client app (e.g. v2rayNG)
                    // that's already installed, since this app itself doesn't tunnel traffic.
                    copyToClipboard(context, config.configLink)
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse(config.configLink)
                    }
                    runCatching { context.startActivity(intent) }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("اتصال با اپ VPN")
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "لینک کپی شد. اگه اپ VPN (مثل v2rayNG) نصب دارید، کانفیگ رو داخلش Import کنید.",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("config", text))
}

private fun generateQrBitmap(content: String): Bitmap? {
    if (content.isBlank()) return null
    return runCatching {
        val size = 512
        val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bmp.setPixel(x, y, if (bits.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        bmp
    }.getOrNull()
}
