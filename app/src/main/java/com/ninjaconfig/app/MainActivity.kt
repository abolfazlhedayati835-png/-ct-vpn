package com.ninjaconfig.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.ninjaconfig.app.data.ConfigViewModel
import com.ninjaconfig.app.data.ConfigsUiState
import com.ninjaconfig.app.data.VpnConfig
import com.ninjaconfig.app.ui.screens.*
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

private enum class Screen { MAIN, ADMIN }

@Composable
private fun AppRoot() {
    val viewModel = remember { ConfigViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    var screen by remember { mutableStateOf(Screen.MAIN) }
    var tab by remember { mutableStateOf(BottomTab.HOME) }
    var selectedConfig by remember { mutableStateOf<VpnConfig?>(null) }
    var configSheetTarget by remember { mutableStateOf<VpnConfig?>(null) }
    var connectionState by remember { mutableStateOf(ConnectionState.DISCONNECTED) }

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
            Scaffold(
                bottomBar = { BottomNavBar(current = tab, onSelect = { tab = it }) }
            ) { innerPadding ->
                androidx.compose.foundation.layout.Box(Modifier.padding(innerPadding)) {
                    when (tab) {
                        BottomTab.HOME -> {
                            ConnectScreen(
                                selectedConfig = selectedConfig,
                                connectionState = connectionState,
                                onToggleConnect = {
                                    connectionState = when (connectionState) {
                                        ConnectionState.DISCONNECTED -> ConnectionState.CONNECTED
                                        ConnectionState.CONNECTED -> ConnectionState.DISCONNECTED
                                        ConnectionState.CONNECTING -> ConnectionState.DISCONNECTED
                                    }
                                },
                                onChooseServerClick = { tab = BottomTab.SERVERS },
                                onMenuClick = { tab = BottomTab.SETTINGS }
                            )
                        }
                        BottomTab.SERVERS -> {
                            HomeScreen(
                                uiState = uiState,
                                onConfigClick = { config ->
                                    selectedConfig = config
                                    configSheetTarget = config
                                },
                                onAdminClick = { screen = Screen.ADMIN }
                            )
                        }
                        BottomTab.STATISTICS -> StatisticsScreen()
                        BottomTab.SETTINGS -> SettingsScreen(onAdminClick = { screen = Screen.ADMIN })
                    }
                }
            }
        }
    }

    configSheetTarget?.let { config ->
        ConfigDetailSheet(config = config, onDismiss = { configSheetTarget = null })
    }
}
