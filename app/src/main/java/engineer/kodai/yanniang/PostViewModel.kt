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

sealed class PostViewModelState {
    object Initial: PostViewModelState()
    object Loading: PostViewModelState()
    data class Success(val post: Post): PostViewModelState()
    data class Failure(val err: Exception): PostViewModelState()
}

private val logger = KotlinLogging.logger {}

class PostViewModel : ViewModel() {
    private val _state = MutableStateFlow<PostViewModelState>(PostViewModelState.Initial)
    val state = _state.asStateFlow()

    fun fetchPost() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = PostViewModelState.Loading

            try {
                val resJson =
                    httpClient
                        .get("https://jsonplaceholder.typicode.com/posts/1")
                        .bodyAsText()

                val post: Post = Json.decodeFromString(resJson)
                logger.info { post }

                _state.value = PostViewModelState.Success(post)
            } catch (e: Exception) {
                _state.value = PostViewModelState.Failure(e)
            }
        }
    }
}
