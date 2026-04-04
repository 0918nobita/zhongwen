package engineer.kodai.yanniang.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import engineer.kodai.yanniang.model.Word
import engineer.kodai.yanniang.viewmodel.WordListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordListScreen(
    onWordClick: (Int) -> Unit,
    // viewModel() は Composable スコープで ViewModel を取得するヘルパー。
    // Activity/Fragment のライフサイクルに紐づいたインスタンスを返す。
    vm: WordListViewModel = viewModel(),
) {
    // collectAsStateWithLifecycle: StateFlow を Compose の State に変換する。
    // ライフサイクルを考慮し、画面が非表示のときは収集を停止する。
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("単語一覧") }) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(innerPadding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.words, key = { it.id }) { word ->
                    WordItem(word = word, onClick = { onWordClick(word.id) })
                }
            }
        }
    }
}

@Composable
private fun WordItem(word: Word, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = word.hanzi)
            Text(text = word.pinyin)
        }
    }
}
