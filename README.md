# 言娘

## 学習ノート

### 2026-04-03 Android / Kotlin / Compose 基礎

#### Activity

- 画面1枚分の入れ物。OS がライフサイクル（`onCreate` → `onResume` → `onPause` → `onDestroy` 等）を管理する
- Jetpack Compose では **1 Activity・複数 Screen** が標準スタイル。`setContent { }` で Compose に制御を渡したら、以降の画面遷移は Compose Navigation が担う

#### Kotlin の糖衣構文

- **Trailing lambda**: 最後の引数がラムダのとき `()` の外に出せる
- **Default arguments**: デフォルト引数があれば省略可能。両者が組み合わさり `setContent { }` のような簡潔な記述が成立する

#### レシーバー

- JavaScript の `this` に相当する Kotlin の概念
- `T.() -> Unit` の形で「レシーバー付きラムダ」を表す。ブロック内で `this` が `T` のインスタンスになる
- JS の `this` と異なりコンパイル時に型が確定するため、静的に安全

#### `@Composable () -> Unit`

- `@Composable`: Compose のコンパイラプラグインが処理するアノテーション。通常のアノテーション（メタデータ付与）と異なり、生成されるバイトコード自体が変わる
- `@Composable () -> Unit` と `() -> Unit` はコンパイラから見て**別の型**。`@Composable` は型修飾子として機能しており、`@Composable` なコンテキスト外で Composable 関数を呼び出すとコンパイルエラーになる
- この仕組みは **Effect system**（特定の能力を型で表現し、コンテキスト外での呼び出しを制限する）と同じ発想。Compose の設計者も Algebraic Effects を意識して設計したと明言している

#### アノテーション

- `@` はじまりの構文。コンパイラ・ツールにメタデータを付与する
- 複数付与可能。同一アノテーションの重複はデフォルトでコンパイルエラー（`@Repeatable` なら可）
- `@Composable` はコンパイラプラグインが介在するため、メタデータ付与にとどまらずコード変換が起きる例外的なケース

#### `contract { }` と ラベル付き `this`

- `contract { }` はコンパイラへの静的なヒント。「戻り値と引数の間の論理関係」を宣言し、スマートキャスト等の型推論を補助する。実行時には何もしない
- `this@ラベル` でどの階層の `this` かを明示できる。ネストされたラムダ内で外側のレシーバーを参照するときに使う（例: `this@isNotNullOrEmpty`）
- `contract` および内部 DSL（`implies`・`returns` 等）はコンパイラにハードコードされた特別扱い。構文論的には通常の関数と区別がつかないが、コンパイラが名前で識別して解釈している。この設計の歪さは Kotlin チームも認識しており、長年 experimental のままである理由のひとつ

#### 次回

- **Composable 関数**: `@Composable` 関数の基本・再コンポーズの仕組み・`remember` による状態保持
- **ViewModel + StateFlow**: MVVM の核心。UI ロジックをどこに置くか、状態をどう流すか
- **Compose Navigation**: 画面遷移の定義方法と NavHost・NavController の役割

---

### 2026-04-04 再コンポーズ・remember・ViewModel・Navigation

#### 再コンポーズと `remember`

- `@Composable` 関数は「依存する State が変わるたびに再実行」される。これを **再コンポーズ (recomposition)** と呼ぶ
- 再コンポーズ時、関数内のローカル変数は毎回初期化される（通常の関数呼び出しと同じ）
- `remember { mutableStateOf(false) }` で囲んだ値は、再コンポーズをまたいでキャッシュされる
  - `remember`: 初回コンポーズ時のみラムダを実行し、値をノードに紐づけて保持する
  - `mutableStateOf(x)`: Compose が変更を観測できるコンテナ。値が変わると依存する Composable が再コンポーズされる
  - `by` キーワード（委譲プロパティ）と組み合わせると `state.value` でなく `state` として直接アクセスできる

```kotlin
var flipped by remember { mutableStateOf(false) }
// flipped = true にすると、このブロックが再コンポーズされる
```

- `animateFloatAsState` のようなアニメーション API も内部で `remember` + `State` を使っており、再コンポーズのループで少しずつ値を変化させることでアニメーションを実現している

#### ViewModel と StateFlow

| 役割 | クラス |
|---|---|
| UI の状態を保持・公開 | `ViewModel` |
| 変化を Flow で流す | `StateFlow<T>` |
| ViewModel 内部で書き換え | `MutableStateFlow<T>` |
| UI に読み取り専用で公開 | `.asStateFlow()` |

```kotlin
// ViewModel 側
private val _uiState = MutableStateFlow(WordListUiState(isLoading = true))
val uiState: StateFlow<WordListUiState> = _uiState.asStateFlow()

// Composable 側
val uiState by vm.uiState.collectAsStateWithLifecycle()
```

- `collectAsStateWithLifecycle()`: `StateFlow` を Compose の `State` に変換する拡張関数。ライフサイクルを考慮し、画面が非表示（Stopped）のときは Flow の収集を停止してリソースを節約する
- **UI State を data class にまとめる**のが Android の推奨パターン。フィールドが増えても ViewModel の公開 API が `uiState` 一本で済む

#### Compose Navigation

- `rememberNavController()`: `NavController` を `remember` で保持する。バックスタック（画面履歴）を管理する
- `NavHost`: 現在の route に応じて表示する Composable を切り替える入れ物
- `composable("route") { }`: route と Composable を対応付けるエントリ

```kotlin
// パスパラメータを使った画面遷移
NavHost(navController, startDestination = "word_list") {
    composable("word_list") { WordListScreen(...) }
    composable("word_detail/{wordId}") { backStackEntry ->
        val wordId = backStackEntry.arguments?.getString("wordId")?.toInt()
        WordDetailScreen(wordId = wordId!!, ...)
    }
}

// 遷移: navigate でルートを指定
navController.navigate("word_detail/1")
// 戻る: backStack を1つ pop する
navController.popBackStack()
```

- Navigation の引数は文字列として渡り、`backStackEntry.arguments` で取得する。型安全にしたい場合は `navArgument` + `NavType` を使う（より大規模なアプリ向け）

#### 今回作ったもの

- `model/Word.kt` — データクラス + サンプルデータ
- `viewmodel/WordListViewModel.kt` — StateFlow で単語リストを管理
- `ui/screen/WordListScreen.kt` — LazyColumn で単語リスト表示
- `ui/screen/WordDetailScreen.kt` — カードをタップで flip するアニメーション（`remember` + `animateFloatAsState` のデモ）
- `MainActivity.kt` — NavHost で2画面のルーティングを定義

#### 次回

- **Repository パターン**: ViewModel からデータ取得ロジックを分離する
- **Room データベース**: 単語データを端末に永続化する
- **`LaunchedEffect` / `SideEffect`**: Composable の中で副作用（非同期処理など）を扱う方法
