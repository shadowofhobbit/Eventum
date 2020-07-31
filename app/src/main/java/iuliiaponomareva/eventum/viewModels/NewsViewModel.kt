package iuliiaponomareva.eventum.viewModels

import android.app.Application
import androidx.lifecycle.*
import iuliiaponomareva.eventum.NewsRepository
import iuliiaponomareva.eventum.data.News
import iuliiaponomareva.eventum.data.ReaderDatabase
import kotlinx.coroutines.launch

class NewsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: NewsRepository
    private val allNews: LiveData<List<News>>
    val n: LiveData<Map<String, List<News>>>

    init {
        val newsDao = ReaderDatabase.getDatabase(application).newsDao()
        repository = NewsRepository(newsDao)
        allNews = repository.loadAll()
        n = allNews.map { x -> x.groupBy { it.channelUrl } }
    }

    fun refreshNews(urls: Array<String>) {
        viewModelScope.launch {
           repository.refreshNews(urls)
        }
    }
}