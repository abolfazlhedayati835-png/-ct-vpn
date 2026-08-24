package com.ninjaconfig.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.ninjaconfig.app.data.ConfigViewModel
import com.ninjaconfig.app.data.ConfigsUiState
import com.ninjaconfig.app.data.VpnConfig
import com.ninjaconfig.app.ui.screens.AdminScreen
import com.ninjaconfig.app.ui.screens.ConnectScreen
import com.ninjaconfig.app.ui.screens.ConnectionState
import com.ninjaconfig.app.ui.theme.NinjaConfigTheme
import kotlinx.coroutines.delay

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

private enum class Screen { MAIN, ADMIN }

@Composable
private fun AppRoot() {
    val viewModel = remember { ConfigViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    var screen by remember { mutableStateOf(Screen.MAIN) }
    var selectedConfig by remember { mutableStateOf<VpnConfig?>(null) }
    var connectionState by remember { mutableStateOf(ConnectionState.DISCONNECTED) }

    // Auto-pick a config for the user once the list loads - no manual server picker.
    LaunchedEffect(uiState) {
        val loaded = uiState as? ConfigsUiState.Loaded
        if (selectedConfig == null) {
            val allConfigs = loaded?.groups?.flatMap { it.configs } ?: emptyList()
            selectedConfig = allConfigs.randomOrNull()
        }
    }

    LaunchedEffect(connectionState) {
        if (connectionState == ConnectionState.CONNECTING) {
            delay(1200)
            connectionState = ConnectionState.CONNECTED
        }
    }

    when (screen) {
        Screen.ADMIN -> {
            val currentConfigs = (uiState as? ConfigsUiState.Loaded)?.groups?.flatMap { it.configs } ?: emptyList()
            AdminScreen(
                configs = currentConfigs,
                onBack = { screen = Screen.MAIN },
                onAdd = { viewModel.addConfig(it) },
                onUpdate = { viewModel.updateConfig(it) },
                onDelete = { viewModel.deleteConfig(it) }
            )
        }
        Screen.MAIN -> {
            ConnectScreen(
                selectedConfig = selectedConfig,
                connectionState = connectionState,
                onToggleConnect = {
                    connectionState = when (connectionState) {
                        ConnectionState.DISCONNECTED -> ConnectionState.CONNECTING
                        ConnectionState.CONNECTING -> ConnectionState.DISCONNECTED
                        ConnectionState.CONNECTED -> ConnectionState.DISCONNECTED
                    }
                },
                onMenuClick = { screen = Screen.ADMIN }
            )
        }
    }
}
