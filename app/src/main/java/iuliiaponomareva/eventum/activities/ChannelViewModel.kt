package iuliiaponomareva.eventum.activities

import androidx.lifecycle.*
import iuliiaponomareva.eventum.ChannelRepository
import iuliiaponomareva.eventum.data.Channel
import kotlinx.coroutines.launch

class ChannelViewModel(private val repository: ChannelRepository) : ViewModel() {
    private val channels: MutableLiveData<List<Channel>> by lazy {
        MutableLiveData<List<Channel>>().also {
            launchLoadingChannels()
        }
    }

    private val _error = MutableLiveData<Event<ChannelError>>()
    val error: LiveData<Event<ChannelError>>
        get() = _error

    fun getChannels(): LiveData<List<Channel>> {
        return channels
    }

    fun addChannel(url: String) {
        val checkedUrl = checkURL(url)
        if (channels.value?.map {it.url } ?.contains(checkedUrl) == true) {
            val alreadyExists = ChannelError.ALREADY_EXISTS
            alreadyExists.url = checkedUrl
            _error.postValue(Event(alreadyExists))
        } else {
            viewModelScope.launch {
                val channel = repository.add(checkedUrl)
                if (channel == null) {
                    _error.postValue(Event(ChannelError.ERROR_ADDING))
                } else {
                    loadChannels()
                }
            }
        }
    }

    fun deleteChannel(channel: Channel) {
        viewModelScope.launch {
            repository.delete(channel.url)
            loadChannels()
        }
    }

    private fun launchLoadingChannels() {
        viewModelScope.launch {
            loadChannels()
        }
    }

    private suspend fun loadChannels() {
        val loadedChannels = repository.load()
        channels.postValue(loadedChannels)
    }

    private fun checkURL(url: String): String {
        if (!url.startsWith("http")) {
            return "http://$url"
        }
        return url
    }

}

class ChannelViewModelFactory(private val channelRepository: ChannelRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ChannelViewModel(channelRepository) as T
}

enum class ChannelError {
    ALREADY_EXISTS, ERROR_ADDING;
    lateinit var url: String
}
