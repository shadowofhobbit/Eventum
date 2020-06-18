package iuliiaponomareva.eventum.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import iuliiaponomareva.eventum.R;

public class RemoveFeedDialogFragment extends DialogFragment {

    public static final String FEEDS = "feeds";

    public interface RemoveFeedDialogListener {
        void removeChosenFeed(String feed);
    }

    private RemoveFeedDialogListener listener;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            listener = (RemoveFeedDialogListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement RemoveFeedDialogListener");
        }
    }

    @NonNull
    @SuppressLint("InflateParams")
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.remove_feed, null);
        final ListView listView = dialogView.findViewById(R.id.feeds_to_remove);
        builder.setTitle(R.string.remove_feed)
                .setView(dialogView);
        String[] feeds = getArguments().getStringArray(FEEDS);
        if (feeds == null) {
            feeds = new String[0];
        }
        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.news_list_item, feeds);
        listView.setAdapter(adapter);
        listView.setEmptyView(dialogView.findViewById(R.id.no_feeds_view));
        builder.setPositiveButton(R.string.remove, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                int position = listView.getCheckedItemPosition();
                if (position != AbsListView.INVALID_POSITION) {
                    String feed = adapter.getItem(position);
                    listener.removeChosenFeed(feed);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // nothing to do
            }
        });
        return builder.create();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
