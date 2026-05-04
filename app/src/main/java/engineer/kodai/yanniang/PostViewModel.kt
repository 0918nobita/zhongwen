package engineer.kodai.yanniang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PostViewModel : ViewModel() {
    fun fetchPost() {
        viewModelScope.launch(Dispatchers.IO) {
            httpClient.get("https://jsonplaceholder.typicode.com/posts/1")
        }
    }
}
