package iuliiaponomareva.eventum.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.DialogFragment
import iuliiaponomareva.eventum.R
import kotlinx.android.synthetic.main.add_feed.view.*

class AddFeedDialogFragment : DialogFragment() {
    interface AddFeedDialogListener {
        fun addChosenFeed(feedUrl: String)
    }

    private var listener: AddFeedDialogListener? = null
    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = try {
            context as AddFeedDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException(
                context.toString()
                        + " must implement AddFeedDialogListener"
            )
        }
    }

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder =
            AlertDialog.Builder(activity)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.add_feed, null)
        builder.setTitle(R.string.add_feed).setMessage(R.string.enter_url).setView(view)
        builder.setPositiveButton(R.string.add_feed) { _, _ ->
            val feedUrl = view.newFeedEditText.text.toString()
            listener?.addChosenFeed(feedUrl)
        }
        builder.setNegativeButton(R.string.cancel) { _, _ ->
            // nothing to do
        }
        val dialog = builder.create()
        view.newFeedEditText.setOnEditorActionListener(OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                dialog.getButton(Dialog.BUTTON_POSITIVE).performClick()
                dialog.dismiss()
                return@OnEditorActionListener true
            }
            false
        })
        return dialog
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}