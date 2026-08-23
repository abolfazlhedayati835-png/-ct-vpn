package com.ninjaconfig.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.ninjaconfig.app.data.ConfigViewModel
import com.ninjaconfig.app.data.ConfigsUiState
import com.ninjaconfig.app.data.VpnConfig
import com.ninjaconfig.app.ui.screens.AdminScreen
import com.ninjaconfig.app.ui.screens.ConfigDetailSheet
import com.ninjaconfig.app.ui.screens.HomeScreen
import com.ninjaconfig.app.ui.theme.NinjaConfigTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NinjaConfigTheme {
                AppRoot()
            }
        }
    }
}

private enum class Screen { HOME, ADMIN }

@Composable
private fun AppRoot() {
    val viewModel = remember { ConfigViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    var screen by remember { mutableStateOf(Screen.HOME) }
    var selectedConfig by remember { mutableStateOf<VpnConfig?>(null) }

    when (screen) {
        Screen.HOME -> {
            HomeScreen(
                uiState = uiState,
                onConfigClick = { selectedConfig = it },
                onAdminClick = { screen = Screen.ADMIN }
            )
            selectedConfig?.let { config ->
                ConfigDetailSheet(config = config, onDismiss = { selectedConfig = null })
            }
        }
        Screen.ADMIN -> {
            val currentConfigs = (uiState as? ConfigsUiState.Loaded)?.groups?.flatMap { it.configs } ?: emptyList()
            AdminScreen(
                configs = currentConfigs,
                onBack = { screen = Screen.HOME },
                onAdd = { viewModel.addConfig(it) },
                onUpdate = { viewModel.updateConfig(it) },
                onDelete = { viewModel.deleteConfig(it) }
            )
        }
    }
}
