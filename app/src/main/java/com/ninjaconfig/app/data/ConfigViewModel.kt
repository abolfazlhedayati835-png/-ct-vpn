package com.ninjaconfig.app.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

sealed class ConfigsUiState {
    object Loading : ConfigsUiState()
    data class Loaded(val groups: List<CountryGroup>) : ConfigsUiState()
    data class Error(val message: String) : ConfigsUiState()
}

class ConfigViewModel(
    private val repository: ConfigRepository = ConfigRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<ConfigsUiState>(ConfigsUiState.Loading)
    val uiState: StateFlow<ConfigsUiState> = _uiState

    // Firestore requires reaching Google's servers, which may be unreachable
    // without an already-working VPN connection. These two lists are merged
    // so the user always has something to connect with even before Firestore
    // becomes reachable (see GithubConfigFetcher.kt for details).
    private var firestoreConfigs: List<VpnConfig> = emptyList()
    private var githubConfigs: List<VpnConfig> = emptyList()
    private var githubRetryJob: Job? = null

    init {
        fetchGithubWithRetry()

        repository.observeConfigs()
            .onEach { configs ->
                firestoreConfigs = configs
                publish()
            }
            .catch { e ->
                // Firestore failed entirely (e.g. blocked) - fall back to
                // whatever GitHub gave us instead of showing an error.
                if (githubConfigs.isNotEmpty()) {
                    publish()
                } else {
                    _uiState.value = ConfigsUiState.Error(e.message ?: "خطای ناشناخته")
                }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Fetches the GitHub fallback list, retrying automatically with backoff if it
     * fails or comes back empty (e.g. no internet yet right after a ShareIt/offline
     * transfer, or the host being briefly unreachable). Stops once it gets a
     * non-empty result. Safe to call again any time (e.g. from onResume) - it
     * cancels any retry loop already in flight first.
     */
    fun fetchGithubWithRetry() {
        githubRetryJob?.cancel()
        githubRetryJob = viewModelScope.launch {
            val delaysMs = listOf(1000L, 2000L, 4000L, 8000L, 15000L, 30000L)
            var attempt = 0
            while (true) {
                val result = withContext(Dispatchers.IO) { GithubConfigFetcher.fetchConfigs() }
                if (result.isNotEmpty()) {
                    githubConfigs = result
                    publish()
                    return@launch
                }
                if (attempt >= delaysMs.size) {
                    // Give up automatic retries after ~1 minute of trying; the user
                    // (or a future manual refresh call) can trigger fetchGithubWithRetry() again.
                    return@launch
                }
                delay(delaysMs[attempt])
                attempt++
            }
        }
    }

    /** Call when the app returns to the foreground to pick up connectivity that wasn't there at launch. */
    fun refresh() {
        fetchGithubWithRetry()
    }

    private fun publish() {
        val merged = (firestoreConfigs + githubConfigs).distinctBy { it.configLink }
        _uiState.value = ConfigsUiState.Loaded(merged.groupedByCountry())
    }

    fun addConfig(config: VpnConfig) {
        viewModelScope.launch { repository.addConfig(config) }
    }

    fun updateConfig(config: VpnConfig) {
        viewModelScope.launch { repository.updateConfig(config) }
    }

    fun deleteConfig(id: String) {
        viewModelScope.launch { repository.deleteConfig(id) }
    }
}
