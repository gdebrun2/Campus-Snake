package edu.illinois.cs.cs125.spring2020.mp;

import android.content.Intent;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

/**
 * Represents the main screen of the app, where the user can view and enter games.
 */
public final class MainActivity extends AppCompatActivity {

    /**
     * Called by the Android system when the activity is to be set up.
     * @param savedInstanceState info from the previously terminated instance (unused)
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        // The super.onCreate call is required for all activities
        super.onCreate(savedInstanceState);
        // Set up the UI from the activity_main.xml layout resource
        setContentView(R.layout.activity_main);
        // Now that setContentView has been called, findViewById can find views

        findViewById(R.id.createGame).setOnClickListener(unused -> startActivity(
                new Intent(this, NewGameActivity.class)));
    }

    // This activity doesn't do much now - it'll be filled out in Checkpoint 2

}
