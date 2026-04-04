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

### 2026-04-04

#### Jetpack Compose 以前：XML によるレイアウト

`res/layout/` ディレクトリ配下で XML 形式で UI を記述し、Activity / Fragment から読み込む

```xml
<!-- res/layout/activity_main.xml -->
<LinearLayout
  xmlns:android="http://schemas.android.com/apk/res/android"
  android:layout_width="match_parent"
  android:layout_height="match_parent"
  android:orientation="vertical"
>
  <TextView
    android:id="@+id/textView"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Hello World"
  />

  <Button
    android:id="@+id/button"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Click Me"
  />
</LinearLayout>
```

```kotlin
// Activity 側
class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main) // XML を inflate する

    val textView = findViewById<TextView>(R.id.textView)
    val button = findViewById<Button>(R.id.button)
    button.setOnClickListener { textView.text = "Clicked!" }
  }
}
```

その後、XML から型安全なバインディングクラスを生成する **View Binding** と、<br>XML 中に式言語 `@{}` を書いて直接バインドする **Data Binding** が登場した

```kotlin
// View Binding の例
binding = ActivityMainBinding.inflate(layoutInflater)
setContentView(binding.root)
binding.button.setOnClickListener { ... }
```

- Activity 単体で画面全体を管理するのは難しいので、画面を **Fragment** に分割して管理するようになった
- Fragment は Activity (画面) を超えて再利用できる
- Fragment にもライフサイクルがあり、Fragment 同士で親子関係をつくれる

#### RecyclerView

ListView の進化版として、スクロール操作中に画面外に行ったビューを再利用することでパフォーマンスを向上させる RecyclerView という機能があったらしい
- ViewHolder: View への参照を維持するコンテナ<br>
  findViewById は内部的に View ツリーを探索する重い処理であり、スクロールのたびに呼び出しが発生するとカクつきの原因になってしまう<br>
  ViewHolder で初回に findViewById して得た View への参照をキャッシュしておき再利用することで、カクつきを防ぐ
  ```kotlin
  class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    // findViewById は一度だけ呼ばれる
    val nameText: TextView = view.findViewById(R.id.name)
    val iconImage: ImageView = view.findViewById(R.id.icon)
  }
  ```
- Adapter: データソース (リスト) と ViewHolder をつなぐ橋渡し役
  ```kotlin
  class UserAdapter(private val users: List<User>) :
    RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    // 1. ViewHolder を新規生成する（XML を inflate して ViewHolder に渡す）
    //    → 最初の数回しか呼ばれない
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
      val view = LayoutInflater.from(parent.context)
        .inflate(R.layout.item_user, parent, false)
      return ViewHolder(view)
    }

    // 2. 再利用される ViewHolder に、対応するデータを書き込む
    //    → スクロールのたびに呼ばれる（ここを軽く保つのが重要）
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
      val user = users[position]
      holder.nameText.text = user.name
      holder.iconImage.setImageResource(user.iconRes)
    }

    // 3. データの件数
    override fun getItemCount() = users.size

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
      val nameText: TextView = view.findViewById(R.id.name)
      val iconImage: ImageView = view.findViewById(R.id.icon)
    }
  }
  ```

Compose では LazyColumn や LazyRow が用意され、ViewHolder / Adapter を自前で用意する必要がなくなった

#### カスタム View

標準 Widget で対応できない独自 UI は、View を継承して `onDraw(canvas: Canvas)` をオーバーライドして描画する

#### View と Fragment の違い

持っている責務の範囲が異なる

- View: 描画だけを担当する
  - 描画ロジック・タッチイベントのハンドラ等を持つ
  - バックスタック (戻るボタンの管理) や ViewModel との接続、「自分が表示中かどうかの管理」は責務の範囲外
- Fragment: 画面の一区画としてのライフサイクルを持つ

ユーザーが「戻る」を押したとき
- View の場合
  - 親の Activity / Fragment が明示的に visibility を変えるなど制御が必要
- Fragment の場合
  - FragmentManager がバックスタックから自動で Fragment を pop し、前の Fragment が復元される。

画面が回転したとき
- View の場合
  - 基本的に再生成される
  - 状態保存するには `onSaveInstanceState` を自前実装する必要がある
- Fragment の場合
  - ViewModel を持てるので、ViewModel に状態を持たせておけば回転後も保持される
  - `retainInstance` という、画面回転などの設定変更後も Fragment インスタンスを保持する機能もあったが、<br>内側の View が破棄されたのに Fragment インスタンスが破棄されていない状態を生み、<br>メモリリークやクラッシュの原因になりやすかったため API 28 で非推奨になった
