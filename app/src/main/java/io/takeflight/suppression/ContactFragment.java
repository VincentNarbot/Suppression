package io.takeflight.suppression;

import android.app.AlertDialog;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;

import java.util.List;

/**
 * Created by vincentnarbot on 4/5/15.
 */
public class ContactFragment extends ListFragment {

    public static final String TAG = "ContactFragment";

    protected List<ParseUser> mFriendsList;
    protected TextView mContactEmpty;
    protected LinearLayout mLoading_ProgressBar;
    protected ParseRelation<ParseUser> mContactRelation;
    protected ParseUser mCurrentUser;

    public ContactFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_contact, container, false);

        mContactEmpty = (TextView) rootView.findViewById(R.id.contactEmpty);
        mLoading_ProgressBar = (LinearLayout) rootView.findViewById(R.id.loadingProgressBar);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

        mLoading_ProgressBar.setVisibility(View.VISIBLE);

        mCurrentUser = ParseUser.getCurrentUser();
        mContactRelation = mCurrentUser.getRelation(AppConstants.KEY_CONTACT_RELATION);

        ParseQuery<ParseUser> query = mContactRelation.getQuery();
        query.orderByAscending(AppConstants.KEY_ORDER_CONTACT);
        query.setLimit(AppConstants.KEY_LIMIT_CONTACT);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> friends, ParseException e) {
                mLoading_ProgressBar.setVisibility(View.INVISIBLE);
                if(e == null){
                    //Success
                    mFriendsList = friends;

                    //Loading only Username
                    String[] usernames = new String[mFriendsList.size()]; //Set size as same as mUserList from Parse
                    int i = 0;
                    for(ParseUser user: mFriendsList)
                    {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    //Remove No Contact label
                    if(i > 0){
                        mContactEmpty.setVisibility(View.INVISIBLE);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            //Set Context using ListView
                            getListView().getContext(),
                            android.R.layout.simple_expandable_list_item_1,
                            usernames);

                    setListAdapter(adapter);
                }else{
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

}
