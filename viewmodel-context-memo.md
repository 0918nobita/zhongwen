# Context / Activity / ViewModel とメモリリーク

Preferences DataStore を ViewModel から扱おうとして、Context のライフサイクルとメモリリークの関係を整理したメモ。

---

## 前提となるライフサイクルの違い

| 要素 | 寿命 |
| --- | --- |
| `Activity` の `Context` | 画面が破棄される（回転・finish 等）と消える |
| `Application` の `Context` | アプリのプロセスが生きている間ずっと |
| `ViewModel` | configuration change（画面回転など）をまたいで生き残る。Activity が `finish` されると初めて破棄される |

ViewModel は **Activity より長生きする可能性がある** のがポイント。

---

## なぜ ViewModel に Activity Context を持たせてはいけないか

ViewModel が Activity の Context を参照していると、画面回転で Activity が破棄されても ViewModel が生きているため、古い Activity が GC されず **メモリリーク** になる。

```kotlin
// NG: Activity を保持している
class BadViewModel(private val activity: Activity) : ViewModel()

// NG になりうる: 渡される Context が Activity だとアウト
class BadViewModel(private val context: Context) : ViewModel() {
    val ds = context.dataStore
}
```

---

## Application Context なら安全

Application はアプリ全体と寿命が同じなので、ViewModel が保持していても問題は起きない。

`val Context.dataStore by preferencesDataStore(name = "settings")` の拡張プロパティは、内部で `applicationContext` に変換した上でアプリ全体に 1 つだけのシングルトンを返す。なので、

- `applicationContext.dataStore` を取り出して ViewModel に渡せば安全
- それを Repository に包んでから渡しても安全

どちらでもリークはしない。

---

## リーク対策の本質と「Repository を挟む」ことの違い

混同しやすいので分けて整理：

### リーク対策の本質
**ViewModel に Activity Context への参照を持たせないこと**。これに尽きる。Repository の有無は関係ない。

### Repository を挟む利点（リーク対策ではない）
1. **ViewModel が Android Framework に依存しなくなる**
   `DataStore` も `androidx.datastore` 寄りの API。Repository でラップして `Flow<String>` のような純粋型だけを露出させると、JVM のユニットテストだけで ViewModel を検証できる。
2. **保存先の差し替えが効く**
   後で「設定の一部はサーバー」「Room に移す」となっても ViewModel を変えずに済む。
3. **キーや変換ロジックの集約**
   `Preferences.Key<*>` の定義や `map { it[KEY] ?: default }` が ViewModel に散らばらない。

---

## ViewModel に Context を渡す典型パターン

### パターン 1: `AndroidViewModel` を使う（手軽）

```kotlin
val Context.dataStore by preferencesDataStore(name = "settings")

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val dataStore = app.dataStore   // app は Application Context
    val flow = dataStore.data.map { it[KEY] ?: "" }
}
```

`AndroidViewModel` の `Application` は Application Context なのでリーク安全。

### パターン 2: Repository を挟む（推奨）

ViewModel は Android 依存を持たなくなり、テストが書きやすい。

```kotlin
class SettingsRepository(private val dataStore: DataStore<Preferences>) {
    val name: Flow<String> = dataStore.data.map { it[NAME_KEY] ?: "" }
    suspend fun setName(v: String) { dataStore.edit { it[NAME_KEY] = v } }
}

class SettingsViewModel(private val repo: SettingsRepository) : ViewModel() { ... }
```

`ViewModelProvider.Factory` で `applicationContext.dataStore` から Repository を組み立てて注入する。

### パターン 3: Hilt などの DI
規模が大きくなったら検討。今は不要。

---

## 覚えておくべき一行

> **Context そのものを ViewModel に持たせない。持たせるなら Application に限る。**
