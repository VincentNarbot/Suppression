package io.takeflight.suppression;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.ParseUser;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class MainActivity extends ActionBarActivity implements ActionBar.TabListener {


    public static final String TAG = "MainActivity";
    String username;
    SectionsPagerAdapter mSectionsPagerAdapter;
    ViewPager mViewPager;

    public static final int TAKE_PHOTO_REQUEST = 0;
    public static final int TAKE_VIDEO_REQUEST = 1;
    public static final int CHOOSE_PHOTO_REQUEST = 2;
    public static final int CHOOSE_VIDEO_REQUEST = 3;

    public static final int MEDIA_TYPE_IMAGE = 4;
    public static final int MEDIA_TYPE_VIDEO = 5;

    public static final int FILE_SIZE_LIMIT = 1024*1024*10; //10 mb

    protected Uri mMediaUri;

    protected DialogInterface.OnClickListener mDialogListener = new DialogInterface.OnClickListener(){
        @Override
        public void onClick(DialogInterface dialog, int which) {
            switch(which){

                case 0: //Take Picture
                    Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
                    //Check if Error with External Storage
                    if(mMediaUri == null){
                        Toast.makeText(MainActivity.this, R.string.error_external_storage_message,Toast.LENGTH_LONG).show();
                    }else{
                        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT,mMediaUri);

                        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST);
                    }
                    break;

                case 1: //Take Video
                    Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    mMediaUri = getOutputMediaFileUri(MEDIA_TYPE_VIDEO);
                    //Check if Error with External Storage
                    if(mMediaUri == null){
                        Toast.makeText(MainActivity.this, R.string.error_external_storage_message,Toast.LENGTH_LONG).show();
                    }else{
                        videoIntent.putExtra(MediaStore.EXTRA_OUTPUT,mMediaUri);
                        videoIntent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10); //Limit to 10 seconds
                        videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0); // LowQuality

                        startActivityForResult(videoIntent, TAKE_VIDEO_REQUEST);
                    }
                    break;

                case 2: //Choose Picture
                    Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    choosePhotoIntent.setType("image/*"); //Limit to image only

                    startActivityForResult(choosePhotoIntent, CHOOSE_PHOTO_REQUEST);
                    break;

                case 3: //Choose Video
                    Intent chooseVideoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    chooseVideoIntent.setType("video/*"); //Limit to video only
                    Toast.makeText(MainActivity.this, R.string.video_max_size, Toast.LENGTH_LONG).show();
                    startActivityForResult(chooseVideoIntent, CHOOSE_VIDEO_REQUEST);
                    break;
            }

        }
    };

    private Uri getOutputMediaFileUri(int mediaType){
        //return Uri.fromFile(getOutputMediaFile(mediaType));
        if(isExternalStorageAvailable()){
            //Get External Storage Directory
            File mediaStorageDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),MainActivity.this.getString(R.string.app_name));
            //If External Storage Doesn't exist
            if(! mediaStorageDirectory.exists()) {
                //Create one or display error
                if (!mediaStorageDirectory.mkdir()) {
                    Log.e(TAG, "Failed to create new directory");
                    return null;
                }
            }
            //Generate File
            File mediaFile;
            Date now = new Date();
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss",Locale.US).format(now);

            String path = mediaStorageDirectory.getPath() + File.separator;
            if(mediaType == MEDIA_TYPE_IMAGE) {
                mediaFile = new File(path + "IMG_" + timestamp + ".jpg");
            }
            else if(mediaType == MEDIA_TYPE_VIDEO) {
                mediaFile = new File(path + "VID_" + timestamp + ".mp4");
            }
            else {
                return null;
            }

            Log.d(TAG, "File:" + Uri.fromFile(mediaFile));

            return Uri.fromFile(mediaFile);
        }else{
            return null;
        }
    }

    private static boolean isExternalStorageAvailable(){
        String state = Environment.getExternalStorageState();

        if(state.equals(Environment.MEDIA_MOUNTED)) {
            return true;
        }else{
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Set up the action bar.
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        // When swiping between different sections, select the corresponding
        // tab. We can also use ActionBar.Tab#select() to do this if we have
        // a reference to the Tab.
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
            // Create a tab with text corresponding to the page title defined by
            // the adapter. Also specify this Activity object, which implements
            // the TabListener interface, as the callback (listener) for when
            // this tab is selected.
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mSectionsPagerAdapter.getPageTitle(i))
                            .setTabListener(this));
        }
        //Check if we are login. If not login, call LoginActivity
        ParseUser currentUser = ParseUser.getCurrentUser();
        if (currentUser == null) {
            NavigationToLogin();

        }else{
            Log.i(TAG, currentUser.getUsername());
            this.username = currentUser.getUsername();
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            //Check if image is picked from Gallery / Else we took a picture
            if(requestCode == CHOOSE_PHOTO_REQUEST || requestCode == CHOOSE_VIDEO_REQUEST){
                if(data == null){
                    Toast.makeText(MainActivity.this, R.string.error_general,Toast.LENGTH_LONG).show();
                }else{
                    mMediaUri = data.getData();
                }

                if(requestCode == CHOOSE_VIDEO_REQUEST){
                    //Check Video size
                    int fileSize = 0;

                    InputStream inputStream = null;
                    try {
                        inputStream = getContentResolver().openInputStream(mMediaUri);
                        fileSize = inputStream.available();
                    } catch (FileNotFoundException e){
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, R.string.error_file_selected,Toast.LENGTH_LONG).show();
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, R.string.error_general,Toast.LENGTH_LONG).show();
                        return;
                    }
                    finally {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if(fileSize >= FILE_SIZE_LIMIT){
                        Toast.makeText(MainActivity.this, R.string.error_file_too_large,Toast.LENGTH_LONG).show();
                        return;
                    }
                }
            }
            else {
                //Add to Image or Video to Phone Gallery when we Capture it
                Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                mediaScanIntent.setData(mMediaUri);
                sendBroadcast(mediaScanIntent);
            }

            //Start Recipient Activity
            Intent recipientsIntent = new Intent(MainActivity.this, RecipientsActivity.class);
            recipientsIntent.setData(mMediaUri);

            String fileType = "";
            if(requestCode == CHOOSE_PHOTO_REQUEST || requestCode == TAKE_PHOTO_REQUEST){
                fileType = AppConstants.TYPE_IMAGE;
            }else if(requestCode == CHOOSE_VIDEO_REQUEST || requestCode == TAKE_VIDEO_REQUEST){
                fileType = AppConstants.TYPE_VIDEO;
            }
            recipientsIntent.putExtra(AppConstants.KEY_FILE_TYPE, fileType);

            startActivity(recipientsIntent);

        }
        else if(resultCode != RESULT_CANCELED){
            Toast.makeText(MainActivity.this, R.string.error_general,Toast.LENGTH_LONG).show();
        }
    }

    private void NavigationToLogin() {
        //Start from this activity a new Activity class
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        //Add this Activity as the last Activity
        intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
        //Clear button in case of back button
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.menu_logout) {
            ParseUser.logOut();
            NavigationToLogin();
        }

        if (id == R.id.menu_settings) {
            Intent intent = new Intent(MainActivity.this, SettingActivity.class);
            startActivity(intent);
        }

        if (id == R.id.menu_add_friend) {
            Intent intent = new Intent(MainActivity.this, AddContactActivity.class);
            startActivity(intent);
        }

        if (id == R.id.menu_take_picture) {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            //Array of Camera Choices, List of case of Dialog
            builder.setItems(R.array.camera_choices, mDialogListener);

            AlertDialog dialog = builder.create();
            dialog.show();
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        // When the given tab is selected, switch to the corresponding page in
        // the ViewPager.
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
    }

}
