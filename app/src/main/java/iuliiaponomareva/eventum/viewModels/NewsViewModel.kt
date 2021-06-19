package iuliiaponomareva.eventum.viewModels

import android.app.Application
import androidx.lifecycle.*
import iuliiaponomareva.eventum.NewsRepository
import iuliiaponomareva.eventum.data.News
import iuliiaponomareva.eventum.data.ReaderDatabase
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NewsRepository
    val allNews: LiveData<Map<String, List<News>>>

    init {
        val newsDao = ReaderDatabase.getDatabase(application).newsDao()
        repository = NewsRepository(newsDao)
        this.allNews = repository.loadAll()
            .map { news -> news.groupBy { it.channelUrl } }
    }

    fun refreshNews(urls: Array<String>) {
        viewModelScope.launch {
           repository.refreshNews(urls)
        }
    }
}