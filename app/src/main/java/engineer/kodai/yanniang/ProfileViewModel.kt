package engineer.kodai.yanniang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val settingsRepository: SettingsRepository
): ViewModel() {
    val name =
        settingsRepository
            .name
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = null
            )

    fun updateName(newName: String) {
        viewModelScope.launch {
            settingsRepository.setName(newName)
        }
    }

    class Factory(val settingsRepository: SettingsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(settingsRepository) as T
        }
    }
}
