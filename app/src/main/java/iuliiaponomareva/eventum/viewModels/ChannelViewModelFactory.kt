package iuliiaponomareva.eventum.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import iuliiaponomareva.eventum.ChannelRepository

class ChannelViewModelFactory(private val channelRepository: ChannelRepository): ViewModelProvider.NewInstanceFactory() {
    override fun <T : ViewModel> create(modelClass: Class<T>): T = ChannelViewModel(
        channelRepository
    ) as T
}