package engineer.kodai.yanniang.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import engineer.kodai.yanniang.model.Word
import engineer.kodai.yanniang.model.sampleWords

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordDetailScreen(
    wordId: Int,
    onBack: () -> Unit,
) {
    val word = sampleWords.first { it.id == wordId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(word.hanzi) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            contentAlignment = Alignment.Center,
        ) {
            FlipCard(word = word)
        }
    }
}

// ===== remember と再コンポーズのデモ =====
//
// @Composable 関数は「状態が変わるたびに再実行」される（再コンポーズ）。
// ただし、ローカル変数はリセットされてしまう。
// remember { } で囲むと、再コンポーズをまたいで値が保持される。
//
// var flipped by remember { mutableStateOf(false) }
//   └─ mutableStateOf: Compose が監視できるミュータブルな値
//   └─ by: getValue/setValue の委譲。flipped で直接 Boolean にアクセスできる
//   └─ remember: 初回コンポーズ時にのみラムダを実行し、値をキャッシュする

@Composable
private fun FlipCard(word: Word) {
    var flipped by remember { mutableStateOf(false) }

    // animateFloatAsState: flipped が変わると 0f→180f のアニメーションが走る。
    // これも再コンポーズを駆動する State の一種。
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "card_flip",
    )

    Card(
        onClick = { flipped = !flipped },
        modifier = Modifier
            .size(280.dp, 180.dp)
            .graphicsLayer { rotationY = rotation },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            if (rotation <= 90f) {
                // 表面: 漢字とピンイン
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(text = word.hanzi, style = MaterialTheme.typography.displayMedium)
                    Text(text = word.pinyin, style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                // 裏面: 日本語の意味（180度回転して文字を正立させる）
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationY = 180f },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = word.meaning, style = MaterialTheme.typography.headlineMedium)
                }
            }
        }
    }
}
