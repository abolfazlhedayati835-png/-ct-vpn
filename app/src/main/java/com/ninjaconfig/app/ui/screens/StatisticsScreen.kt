package com.ninjaconfig.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.ninjaconfig.app.ui.theme.BgBlack
import com.ninjaconfig.app.ui.theme.TextSecondary

@Composable
fun StatisticsScreen() {
    Box(
        modifier = Modifier.fillMaxSize().background(BgBlack).padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("آمار مصرف به‌زودی اضافه میشه", color = TextSecondary)
    }
}
