package edu.illinois.cs.cs125.spring2020.mp;

import android.content.Intent;
import android.graphics.Point;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import edu.illinois.cs.cs125.spring2020.mp.logic.GameSetup;
import edu.illinois.cs.cs125.spring2020.mp.logic.Invitee;
import edu.illinois.cs.cs125.spring2020.mp.logic.TeamID;
import edu.illinois.cs.cs125.spring2020.mp.logic.WebApi;

/**
 * Represents the game creation screen, where the user configures a new game.
 */
@SuppressWarnings("ConstantConditions")
public final class NewGameActivity extends AppCompatActivity {

    /** The list of invitees added so far. */
    private List<Invitee> invitees = new ArrayList<>();

    /** The Google Maps view used to set the area for area mode. */
    private GoogleMap areaMap;

    /** The Google Maps view used to manage targets for area mode. */
    private GoogleMap targetsMap;

    /** Markers on the target map representing targets. */
    private List<Marker> targets = new ArrayList<>();

    /** The group of radio buttons that allow setting the game mode. */
    private RadioGroup modeGroup;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Required by Android
        setContentView(R.layout.activity_new_game); // Loads the UI, now findViewById can work
        setTitle(R.string.create_game);

        modeGroup = findViewById(R.id.gameModeGroup);

        findViewById(R.id.addInvitee).setOnClickListener(unused -> addInvitee());
        findViewById(R.id.createGame).setOnClickListener(unused -> tryCreate());
        SupportMapFragment areaMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.areaSizeMap);
        areaMapFragment.getMapAsync(newMap -> {
            areaMap = newMap;
            centerMap(areaMap);
        });
        SupportMapFragment targetMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.targetsMap);
        targetMapFragment.getMapAsync(newMap -> {
            targetsMap = newMap;
            centerMap(targetsMap);
            targetsMap.setOnMapLongClickListener(point -> {
                Marker marker = targetsMap.addMarker(new MarkerOptions().position(point).draggable(true));
                targets.add(marker);
            });
            targetsMap.setOnMarkerClickListener(marker -> {
                marker.remove();
                targets.remove(marker);
                return true;
            });
        });

        invitees.add(new Invitee(FirebaseAuth.getInstance().getCurrentUser().getEmail(), TeamID.OBSERVER));
        updateInviteeUi();
    }

    /**
     * Adds the just-entered player to the invitee list.
     */
    private void addInvitee() {
        EditText emailText = findViewById(R.id.newInviteeEmail);
        String email = emailText.getText().toString();
        if (!email.trim().equals("")) {
            invitees.add(new Invitee(email.toLowerCase().trim(), TeamID.OBSERVER));
            updateInviteeUi();
            emailText.setText("");
        }
    }

    /**
     * Updates the Players list from the invitees list field.
     */
    private void updateInviteeUi() {
        LinearLayout inviteesList = findViewById(R.id.playersList);
        inviteesList.removeAllViews();
        for (int i = 0; i < invitees.size(); i++) {
            Invitee player = invitees.get(i);
            View chunk = getLayoutInflater().inflate(R.layout.chunk_invitee, inviteesList, false);
            TextView emailText = chunk.findViewById(R.id.inviteeEmail);
            emailText.setText(player.getEmail());
            Spinner teamSpinner = chunk.findViewById(R.id.inviteeTeam);
            teamSpinner.setSelection(player.getTeamId());
            teamSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(final AdapterView<?> parent, final View view,
                                           final int position, final long id) {
                    player.setTeamId(position);
                }
                @Override
                public void onNothingSelected(final AdapterView<?> parent) {
                    /* Do nothing */
                }
            });
            Button removeButton = chunk.findViewById(R.id.removeInvitee);
            if (i == 0) {
                removeButton.setVisibility(View.GONE);
            } else {
                removeButton.setOnClickListener(unused -> {
                    invitees.remove(player);
                    updateInviteeUi();
                });
            }
            inviteesList.addView(chunk);
        }
    }

    /**
     * Sets up the area sizing map with initial settings: centering on campustown.
     * @param map the map to center
     */
    private void centerMap(final GoogleMap map) {
        // Bounds of campustown and some surroundings
        final double swLatitude = 40.098331;
        final double swLongitude = -88.246065;
        final double neLatitude = 40.116601;
        final double neLongitude = -88.213077;

        // Get the window dimensions (for the width)
        Point windowSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(windowSize);

        // Convert 300dp (height of map control) to pixels
        final int mapHeightDp = 300;
        float heightPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mapHeightDp,
                getResources().getDisplayMetrics());

        // Schedule the camera update
        final int paddingPx = 10;
        map.moveCamera(CameraUpdateFactory.newLatLngBounds(new LatLngBounds(
                        new LatLng(swLatitude, swLongitude),
                        new LatLng(neLatitude, neLongitude)), windowSize.x, (int) heightPx, paddingPx));
    }

    /**
     * Attempts to create the game, displaying a toast if there is a problem.
     */
    private void tryCreate() {
        String result = create();
        if (result != null) {
            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Attempts to create the game, returning information on any error that occurred.
     * @return a human-readable error message if there was a problem, null if everything is OK
     */
    private String create() {
        JsonObject request;
        switch (modeGroup.getCheckedRadioButtonId()) {
            case R.id.areaModeOption:
                int cellSize;
                try {
                    EditText cellSizeBox = findViewById(R.id.cellSize);
                    cellSize = Integer.parseInt(cellSizeBox.getText().toString());
                } catch (NumberFormatException e) {
                    return "Cell size must be a valid number.";
                }
                request = GameSetup.areaMode(invitees,
                        areaMap.getProjection().getVisibleRegion().latLngBounds, cellSize);
                break;
            case R.id.targetModeOption:
                int proximityThreshold;
                try {
                    EditText proximityBox = findViewById(R.id.proximityThreshold);
                    proximityThreshold = Integer.parseInt(proximityBox.getText().toString());
                } catch (NumberFormatException e) {
                    return "Proximity threshold must be a valid number.";
                }
                List<LatLng> targetPositions = new ArrayList<>();
                for (Marker m : targets) {
                    targetPositions.add(m.getPosition());
                }
                request = GameSetup.targetMode(invitees, targetPositions, proximityThreshold);
                break;
            default:
                return "You must specify the game mode.";
        }
        if (request == null) {
            return "Game setup is invalid.";
        }
        Button createButton = findViewById(R.id.createGame);
        createButton.setEnabled(false);
        WebApi.startRequest(this, WebApi.API_BASE + "/games/create", Request.Method.POST, request,
            response -> {
                Intent launchIntent = new Intent(this, GameActivity.class);
                launchIntent.putExtra("game", response.get("game").getAsString());
                startActivity(launchIntent);
                finish();
            },
            error -> {
                createButton.setEnabled(true);
                Toast.makeText(this, error.getMessage(), Toast.LENGTH_LONG).show();
            });
        return null;
    }

}
