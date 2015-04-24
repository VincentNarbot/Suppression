package io.takeflight.suppression;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by vincentnarbot on 4/4/15.
 */
public class SuppressionApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "lO8ZjYUQ4ePQo4CfxwFxUw9dOAlfB9gUbXp2rlo0", "eO0nE6nm1utoVOyLdC79X9EGyEI4WpJeMLBX8yJE");
    }
}
