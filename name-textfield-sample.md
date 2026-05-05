# Preferences DataStore × Compose: 永続化された名前の TextField 表示

`SettingsRepository` で永続化されている `name`（空文字でなければ既存値）を 1 行の TextField のデフォルト値として表示し、後から保存処理を足せるようにする手順。

---

## 前提

すでに以下が実装されている：

- `SettingsRepository`（`name: Flow<String>` / `setName(newName: String)`）
- `SettingsRepositoryImpl(dataStore: DataStore<Preferences>)`
- `ProfileViewModel`（`name` を Flow のまま公開）
- `app/build.gradle.kts` に `androidx.datastore:datastore-preferences` を導入済み

---

## ハマりポイント

```kotlin
// NG パターン
val name by viewModel.name.collectAsState(initial = "")
var text by remember { mutableStateOf(name) }  // 後から永続値が来ても反映されない
```

- `collectAsState` は最初のフレームでは初期値（空文字）しか返さない
- `remember { mutableStateOf(...) }` はキーが変わらないかぎり初期化をやり直さない

→ 「ロード前」と「ロード済みで空文字」を区別する必要がある。

---

## Step 1: `Context.dataStore` 拡張プロパティを追加

`SettingsRepository.kt` のクラス宣言の外に追加する。

```kotlin
import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.settingsDataStore by preferencesDataStore(name = "settings")
```

`preferencesDataStore` は内部で `applicationContext` に変換するので、リーク安全。

---

## Step 2: ViewModel でロード状態を表現する

`null = まだ読めていない` として `StateFlow<String?>` で公開する。

```kotlin
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val name: StateFlow<String?> = settingsRepository.name
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun updateName(newName: String) {
        viewModelScope.launch { settingsRepository.setName(newName) }
    }

    class Factory(private val settingsRepository: SettingsRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return ProfileViewModel(settingsRepository) as T
        }
    }
}
```

`SettingsRepository.name` は空文字を初期値に返すため、ViewModel 側で `null` 初期値の `stateIn` を挟まないと「永続値が空文字」と「ロード前」を区別できない。

---

## Step 3: Composable はロード後にだけ TextField を出す

```kotlin
@Composable
fun NameSection(viewModel: ProfileViewModel) {
    val persistedName by viewModel.name.collectAsState()

    when (val current = persistedName) {
        null -> Text("読み込み中...")
        else -> NameField(initialValue = current)
    }
}

@Composable
fun NameField(initialValue: String) {
    var text by remember { mutableStateOf(initialValue) }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        singleLine = true,
        label = { Text("名前") },
        placeholder = { Text("名前を入力") },
    )
}
```

`NameField` は `current != null` のときだけコンポーズされるので、`remember { mutableStateOf(initialValue) }` が「永続値で 1 回だけ初期化」を正しく実現する。

永続値が `""` のときも単に `""` で初期化されるだけで OK。`OutlinedTextField` の `placeholder` が空時のヒント表示を担うため、`isNotEmpty()` の分岐は不要。

---

## Step 4: `MainActivity` で配線する

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val postRepository = PostRepositoryImpl(httpClient)
    val postFactory = PostViewModel.Factory(postRepository)

    // applicationContext を渡す（Activity Context は渡さない）
    val settingsRepository = SettingsRepositoryImpl(applicationContext.settingsDataStore)
    val profileFactory = ProfileViewModel.Factory(settingsRepository)

    setContent {
        Scaffold { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                SwitchSample()
                Text("好久不见！")
                ComposableFunc(viewModel(factory = postFactory))
                NameSection(viewModel(factory = profileFactory))
            }
        }
    }
}
```

---

## Step 5（後から）: 保存処理を追加する

`NameField` にコールバックを足し、保存ボタン（または debounce による自動保存）から呼ぶ。

```kotlin
@Composable
fun NameField(initialValue: String, onSave: (String) -> Unit) {
    var text by remember { mutableStateOf(initialValue) }
    Column {
        OutlinedTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
        )
        Button(onClick = { onSave(text) }) { Text("保存") }
    }
}

// 呼び出し側
NameField(initialValue = current, onSave = viewModel::updateName)
```

保存後 `viewModel.name` は新しい値を流すが、`NameField` のキーは変わらないので `remember` は再初期化されず、ユーザーの編集は打ち消されない。

---

## 実装手順まとめ

1. `SettingsRepository.kt` に `Context.settingsDataStore` 拡張プロパティを追加
2. `ProfileViewModel` の `name` を `StateFlow<String?>`（初期値 `null`）にし、`updateName` を追加
3. `NameSection` / `NameField` を作成（`null` 時は読み込み中表示）
4. `MainActivity` で `SettingsRepositoryImpl(applicationContext.settingsDataStore)` を組み立て、`ProfileViewModel.Factory` 経由で注入
5. 後から `NameField` に `onSave` コールバックを追加して保存処理を配線
