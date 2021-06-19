package iuliiaponomareva.eventum.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import iuliiaponomareva.eventum.R
import iuliiaponomareva.eventum.data.Channel
import kotlinx.android.synthetic.main.drawer_list_item.view.*

class ChannelsAdapter(private val onClickListener: (Channel) -> Unit) : RecyclerView.Adapter<ChannelViewHolder>() {
    private var channels = mutableListOf<Channel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChannelViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = layoutInflater.inflate(R.layout.drawer_list_item, parent, false)
        val holder = ChannelViewHolder(itemView)
        holder.itemView.setOnClickListener {
            onClickListener(channels[holder.bindingAdapterPosition])
        }
        return holder
    }

    override fun onBindViewHolder(holder: ChannelViewHolder, position: Int) {
        holder.itemView.channelView.text = channels[position].title
    }

    override fun getItemCount(): Int = channels.size

    fun clear() {
        val oldSize = channels.size
        channels = mutableListOf()
        notifyItemRangeRemoved(0, oldSize)
    }

    fun remove(feed: Channel) {
        val position = channels.indexOf(feed)
        if (position != -1) {
            channels.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    fun addAll(data: List<Channel>) {
        val start = channels.size
        channels.addAll(data)
        notifyItemRangeInserted(start, data.size)
    }
}

class ChannelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)