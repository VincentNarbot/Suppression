package io.takeflight.suppression;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class SignUpActivity extends Activity {

    protected TextView mLogin_Button;
    protected EditText mUsernameField;
    protected EditText mPasswordField;
    protected EditText mEmailField;
    protected Button mSignUp_Button;
    protected LinearLayout mLoading_ProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mLoading_ProgressBar = (LinearLayout)findViewById(R.id.loadingProgressBar);

        mLogin_Button = (TextView)findViewById(R.id.loginLabel);
        mLogin_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Killing activity
                finish();
            }
        });

        mSignUp_Button = (Button)findViewById(R.id.signupButton);
        mSignUp_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUsernameField = (EditText)findViewById(R.id.usernameField);
                mPasswordField = (EditText)findViewById(R.id.passwordField);
                mEmailField = (EditText)findViewById(R.id.emailField);

                String username = mUsernameField.getText().toString().trim();
                String password = mPasswordField.getText().toString().trim();
                String email = mEmailField.getText().toString().trim();

                //Check if fields are empty
                if(username.isEmpty() || password.isEmpty() || email.isEmpty()){
                    //Failed - Create an Error message
                    AlertDialog.Builder error = new AlertDialog.Builder(SignUpActivity.this);
                    error.setMessage(R.string.error_signup_message)
                            .setTitle(R.string.error_label)
                            .setNeutralButton(android.R.string.ok, null);

                    AlertDialog dialog = error.create();

                    dialog.show();

                }else{
                    //Success - Create an Account on Parse
                    mLoading_ProgressBar.setVisibility(View.VISIBLE);

                    ParseUser user = new ParseUser();
                    user.setUsername(username);
                    user.setPassword(password);
                    user.setEmail(email);

                    user.signUpInBackground(new SignUpCallback() {
                        @Override
                        public void done(com.parse.ParseException e) {
                            mLoading_ProgressBar.setVisibility(View.INVISIBLE);
                            if (e == null) {
                                //Success - Go on MainActivity
                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                intent.addFlags(intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                //Failed - Create an Error using Parse message
                                AlertDialog.Builder error = new AlertDialog.Builder(SignUpActivity.this);
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
