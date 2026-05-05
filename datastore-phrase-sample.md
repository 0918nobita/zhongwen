# Proto DataStore サンプル: 中国語フレーズの永続化

中国語フレーズ（中国語・ピンイン・日本語訳のタプル）を複数件ローカルに保存するサンプル実装のメモ。

## 採用技術

- **Proto DataStore** (`androidx.datastore:datastore`)
  - Preferences DataStore はキーバリューのみのため、リスト構造には不向き
  - `.proto` でスキーマを定義し、型安全に扱える

---

## ファイル構成

```
app/
├── build.gradle.kts
├── src/main/proto/
│   └── phrase_list.proto
└── src/main/java/.../
    ├── PhraseSerializer.kt
    └── PhraseRepository.kt
```

---

## Step 1: 依存の追加（`app/build.gradle.kts`）

```kotlin
plugins {
    id("com.google.protobuf") version "0.9.4"
}

dependencies {
    implementation("androidx.datastore:datastore:1.1.1")
    implementation("com.google.protobuf:protobuf-javalite:4.26.1")
    implementation("com.google.protobuf:protobuf-kotlin-lite:4.26.1")
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:4.26.1"
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") { option("lite") }
                create("kotlin") { option("lite") }
            }
        }
    }
}
```

---

## Step 2: スキーマ定義（`src/main/proto/phrase_list.proto`）

```proto
syntax = "proto3";

option java_package = "com.example.yourapp";
option java_multiple_files = true;

message Phrase {
  string chinese = 1;
  string pinyin  = 2;
  string japanese = 3;
}

message PhraseList {
  repeated Phrase phrases = 1;
}
```

`java_package` は実際のパッケージ名に変更すること。

---

## Step 3: シリアライザ（`PhraseSerializer.kt`）

```kotlin
object PhraseSerializer : Serializer<PhraseList> {
    override val defaultValue: PhraseList = PhraseList.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): PhraseList =
        try {
            PhraseList.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", e)
        }

    override suspend fun writeTo(t: PhraseList, output: OutputStream) =
        t.writeTo(output)
}
```

---

## Step 4: リポジトリ（`PhraseRepository.kt`）

```kotlin
val Context.phraseDataStore by dataStore(
    fileName = "phrase_list.pb",
    serializer = PhraseSerializer
)

class PhraseRepository(private val context: Context) {

    val phrases: Flow<List<Phrase>> =
        context.phraseDataStore.data.map { it.phrasesList }

    suspend fun addPhrase(chinese: String, pinyin: String, japanese: String) {
        context.phraseDataStore.updateData { current ->
            val newPhrase = Phrase.newBuilder()
                .setChinese(chinese)
                .setPinyin(pinyin)
                .setJapanese(japanese)
                .build()
            current.toBuilder().addPhrases(newPhrase).build()
        }
    }

    suspend fun clearAll() {
        context.phraseDataStore.updateData { PhraseList.getDefaultInstance() }
    }
}
```

---

## Step 5: ViewModel からの利用例

```kotlin
class PhraseViewModel(private val repo: PhraseRepository) : ViewModel() {
    val phrases: StateFlow<List<Phrase>> = repo.phrases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(chinese: String, pinyin: String, japanese: String) {
        viewModelScope.launch { repo.addPhrase(chinese, pinyin, japanese) }
    }
}
```

---

## 実装手順まとめ

1. `app/build.gradle.kts` を編集（protobuf プラグイン・依存追加）
2. `src/main/proto/phrase_list.proto` を新規作成
3. Gradle Sync → `PhraseList` / `Phrase` クラスが自動生成される
4. `PhraseSerializer.kt` を作成
5. `PhraseRepository.kt` を作成
6. ViewModel・UI から利用
