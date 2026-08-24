package com.ninjaconfig.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninjaconfig.app.ui.theme.*

@Composable
fun SettingsScreen(onAdminClick: () -> Unit) {
    Column(
        modifier = androidx.compose.ui.Modifier
            .fillMaxSize()
            .background(BgBlack)
            .padding(20.dp)
    ) {
        Text("تنظیمات", color = AccentWhite, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .clickable(onClick = onAdminClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.AdminPanelSettings, contentDescription = null, tint = NeonGreen)
            Spacer(Modifier.width(12.dp))
            Text("مدیریت کانفیگ‌ها (ادمین)", color = AccentWhite, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}
