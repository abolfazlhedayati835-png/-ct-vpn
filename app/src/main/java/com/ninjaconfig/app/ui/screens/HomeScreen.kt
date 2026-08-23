package com.ninjaconfig.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ninjaconfig.app.data.*
import com.ninjaconfig.app.ui.theme.*

@Composable
fun HomeScreen(
    uiState: ConfigsUiState,
    onConfigClick: (VpnConfig) -> Unit,
    onAdminClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        HeaderBar(onAdminClick = onAdminClick)
        Spacer(Modifier.height(16.dp))
        FeaturePills()
        Spacer(Modifier.height(16.dp))

        when (uiState) {
            is ConfigsUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentWhite)
            }
            is ConfigsUiState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(uiState.message, color = TextSecondary)
            }
            is ConfigsUiState.Loaded -> {
                if (uiState.groups.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("هنوز کانفیگی اضافه نشده", color = TextSecondary)
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(uiState.groups) { group ->
                            CountryCard(group = group, onConfigClick = onConfigClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeaderBar(onAdminClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            "NINJA CONFIG",
            color = AccentWhite,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Long-press-free direct access; hide/replace with a hidden gesture if you want it more discreet.
            IconButton(onClick = onAdminClick) {
                Icon(Icons.Filled.Settings, contentDescription = "Admin", tint = AccentWhite)
            }
            Spacer(Modifier.width(4.dp))
        }
    }
}

@Composable
private fun FeaturePills() {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Pill(icon = Icons.Filled.Favorite, text = "Truly Free & Unlimited")
        Pill(icon = Icons.Filled.Bolt, text = "10 Gbps High-Speed")
    }
}

@Composable
private fun Pill(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = AccentWhite, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = AccentWhite, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun CountryCard(group: CountryGroup, onConfigClick: (VpnConfig) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
    ) {
        group.configs.forEachIndexed { index, config ->
            ConfigRow(
                flagCode = group.countryCode,
                countryName = group.countryName,
                config = config,
                onClick = { onConfigClick(config) }
            )
            if (index != group.configs.lastIndex) {
                Divider(color = CardDarker, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
            }
        }
    }
}

@Composable
private fun ConfigRow(
    flagCode: String,
    countryName: String,
    config: VpnConfig,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CardDarker),
            contentAlignment = Alignment.Center
        ) {
            Text(countryCodeToFlagEmoji(flagCode), fontSize = 18.sp)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(countryName, color = AccentWhite, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            if (config.label.isNotBlank()) {
                Text(config.label, color = TextSecondary, fontSize = 12.sp)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
            if (config.isPremium) {
                Icon(Icons.Filled.WorkspacePremium, contentDescription = "Premium", tint = PremiumGold, modifier = Modifier.size(18.dp))
            }
            if (config.supportsGaming) {
                Icon(Icons.Filled.SportsEsports, contentDescription = "Gaming", tint = TextSecondary, modifier = Modifier.size(18.dp))
            }
            Icon(Icons.Filled.Wifi, contentDescription = "Wifi", tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
    }
}
