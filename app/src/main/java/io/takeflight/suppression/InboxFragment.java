package io.takeflight.suppression;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by vincentnarbot on 4/5/15.
 */
public class InboxFragment extends ListFragment {

    public static final String TAG = "InboxFragment";

    protected List<ParseObject> mMessages;
    protected TextView mInboxEmpty;
    protected LinearLayout mLoading_ProgressBar;

    public InboxFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_inbox, container, false);

        mInboxEmpty = (TextView) rootView.findViewById(R.id.inboxEmpty);
        mLoading_ProgressBar = (LinearLayout) rootView.findViewById(R.id.loadingProgressBar);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mLoading_ProgressBar.setVisibility(View.VISIBLE);
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(AppConstants.CLASS_MESSAGES);
        query.whereEqualTo(AppConstants.KEY_RECIPIENT_IDS, ParseUser.getCurrentUser().getObjectId());
        query.addDescendingOrder(AppConstants.KEY_CREATED_AT);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> messages, ParseException e) {
                mLoading_ProgressBar.setVisibility(View.INVISIBLE);

                if(e == null){
                    //Success getting message
                    mMessages = messages;

                    String[] messagesArray = new String[mMessages.size()]; //Set size as same as mUserList from Parse
                    int i = 0;
                    for(ParseObject message: mMessages)
                    {
                        messagesArray[i] = message.getString(AppConstants.KEY_SENDER_NAME);
                        i++;
                    }

                    if(i > 0){
                        mInboxEmpty.setVisibility(View.INVISIBLE);
                    }

                    if(getListView().getAdapter() == null){
                        //Set Adapter only if doesn't exist
                        MessageAdapter adapter = new MessageAdapter(
                                getListView().getContext(),
                                mMessages);
                        setListAdapter(adapter);
                    }
                    else{
                        //Refill Adapter
                        ((MessageAdapter)getListView().getAdapter()).refill(mMessages);
                    }

                }
                else{
                    //Failed - Error
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder error = new AlertDialog.Builder(getActivity());
                    error.setMessage(R.string.error_loading_backend)
                            .setTitle(R.string.error_label)
                            .setNeutralButton(android.R.string.ok, null);

                    AlertDialog dialog = error.create();

                    dialog.show();
                }
            }
        });
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ParseObject message = mMessages.get(position);
        String messageType = message.getString(AppConstants.KEY_FILE_TYPE);
        ParseFile file = message.getParseFile(AppConstants.KEY_FILE);
        Uri fileUri = Uri.parse(file.getUrl());

        if(messageType.equals(AppConstants.TYPE_IMAGE)){
            Intent imageViewIntent = new Intent(getActivity(), ViewImageActivity.class);
            imageViewIntent.setData(fileUri);
            startActivity(imageViewIntent);
        }else{
            Intent videoViewIntent = new Intent(Intent.ACTION_VIEW,fileUri);
            videoViewIntent.setDataAndType(fileUri, "video/*");
            startActivity(videoViewIntent);
        }

    }
}
