package iuliiaponomareva.eventum.viewModels

import androidx.lifecycle.*
import iuliiaponomareva.eventum.ChannelRepository
import iuliiaponomareva.eventum.data.Channel
import kotlinx.coroutines.launch

class ChannelViewModel(private val repository: ChannelRepository) : ViewModel() {
    val channels: LiveData<Event<List<Channel>>> = repository.load()
        .map { channels -> Event(channels) }

    private val _error = MutableLiveData<Event<ChannelError>>()
    val error: LiveData<Event<ChannelError>>
        get() = _error

    fun addChannel(url: String) {
        val checkedUrl = checkURL(url)
        if (channels.value?.getEvent()?.map {it.url } ?.contains(checkedUrl) == true) {
            val alreadyExists = ChannelError.ALREADY_EXISTS
            alreadyExists.url = checkedUrl
            _error.postValue(Event(alreadyExists))
        } else {
            viewModelScope.launch {
                val channel = repository.add(checkedUrl)
                if (channel == null) {
                    _error.postValue(Event(ChannelError.ERROR_ADDING))
                }
            }
        }
    }

    fun deleteChannel(channel: Channel) {
        viewModelScope.launch {
            repository.delete(channel)
        }
    }

    private fun checkURL(url: String): String {
        if (!url.startsWith("http")) {
            return "http://$url"
        }
        return url
    }

}

enum class ChannelError {
    ALREADY_EXISTS, ERROR_ADDING;
    lateinit var url: String
}
