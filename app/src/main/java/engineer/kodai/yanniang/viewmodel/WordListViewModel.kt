package engineer.kodai.yanniang.viewmodel

import androidx.lifecycle.ViewModel
import engineer.kodai.yanniang.model.Word
import engineer.kodai.yanniang.model.sampleWords
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// UI が表示するデータをまとめた data class。
// これが「UI の状態 (UI State)」。ViewModel が持ち、UI が購読する。
data class WordListUiState(
    val words: List<Word> = emptyList(),
    val isLoading: Boolean = false,
)

class WordListViewModel : ViewModel() {

    // MutableStateFlow: ViewModel 内部からのみ書き換え可能な StateFlow
    // StateFlow: 現在値を持ち、変化を Flow として流す。LiveData の Kotlin 版
    private val _uiState = MutableStateFlow(WordListUiState(isLoading = true))

    // 外部 (UI) には読み取り専用の StateFlow として公開
    val uiState: StateFlow<WordListUiState> = _uiState.asStateFlow()

    init {
        // 本来はここで Repository からデータを取得する
        // 今はサンプルデータを直接セット
        _uiState.value = WordListUiState(words = sampleWords)
    }
}
