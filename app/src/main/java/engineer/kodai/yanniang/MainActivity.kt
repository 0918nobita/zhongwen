package engineer.kodai.yanniang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // アプリの描画領域をシステムバー (ステータスバー・ナビゲーションバー) の背後まで拡張する
        enableEdgeToEdge()

        setContent {
            // Scaffold で innerPadding を得て上下 padding を設定しないと、
            // ステータスバーやナビゲーションバーと UI が重なってしまう
            // TopBar, BottomBar を用意する場合は、Scaffold の引数 topBAr, bottomBar に渡す
            Scaffold { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    SwitchSample()
                    Text(text = "好久不见！")
                    ComposableFunc()
                }
            }
        }
    }
}

@Composable
fun SwitchSample() {
    var checked by remember { mutableStateOf(false) }

    Switch(checked = checked, onCheckedChange = { checked = !checked })
}

@Composable
fun ComposableFunc() {
    var isVisible by remember { mutableStateOf(false) }

    Column {
        Button(onClick = { isVisible = true }) {
            Text("按钮")
        }
        if (isVisible) {
            Text("按钮被点击了")
        }
    }
}
