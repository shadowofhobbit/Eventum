package iuliiaponomareva.eventum.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import iuliiaponomareva.eventum.R;

public class AddFeedDialogFragment extends DialogFragment {
    public interface AddFeedDialogListener {
        void addChosenFeed(String feedUrl);
    }

    private AddFeedDialogListener listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (AddFeedDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement AddFeedDialogListener");
        }
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.add_feed, null);
        builder.setTitle(R.string.add_feed).setMessage(R.string.enter_url).setView(view);
        final EditText editText = view.findViewById(R.id.new_feed_url);

        builder.setPositiveButton(R.string.add_feed, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                String feedUrl = editText.getText().toString();
                listener.addChosenFeed(feedUrl);
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // nothing to do
            }
        });

        final AlertDialog dialog = builder.create();
        editText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    dialog.getButton(Dialog.BUTTON_POSITIVE).performClick();
                    dialog.dismiss();
                    return true;
                }
                return false;
            }
        });
        return dialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
