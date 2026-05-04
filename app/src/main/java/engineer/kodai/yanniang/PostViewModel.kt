package engineer.kodai.yanniang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

class PostViewModel : ViewModel() {
    fun fetchPost() {
        viewModelScope.launch(Dispatchers.IO) {
            val text = httpClient.get("https://jsonplaceholder.typicode.com/posts/1").bodyAsText()
            logger.info { text }
        }
    }
}
