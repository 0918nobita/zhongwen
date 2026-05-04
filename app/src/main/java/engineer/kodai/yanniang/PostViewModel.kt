package engineer.kodai.yanniang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Post(val userId: Int, val id: Int, val title: String, val body: String)

private val logger = KotlinLogging.logger {}

class PostViewModel : ViewModel() {
    private val _post = MutableStateFlow<Post?>(null)
    val post = _post.asStateFlow()

    fun fetchPost() {
        viewModelScope.launch(Dispatchers.IO) {
            val resJson =
                httpClient
                    .get("https://jsonplaceholder.typicode.com/posts/1")
                    .bodyAsText()

            val res: Post = Json.decodeFromString(resJson)
            logger.info { res }
            _post.value = res
        }
    }
}
