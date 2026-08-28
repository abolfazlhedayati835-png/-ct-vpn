package com.ninjaconfig.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.VpnService
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.ninjaconfig.app.data.ConfigViewModel
import com.ninjaconfig.app.data.ConfigsUiState
import com.ninjaconfig.app.data.VpnConfig
import com.ninjaconfig.app.ui.screens.AdminScreen
import com.ninjaconfig.app.ui.screens.ConnectScreen
import com.ninjaconfig.app.ui.screens.ConnectionState
import com.ninjaconfig.app.ui.screens.HomeScreen
import com.ninjaconfig.app.ui.theme.NinjaConfigTheme
import com.ninjaconfig.app.vpn.CtVpnService

class MainActivity : ComponentActivity() {

    private var onVpnPermissionResult: ((Boolean) -> Unit)? = null

    private val vpnPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onVpnPermissionResult?.invoke(result.resultCode == RESULT_OK)
    }

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op, notification is best-effort */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            NinjaConfigTheme {
                AppRoot(
                    requestVpnPermission = { onResult ->
                        onVpnPermissionResult = onResult
                        val prepareIntent = VpnService.prepare(this)
                        if (prepareIntent != null) {
                            vpnPermissionLauncher.launch(prepareIntent)
                        } else {
                            onResult(true)
                        }
                    }
                )
            }
        }
    }
}

private enum class Screen { MAIN, SERVERS, ADMIN }

@Composable
private fun AppRoot(requestVpnPermission: (( (Boolean) -> Unit ) -> Unit)) {
    val context = LocalContext.current
    val viewModel = remember { ConfigViewModel() }
    val uiState by viewModel.uiState.collectAsState()

    var screen by remember { mutableStateOf(Screen.MAIN) }
    var selectedConfig by remember { mutableStateOf<VpnConfig?>(null) }
    var userPickedConfig by remember { mutableStateOf(false) }
    var connectionState by remember { mutableStateOf(ConnectionState.DISCONNECTED) }

    // Auto-pick a config once the list loads, but only until the user manually
    // chooses one from the server list - after that we respect their choice.
    LaunchedEffect(uiState) {
        val loaded = uiState as? ConfigsUiState.Loaded
        if (selectedConfig == null && !userPickedConfig) {
            val allConfigs = loaded?.groups?.flatMap { it.configs } ?: emptyList()
            selectedConfig = allConfigs.randomOrNull()
        }
    }

    // Re-fetch the GitHub fallback list whenever the app comes back to the
    // foreground, so returning from ShareIt/offline transfers with no internet
    // yet at launch self-heals once connectivity is actually available.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Listen for real status updates coming from the VPN service.
    DisposableEffect(Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val message = intent?.getStringExtra(CtVpnService.EXTRA_MESSAGE)
                when (intent?.getStringExtra(CtVpnService.EXTRA_STATUS)) {
                    "connected" -> connectionState = ConnectionState.CONNECTED
                    "disconnected" -> connectionState = ConnectionState.DISCONNECTED
                    "error" -> {
                        connectionState = ConnectionState.DISCONNECTED
                        if (!message.isNullOrBlank()) {
                            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                    "diagnostic" -> {
                        if (!message.isNullOrBlank()) {
                            android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
        val filter = IntentFilter(CtVpnService.ACTION_STATUS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            context.registerReceiver(receiver, filter)
        }
        onDispose { context.unregisterReceiver(receiver) }
    }

    fun startVpn(config: VpnConfig) {
        connectionState = ConnectionState.CONNECTING
        requestVpnPermission { granted ->
            if (!granted) {
                connectionState = ConnectionState.DISCONNECTED
                return@requestVpnPermission
            }
            val intent = Intent(context, CtVpnService::class.java).apply {
                action = CtVpnService.ACTION_CONNECT
                putExtra(CtVpnService.EXTRA_CONFIG_LINK, config.configLink)
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }

    fun stopVpn() {
        val intent = Intent(context, CtVpnService::class.java).apply {
            action = CtVpnService.ACTION_DISCONNECT
        }
        context.startService(intent)
        connectionState = ConnectionState.DISCONNECTED
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
        Screen.SERVERS -> {
            HomeScreen(
                uiState = uiState,
                onConfigClick = { config ->
                    selectedConfig = config
                    userPickedConfig = true
                    screen = Screen.MAIN
                },
                onBack = { screen = Screen.MAIN }
            )
        }
        Screen.MAIN -> {
            ConnectScreen(
                selectedConfig = selectedConfig,
                connectionState = connectionState,
                onToggleConnect = {
                    when (connectionState) {
                        ConnectionState.DISCONNECTED -> {
                            selectedConfig?.let { startVpn(it) }
                        }
                        ConnectionState.CONNECTED, ConnectionState.CONNECTING -> stopVpn()
                    }
                },
                onMenuClick = { screen = Screen.ADMIN },
                onServerClick = { screen = Screen.SERVERS }
            )
        }
    }
}
