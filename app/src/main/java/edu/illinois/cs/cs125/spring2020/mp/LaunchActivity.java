package edu.illinois.cs.cs125.spring2020.mp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.List;
/**
 * RC_SIGN_IN.
 */
public class LaunchActivity extends AppCompatActivity {
    /**
     * RC_SIGN_IN.
     */
    private static final int RC_SIGN_IN = 0;

    /**
     * .
     * @param savedInstanceState fsdfsd.
     */
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);
        if (FirebaseAuth.getInstance().getCurrentUser() != null /* the user is logged in */) { // see below discussion
            // launch MainActivity
            Intent x = new Intent(this, MainActivity.class);
            startActivity(x);
            finish();
        } else {
            createSignInIntent();

            // start login activity for result - see below discussion
        }
    }

    /**
     * .
     */

    public void createSignInIntent() {
        // [START auth_fui_create_intent]
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.EmailBuilder().build());

        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .build(),
                RC_SIGN_IN);
        // [END auth_fui_create_intent]
    }

    /**
     * .
     * @param requestCode .
     * @param resultCode .
     * @param data .
     */
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                Intent x = new Intent(this, MainActivity.class);
                startActivity(x);
                finish();
                // ...
            } else {
                Button goLogin = findViewById(R.id.goLogin);
                goLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        // Change the label's text
                        createSignInIntent();
                    }
                });
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                // ...
            }
        }
    }

}
