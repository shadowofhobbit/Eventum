package iuliiaponomareva.eventum.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.AbsListView
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import iuliiaponomareva.eventum.R
import iuliiaponomareva.eventum.data.Channel
import kotlinx.android.synthetic.main.remove_feed.view.*

class RemoveFeedDialogFragment : DialogFragment() {
    interface RemoveFeedDialogListener {
        fun removeChosenFeed(feed: Channel)
    }

    private var listener: RemoveFeedDialogListener? = null
    override fun onAttach(activity: Context) {
        super.onAttach(activity)
        listener = try {
            activity as RemoveFeedDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                activity.toString()
                        + " must implement RemoveFeedDialogListener"
            )
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder =
            AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.remove_feed, null)
        builder.setTitle(R.string.remove_feed)
            .setView(dialogView)
        val feeds = arguments?.getParcelableArrayList<Channel>(FEEDS) ?: arrayListOf()
        val adapter = ArrayAdapter(
            requireActivity(),
            R.layout.news_list_item, feeds
        )
        dialogView.feedsListView.adapter = adapter
        dialogView.feedsListView.emptyView = dialogView.noFeedsView
        builder.setPositiveButton(R.string.remove) { _, _ ->
            val position = dialogView.feedsListView.checkedItemPosition
            if (position != AbsListView.INVALID_POSITION) {
                val feed = adapter.getItem(position)
                listener?.removeChosenFeed(feed!!)
            }
        }
        builder.setNegativeButton(R.string.cancel) { _, _ ->
            // nothing to do
        }
        return builder.create()
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    companion object {
        const val FEEDS = "feeds"
    }
}