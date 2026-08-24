package com.ninjaconfig.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninjaconfig.app.ui.theme.AccentWhite
import com.ninjaconfig.app.ui.theme.BgBlack
import com.ninjaconfig.app.ui.theme.NeonGreen
import com.ninjaconfig.app.ui.theme.TextSecondary

enum class BottomTab { HOME, SERVERS, STATISTICS, SETTINGS }

@Composable
fun BottomNavBar(current: BottomTab, onSelect: (BottomTab) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BgBlack)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        NavItem("خانه", Icons.Filled.Home, current == BottomTab.HOME) { onSelect(BottomTab.HOME) }
        NavItem("سرورها", Icons.Filled.Public, current == BottomTab.SERVERS) { onSelect(BottomTab.SERVERS) }
        NavItem("آمار", Icons.Filled.BarChart, current == BottomTab.STATISTICS) { onSelect(BottomTab.STATISTICS) }
        NavItem("تنظیمات", Icons.Filled.Settings, current == BottomTab.SETTINGS) { onSelect(BottomTab.SETTINGS) }
    }
}

@Composable
private fun NavItem(label: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    val color = if (selected) NeonGreen else TextSecondary
    androidx.compose.foundation.layout.Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(22.dp))
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
