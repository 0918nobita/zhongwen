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
