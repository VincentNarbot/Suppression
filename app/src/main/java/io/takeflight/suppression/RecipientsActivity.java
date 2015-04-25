package io.takeflight.suppression;

import android.app.AlertDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class RecipientsActivity extends ActionBarActivity {

    public static final String TAG = "RecipientsActivity";

    protected List<ParseUser> mFriendsList;
    protected TextView mContactEmpty;
    protected ListView mListView;
    protected LinearLayout mLoading_ProgressBar;
    protected ParseRelation<ParseUser> mContactRelation;
    protected ParseUser mCurrentUser;
    protected MenuItem mSendButton;
    protected Uri mMediaURI;
    protected String mFileType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipients);

        mMediaURI = getIntent().getData();
        mFileType = getIntent().getExtras().getString(AppConstants.KEY_FILE_TYPE);
    }

    @Override
    public void onResume() {
        super.onResume();

        mLoading_ProgressBar = (LinearLayout)findViewById(R.id.loadingProgressBar);
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
                        mContactEmpty = (TextView)findViewById(R.id.contactEmpty);
                        mContactEmpty.setVisibility(View.INVISIBLE);
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(
                            RecipientsActivity.this,
                            android.R.layout.simple_list_item_checked,
                            usernames);

                    mListView = (ListView)findViewById(android.R.id.list);
                    mListView.setAdapter(adapter);
                    mListView.setChoiceMode(mListView.CHOICE_MODE_MULTIPLE);

                    mListView.setOnItemClickListener(new ListView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                            if (mListView.isItemChecked(position)) {

                            }
                            if(mListView.getCheckedItemCount() > 0){
                                mSendButton.setVisible(true);
                            }else{
                                mSendButton.setVisible(false);
                            }
                        }
                    });




                }else{
                    //Failed - Error
                    Log.e(TAG, e.getMessage());
                    AlertDialog.Builder error = new AlertDialog.Builder(RecipientsActivity.this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recipients, menu);
        mSendButton = menu.getItem(0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_send) {
            ParseObject message = createMessage();
            if(message == null){
               AlertDialog.Builder builder = new AlertDialog.Builder(this);
               builder.setMessage(R.string.error_file_selected);
               builder.setTitle(R.string.error_label);
               builder.setNeutralButton(android.R.string.ok, null);
               AlertDialog dialog = builder.create();
               dialog.show();
            }
            else{
                send(message);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private ParseObject createMessage() {
        ParseObject message = new ParseObject(AppConstants.CLASS_MESSAGES);

        message.put(AppConstants.KEY_SENDER_ID, ParseUser.getCurrentUser().getObjectId());
        message.put(AppConstants.KEY_SENDER_NAME, ParseUser.getCurrentUser().getUsername());
        message.put(AppConstants.KEY_RECIPIENT_IDS, getRecipientIds());
        message.put(AppConstants.KEY_FILE_TYPE, mFileType);

        //mMediaURI resizing
        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mMediaURI);

        if(fileBytes == null){
            return null;
        }
        else{
            if(mFileType.equals(AppConstants.TYPE_IMAGE)){
                fileBytes = FileHelper.reduceImageForUpload(fileBytes);
            }

            String fileName = FileHelper.getFileName(this, mMediaURI, mFileType);

            ParseFile file = new ParseFile(fileName, fileBytes);
            message.put(AppConstants.KEY_FILE, file);
            return message;
        }
    }

    protected ArrayList<String> getRecipientIds(){
        ArrayList<String> recipientIds = new ArrayList<String>();
        for(int i = 0; i < mListView.getCount(); i++){
            if (mListView.isItemChecked(i)) {
                recipientIds.add(mFriendsList.get(i).getObjectId());
            }
        }
        return recipientIds;
    }

    protected void send(ParseObject message){
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    Toast.makeText(RecipientsActivity.this, getString(R.string.message_sent), Toast.LENGTH_LONG).show();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(RecipientsActivity.this);
                    builder.setMessage(getString(R.string.error_sending_message));
                    builder.setTitle(R.string.error_label);
                    builder.setNeutralButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }
}
