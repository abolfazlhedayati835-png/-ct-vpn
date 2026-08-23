package com.ninjaconfig.app.data

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

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

    init {
        repository.observeConfigs()
            .onEach { configs -> _uiState.value = ConfigsUiState.Loaded(configs.groupedByCountry()) }
            .catch { e -> _uiState.value = ConfigsUiState.Error(e.message ?: "خطای ناشناخته") }
            .launchIn(viewModelScope)
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
