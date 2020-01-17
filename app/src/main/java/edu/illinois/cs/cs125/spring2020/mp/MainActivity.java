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
        // This "super" call is required for all activities
        super.onCreate(savedInstanceState);
        // Create the UI from a layout resource
        setContentView(R.layout.activity_main);
        // Now that setContentView has been called, findViewById can find views

        // This activity doesn't do anything yet - it immediately launches the game activity
        // It will be changed a little in Checkpoint 1 and filled out in Checkpoint 2

        // Intents are Android's way of specifying what to do/launch
        // Here we create an Intent for launching GameActivity and act on it with startActivity
        startActivity(new Intent(this, GameActivity.class));
        // End this activity so that it's removed from the history
        // Otherwise pressing the back button in the game would come back to a blank screen here
        finish();
    }

}
