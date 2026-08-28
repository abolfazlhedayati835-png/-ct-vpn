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
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBlack)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        BackBar(onBack = onBack)
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
private fun BackBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = AccentWhite)
        }
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
            val code = flagCode.trim()
            if (code.length == 2) {
                coil.compose.AsyncImage(
                    model = "https://flagcdn.com/w160/${code.lowercase()}.png",
                    contentDescription = countryName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Text(countryCodeToFlagEmoji(flagCode), fontSize = 18.sp)
            }
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
            Icon(Icons.Filled.Wifi, contentDescription = "Wifi", tint = NeonGreen, modifier = Modifier.size(18.dp))
        }
    }
}
