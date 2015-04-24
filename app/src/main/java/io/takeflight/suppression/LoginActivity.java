package io.takeflight.suppression;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseUser;

public class LoginActivity extends Activity {

    protected TextView mSignUp_Button;
    protected EditText mUsernameField;
    protected EditText mPasswordField;
    protected Button mLogin_Button;
    protected LinearLayout mLoading_ProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoading_ProgressBar = (LinearLayout)findViewById(R.id.loadingProgressBar);

        mSignUp_Button = (TextView)findViewById(R.id.signupLabel);
        mSignUp_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        mLogin_Button = (Button)findViewById(R.id.loginButton);
        mLogin_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsernameField = (EditText)findViewById(R.id.usernameField);
                mPasswordField = (EditText)findViewById(R.id.passwordField);

                String username = mUsernameField.getText().toString().trim();
                String password = mPasswordField.getText().toString().trim();

                //Check if fields are empty
                if(username.isEmpty() || password.isEmpty()){
                    //Failed - Create an Error message
                    AlertDialog.Builder error = new AlertDialog.Builder(LoginActivity.this);
                    error.setMessage(R.string.error_login_message)
                            .setTitle(R.string.error_label)
                            .setNeutralButton(android.R.string.ok, null);

                    AlertDialog dialog = error.create();

                    dialog.show();

                }else{
                    //Success - Try to login using Parse
                    mLoading_ProgressBar.setVisibility(View.VISIBLE);
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, com.parse.ParseException e) {
                            mLoading_ProgressBar.setVisibility(View.INVISIBLE);
                            if (user != null) {
                                //Success - Let's go to the MainActivity
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                //Failed - Create an Error using Parse message
                                AlertDialog.Builder error = new AlertDialog.Builder(LoginActivity.this);
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

    }
}
