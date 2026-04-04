package engineer.kodai.yanniang

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import engineer.kodai.yanniang.ui.screen.WordDetailScreen
import engineer.kodai.yanniang.ui.screen.WordListScreen
import engineer.kodai.yanniang.ui.theme.YanNiangTheme

// ===== Compose Navigation の仕組み =====
//
// NavController: 現在のバックスタック（画面履歴）を管理するオブジェクト。
//   navigate("detail/1") で画面遷移、popBackStack() で戻る。
//
// NavHost: ルートとして機能する Composable。
//   どのルート (route) にいるかに応じて、対応する composable { } ブロックを表示する。
//
// route: 画面を識別する文字列。URL のパス設計と同じ感覚で定義する。
//   例: "word_list"、"word_detail/{wordId}"
//
// 引数の渡し方:
//   パスパラメータ "word_detail/{wordId}" → navArgument で型定義 → backStackEntry で取得

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            YanNiangTheme {
                // rememberNavController: NavController を remember で保持する
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "word_list",
                ) {
                    composable("word_list") {
                        WordListScreen(
                            onWordClick = { wordId ->
                                navController.navigate("word_detail/$wordId")
                            }
                        )
                    }
                    composable("word_detail/{wordId}") { backStackEntry ->
                        val wordId = backStackEntry.arguments
                            ?.getString("wordId")
                            ?.toInt() ?: return@composable
                        WordDetailScreen(
                            wordId = wordId,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }
            }
        }
    }
}
