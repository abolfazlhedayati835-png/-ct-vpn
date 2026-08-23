package com.ninjaconfig.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninjaconfig.app.data.VpnConfig
import com.ninjaconfig.app.ui.theme.*

/**
 * Very lightweight gate so a random user browsing the app can't reach the
 * admin form. This is NOT real security by itself — see README.md for how
 * to also lock down Firestore write rules so a decompiled APK can't write
 * either. Change ADMIN_PIN before you ship.
 */
const val ADMIN_PIN = "1234"

@Composable
fun AdminScreen(
    configs: List<VpnConfig>,
    onBack: () -> Unit,
    onAdd: (VpnConfig) -> Unit,
    onUpdate: (VpnConfig) -> Unit,
    onDelete: (String) -> Unit
) {
    var unlocked by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    if (!unlocked) {
        Column(
            Modifier
                .fillMaxSize()
                .background(BgBlack)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("ورود ادمین", color = AccentWhite, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = pinInput,
                onValueChange = { pinInput = it; pinError = false },
                label = { Text("پین ادمین") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                isError = pinError,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = AccentWhite,
                    unfocusedTextColor = AccentWhite,
                )
            )
            if (pinError) {
                Spacer(Modifier.height(6.dp))
                Text("پین اشتباهه", color = Color.Red)
            }
            Spacer(Modifier.height(16.dp))
            Button(onClick = {
                if (pinInput == ADMIN_PIN) unlocked = true else pinError = true
            }) {
                Text("ورود")
            }
            Spacer(Modifier.height(8.dp))
            TextButton(onClick = onBack) { Text("بازگشت", color = TextSecondary) }
        }
        return
    }

    var showForm by remember { mutableStateOf(false) }
    var editingConfig by remember { mutableStateOf<VpnConfig?>(null) }

    Column(Modifier.fillMaxSize().background(BgBlack).padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = AccentWhite)
            }
            Text("مدیریت کانفیگ‌ها", color = AccentWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = { editingConfig = null; showForm = true }) {
            Text("+ کانفیگ جدید")
        }
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(configs) { config ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CardDark)
                        .clickable { editingConfig = config; showForm = true }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("${config.countryName} · ${config.protocol}", color = AccentWhite, fontWeight = FontWeight.SemiBold)
                        Text(config.label.ifBlank { "بدون برچسب" }, color = TextSecondary, fontSize = 12.sp)
                    }
                    IconButton(onClick = { onDelete(config.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                    }
                }
            }
        }
    }

    if (showForm) {
        ConfigFormDialog(
            initial = editingConfig,
            onDismiss = { showForm = false },
            onSave = { config ->
                if (editingConfig == null) onAdd(config) else onUpdate(config.copy(id = editingConfig!!.id))
                showForm = false
            }
        )
    }
}

@Composable
private fun ConfigFormDialog(
    initial: VpnConfig?,
    onDismiss: () -> Unit,
    onSave: (VpnConfig) -> Unit
) {
    var countryCode by remember { mutableStateOf(initial?.countryCode ?: "") }
    var countryName by remember { mutableStateOf(initial?.countryName ?: "") }
    var protocol by remember { mutableStateOf(initial?.protocol ?: "vmess") }
    var configLink by remember { mutableStateOf(initial?.configLink ?: "") }
    var label by remember { mutableStateOf(initial?.label ?: "") }
    var isPremium by remember { mutableStateOf(initial?.isPremium ?: false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "کانفیگ جدید" else "ویرایش کانفیگ") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = countryCode, onValueChange = { countryCode = it.uppercase() }, label = { Text("کد کشور (مثلاً DE)") })
                OutlinedTextField(value = countryName, onValueChange = { countryName = it }, label = { Text("نام کشور (مثلاً Germany)") })
                OutlinedTextField(value = protocol, onValueChange = { protocol = it }, label = { Text("پروتکل (vmess/vless/shadowsocks)") })
                OutlinedTextField(value = configLink, onValueChange = { configLink = it }, label = { Text("لینک کانفیگ") })
                OutlinedTextField(value = label, onValueChange = { label = it }, label = { Text("برچسب (اختیاری)") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPremium, onCheckedChange = { isPremium = it })
                    Text("پرمیوم")
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    VpnConfig(
                        countryCode = countryCode,
                        countryName = countryName,
                        protocol = protocol,
                        configLink = configLink,
                        label = label,
                        isPremium = isPremium
                    )
                )
            }) { Text("ذخیره") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("لغو") }
        }
    )
}
