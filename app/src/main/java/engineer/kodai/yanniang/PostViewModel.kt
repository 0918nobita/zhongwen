package engineer.kodai.yanniang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class PostViewModelState {
    object Initial: PostViewModelState()
    object Loading: PostViewModelState()
    data class Success(val post: Post): PostViewModelState()
    data class Failure(val cause: Throwable): PostViewModelState()
}

private val logger = KotlinLogging.logger {}

class PostViewModel(private val postRepository: PostRepository) : ViewModel() {
    private val _state = MutableStateFlow<PostViewModelState>(PostViewModelState.Initial)
    val state = _state.asStateFlow()

    fun fetchPost() {
        viewModelScope.launch(Dispatchers.IO) {
            _state.value = PostViewModelState.Loading

            postRepository
                .fetchPost()
                .onSuccess { post ->
                    logger.info { post }
                    _state.value = PostViewModelState.Success(post)
                }
                .onFailure { cause ->
                    _state.value = PostViewModelState.Failure(cause)
                }
        }
    }

    class Factory(val postRepository: PostRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            return PostViewModel(postRepository) as T
        }
    }
}
