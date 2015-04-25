package io.takeflight.suppression;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Timer;
import java.util.TimerTask;


public class ViewImageActivity extends ActionBarActivity {

    public static final String TAG = "ViewImageActivity";

    protected int mCountDown = 11;
    TextView countDown;
    final Handler mHandler = new Handler();
    Timer mTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_image);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        countDown = (TextView) findViewById(R.id.countDown);

        Uri imageUri = getIntent().getData();

        Picasso.with(this).load(imageUri.toString()).placeholder(R.drawable.ic_action_picture).error(R.drawable.ic_action_picture).into(imageView);

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                cancelTimer();
                finish();
            }
        }, 10*1000);

        //Update count down every second
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                UpdateGUI();
            }
        }, 0, 1000);
    }

    private void UpdateGUI() {
        mCountDown--;
        Log.d(TAG, String.valueOf(mCountDown));
        mHandler.post(myRunnable);
    }

    final Runnable myRunnable = new Runnable() {
        public void run() {
            countDown.setText(String.valueOf(mCountDown));
        }
    };

    public void cancelTimer(){
        mTimer.cancel();
    }
}
