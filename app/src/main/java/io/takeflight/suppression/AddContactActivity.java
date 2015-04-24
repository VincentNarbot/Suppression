package io.takeflight.suppression;

import android.app.AlertDialog;
import android.content.Context;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class AddContactActivity extends ActionBarActivity  {

    public static final String TAG = "AddContactActivity";

    protected List<ParseUser> mUsersList;
    protected LinearLayout mLoading_ProgressBar;
    protected ListView mListView;
    protected ParseRelation<ParseUser> mContactRelation;
    protected ParseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
    }

    //Always call backend onResume()
    @Override
    protected void onResume() {
        super.onResume();

        mListView = (ListView)findViewById(android.R.id.list);

        mLoading_ProgressBar = (LinearLayout)findViewById(R.id.loadingProgressBar);
        mLoading_ProgressBar.setVisibility(View.VISIBLE);

        this.mCurrentUser = ParseUser.getCurrentUser();
        mContactRelation = mCurrentUser.getRelation(AppConstants.KEY_CONTACT_RELATION);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.orderByAscending(AppConstants.KEY_ORDER_ADD_CONTACT);
        query.setLimit(AppConstants.KEY_LIMIT_ADD_CONTACT);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> usersList, ParseException e) {
                mLoading_ProgressBar.setVisibility(View.INVISIBLE);
                if(e==null){
                    //Success - Loaded user from Parse
                    mUsersList = usersList;
                    //Loading only Username
                    String[] usernames = new String[mUsersList.size()]; //Set size as same as mUserList from Parse
                    int i = 0;
                    for(ParseUser user: mUsersList)
                    {
                        usernames[i] = user.getUsername();
                        i++;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            AddContactActivity.this,
                            android.R.layout.simple_list_item_checked,
                            usernames);

                    mListView.setAdapter(adapter);

                    checkIfFriend();

                    //Select one or more Choice
                    mListView.setChoiceMode(mListView.CHOICE_MODE_MULTIPLE);
                    mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            if(mListView.isItemChecked(position)){
                                //Add a New Friend to Contact list
                                mContactRelation.add(mUsersList.get(position));
                                mCurrentUser.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e == null){
                                            Context context = getApplicationContext();
                                            CharSequence text = "Added " + mUsersList.get(position).getUsername();
                                            int duration = Toast.LENGTH_SHORT;
                                            Toast toast = Toast.makeText(context, text, duration);
                                            toast.show();
                                            //Success Saved
                                        }else{
                                            //Failed
                                            AlertDialog.Builder error = new AlertDialog.Builder(AddContactActivity.this);
                                            error.setMessage(e.getMessage())
                                                    .setTitle(R.string.error_label)
                                                    .setNeutralButton(android.R.string.ok, null);

                                            AlertDialog dialog = error.create();

                                            dialog.show();
                                        }
                                    }
                                });
                            }else{
                                //Remove from Contact list
                                mContactRelation.remove(mUsersList.get(position));
                                mCurrentUser.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e == null){
                                            Context context = getApplicationContext();
                                            CharSequence text = "Removed " + mUsersList.get(position).getUsername();
                                            int duration = Toast.LENGTH_SHORT;
                                            Toast toast = Toast.makeText(context, text, duration);
                                            toast.show();
                                            //Success Saved
                                        }else{
                                            //Failed
                                            AlertDialog.Builder error = new AlertDialog.Builder(AddContactActivity.this);
                                            error.setMessage(e.getMessage())
                                                    .setTitle(R.string.error_label)
                                                    .setNeutralButton(android.R.string.ok, null);

                                            AlertDialog dialog = error.create();

                                            dialog.show();
                                        }
                                    }
                                });
                            }
                        }
                    });

                }else{
                    //Failed - Error
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder error = new AlertDialog.Builder(AddContactActivity.this);
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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Check if Friend or User is mCurrentUSer
    private void checkIfFriend(){
        mContactRelation.getQuery().findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> contacts, ParseException e) {
                if(e == null){
                    for(int i = 0; i < mUsersList.size() ;i++ ){
                        //Find all the user from list
                        ParseUser user = mUsersList.get(i);


                        for(ParseUser contact: contacts){
                            //Check if user (from contactRelation) is equal to the one on my mUsersList
                            if(user.getObjectId().equals(contact.getObjectId())) {
                                //Turn check On
                                mListView.setItemChecked(i, true);
                            }
                        }
                    }

                }else{
                    //Failed - Error
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder error = new AlertDialog.Builder(AddContactActivity.this);
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
