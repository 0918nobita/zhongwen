package engineer.kodai.yanniang

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Post(
    val userId: Int,
    val id: Int,
    val title: String,
    val body: String,
)

interface PostRepository {
    suspend fun fetchPost(): Result<Post>
}

class PostRepositoryImpl(private val httpClient: HttpClient) : PostRepository {
    override suspend fun fetchPost(): Result<Post> = runCatching {
        val resJson =
            httpClient
                .get("https://jsonplaceholder.typicode.com/posts/1")
                .bodyAsText()

        Json.decodeFromString(resJson)
    }
}
