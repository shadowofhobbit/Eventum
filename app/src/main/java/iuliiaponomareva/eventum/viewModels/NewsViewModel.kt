package iuliiaponomareva.eventum.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import iuliiaponomareva.eventum.NewsRepository
import iuliiaponomareva.eventum.data.News
import iuliiaponomareva.eventum.data.Reader
import kotlinx.coroutines.launch

class NewsViewModel : ViewModel() {
    private val newsRepository = NewsRepository()
    private val _news: MutableLiveData<Map<String, Set<News>>> = MutableLiveData()
    val reader: Reader = Reader()

    val news: LiveData<Map<String, Set<News>>>
        get() {
            return _news
        }

    fun refreshNews(urls: Array<String>) {
        viewModelScope.launch {
            val downloadedNews = newsRepository.refreshNews(urls)
            _news.postValue(downloadedNews)
        }
    }
}