package engineer.kodai.yanniang

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging

val httpClient = HttpClient(OkHttp) {
    install(Logging) {
        level = LogLevel.ALL
    }

    install(HttpTimeout) {
        // サーバへの TCP 接続が確立するまでの制限時間
        // サーバが存在しない IP にアクセスしようとした場合や、
        // ファイアウォールでブロックされている場合はここで打ち切られる
        connectTimeoutMillis = 15_000

        // 接続確立後の、パケットとパケットの間隔に対する制限時間
        // 大きなレスポンスを少しずつ受信している場合、パケットが途絶えてから
        // この時間が経過するとタイムアウトとなる
        socketTimeoutMillis = 15_000

        // リクエスト開始から完了までのトータルの制限時間
        // 接続・送信・受信すべてを含む
        requestTimeoutMillis = 15_000
    }
}
