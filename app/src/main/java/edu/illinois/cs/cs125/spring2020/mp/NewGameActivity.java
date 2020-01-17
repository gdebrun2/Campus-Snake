package edu.illinois.cs.cs125.spring2020.mp;

import android.content.Intent;
import android.graphics.Point;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Represents the game creation screen, where the user configures a new game.
 * <p>
 * This file will be modified in Checkpoint 3.
 */
@SuppressWarnings("ConstantConditions")
public final class NewGameActivity extends AppCompatActivity {

    /** The Google Maps view used to set the area for area mode. */
    private GoogleMap areaMap;

    /** The group of radio buttons that allow setting the game mode. */
    private RadioGroup modeGroup;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); // Required by Android
        setContentView(R.layout.activity_new_game); // Loads the UI, now findViewById can work
        setTitle(R.string.create_game);

        modeGroup = findViewById(R.id.gameModeGroup);

        findViewById(R.id.createGame).setOnClickListener(unused -> tryCreate());
        SupportMapFragment areaMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.areaSizeMap);
        areaMapFragment.getMapAsync(newMap -> {
            areaMap = newMap;
            centerMap(areaMap);
        });
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
        Intent intent = new Intent(this, GameActivity.class);
        // Determine which game mode, if any, is selected by looking at the ID of the chosen radio button
        switch (modeGroup.getCheckedRadioButtonId()) {
            case R.id.areaModeOption:
                int cellSize;
                try {
                    // A try block allows us to handle problems so they don't cause a crash
                    EditText cellSizeBox = findViewById(R.id.cellSize);
                    // Text boxes hold strings, so we need to parse the text as a number (which can fail)
                    cellSize = Integer.parseInt(cellSizeBox.getText().toString());
                } catch (NumberFormatException e) {
                    // If there is a problem (from parseInt in this case), a catch block can run
                    return "Cell size must be a valid number.";
                }
                // Specify the game mode
                intent.putExtra("mode", "area");
                // Specify the cell size (as an integer)
                intent.putExtra("cellSize", cellSize);
                // Get and add the bounds of the region the user selected
                LatLngBounds bounds = areaMap.getProjection().getVisibleRegion().latLngBounds;
                intent.putExtra("areaNorth", bounds.northeast.latitude);
                intent.putExtra("areaEast", bounds.northeast.longitude);
                intent.putExtra("areaSouth", bounds.southwest.latitude);
                intent.putExtra("areaWest", bounds.southwest.longitude);
                break;
            case R.id.targetModeOption:
                int proximityThreshold;
                try {
                    EditText proximityBox = findViewById(R.id.proximityThreshold);
                    proximityThreshold = Integer.parseInt(proximityBox.getText().toString());
                } catch (NumberFormatException e) {
                    return "Proximity threshold must be a valid number.";
                }
                intent.putExtra("mode", "target");
                intent.putExtra("proximityThreshold", proximityThreshold);
                break;
            default:
                return "You must specify the game mode.";
        }
        // Launch the game and end this game setup activity
        startActivity(intent);
        finish();
        return null;
    }

}
