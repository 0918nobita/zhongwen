package engineer.kodai.yanniang

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

val Context.settingsDataStore by preferencesDataStore("settings")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // アプリの描画領域をシステムバー (ステータスバー・ナビゲーションバー) の背後まで拡張する
        enableEdgeToEdge()

        val settingsRepository = SettingsRepositoryImpl(applicationContext.settingsDataStore)
        val postRepository = PostRepositoryImpl(httpClient)
        val profileViewModelFactory = ProfileViewModel.Factory(settingsRepository)
        val factory = PostViewModel.Factory(postRepository)

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
                    NameSection(viewModel(factory = profileViewModelFactory))
                    Text(text = "好久不见！")
                    ComposableFunc(viewModel(factory = factory))
                }
            }
        }
    }
}

@Composable
fun NameSection(profileViewModel: ProfileViewModel) {
    val persistedName by profileViewModel.name.collectAsStateWithLifecycle()

    when (val currentName = persistedName) {
        null -> Text("全力加载中")
        else -> NameField(initialValue = currentName, profileViewModel)
    }
}

@Composable
fun NameField(initialValue: String, profileViewModel: ProfileViewModel) {
    var newName by remember { mutableStateOf(initialValue) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = newName,
            onValueChange = {
                newName = it
            },
            singleLine = true,
            placeholder = { Text("输入你的用户名") },
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                profileViewModel.updateName(newName)
            }
        ) {
            Text("保存")
        }
    }
}

@Composable
fun SwitchSample() {
    var checked by remember { mutableStateOf(false) }

    Switch(checked = checked, onCheckedChange = { checked = !checked })
}

// viewModel() が返す ViewModel インスタンスは Activity の ViewModelStore で管理され、
// 再コンポーズが発生しても同一のインスタンスが再利用される
@Composable
fun ComposableFunc(viewModel: PostViewModel) {
    var isVisible by remember { mutableStateOf(false) }
    val viewModelState by viewModel.state.collectAsStateWithLifecycle()

    val onClick = {
        isVisible = true

        viewModel.fetchPost()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = onClick) {
            Text("按钮")
        }
        if (isVisible) {
            Text("按钮被点击了")
        }
        when (val state = viewModelState) {
            is PostViewModelState.Initial -> Text("还没加载")
            is PostViewModelState.Loading -> Text("全力加载中")
            is PostViewModelState.Success -> Text(state.post.title)
            is PostViewModelState.Failure -> Text("加载失败了")
        }
    }
}
