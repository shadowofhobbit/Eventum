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

    fun getChannels(): LiveData<List<Channel>> {
        return channels
    }

    fun onChannelsChanged() = launchLoadingChannels()

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

}

class ChannelViewModelFactory(private val channelRepository: ChannelRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ChannelViewModel(channelRepository) as T
}