package edu.illinois.cs.cs125.spring2020.mp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import edu.illinois.cs.cs125.spring2020.mp.logic.AreaDivider;
import edu.illinois.cs.cs125.spring2020.mp.logic.DefaultTargets;
import edu.illinois.cs.cs125.spring2020.mp.logic.LatLngUtils;
import edu.illinois.cs.cs125.spring2020.mp.logic.TargetVisitChecker;

/*
 * Welcome to the Machine Project app!
 *
 * There's a lot going on here. Don't worry about understanding it fully yet, but feel free to look
 * around to get a broad overview. This is an activity, i.e. a screen of UI. This activity has a
 * map showing the game. Once you're finished, it will detect the user's progress in the game and
 * update according to the game rules.
 *
 * First, complete the functions in TargetVisitChecker. You will need those to make the game work.
 * You'll know they're working properly when all tests - except testTargetModeGameplay which you'll
 * do in this file - pass.
 *
 * A bit below this big comment you'll find a lot of variable declarations. All of these can be
 * used from any function in this file. Some are used only by our provided code; others will be
 * used to implement gameplay.
 *
 * There are two functions here you need to modify. setUpMap needs to prepare the map with markers
 * for the targets. onLocationUpdate will be called when the player moves; it's responsible for
 * gameplay. There are comments with more details in those two functions.
 */

/**
 * Represents the game activity, where the user plays the game and sees its state.
 */
public final class GameActivity extends AppCompatActivity {

    /** Tag for log entries. */
    private static final String TAG = "GameActivity";

    /** The radial location accuracy required to send a location update. */
    private static final float REQUIRED_LOCATION_ACCURACY = 28f;

    /** How close the user has to be (in meters) to a target to capture it. */
    private static final int PROXIMITY_THRESHOLD = 20;

    /** Hue of the markers showing captured target locations.
     * Note that this is ONLY the hue; markers don't allow specifying the RGB color like other map elements do. */
    private static final float CAPTURED_MARKER_HUE = BitmapDescriptorFactory.HUE_GREEN;

    /** Color of other map elements related to the player's progress (e.g. lines connecting captured targets). */
    private static final int PLAYER_COLOR = Color.GREEN;

    /** The handler for location updates sent by the location listener service. */
    private BroadcastReceiver locationUpdateReceiver;

    /** A reference to the map control. */
    private GoogleMap map;

    /** Whether the user's location has been found and used to center the map. */
    private boolean centeredMap;

    /** Whether permission has been granted to access the phone's exact location. */
    private boolean hasLocationPermission;

    /** List of the markers that have been added by the placeMarker function. */
    private List<Marker> markers = new ArrayList<>();

    /** The predefined targets' latitudes. */
    private double[] targetLats;

    /** The predefined targets' longitudes. */
    private double[] targetLngs;

    /** The sequence of target indexes captured by the player (-1 if none). */
    private int[] path;

    /** What the fuck does this do???
     * fdsfdsa.
     */
    private int proximity;

    /**
     * .
     */
    private String mode;

    /**
     *.
     */
    private int cellSize;

    /**
     * .
     */
    private double areaNorth;
    /**
     * .
     */
    private double areaSouth;
    /**
     * .
     */
    private double areaEast;
    /**
     * .
     */
    private double areaWest;
    /**
     * .
     */
    private boolean[][] checkCaptured;
    /**
     * .
     */
    private AreaDivider theAreaDivider;
    /**
     * .
     */
    private Intent intent1;
    /**
     * .
     */
    private boolean temp = false;
    /**
     * .
     */
    private int proximityThreshold;
    /**
     * .
     */
    private int lastCapturedX;
    /**
     * .
     */
    private int lastCapturedY;
    /**
     * Called by the Android system when the activity is to be set up.
     * <p>
     * Prepares the variables needed for gameplay. You do not need to modify this function
     * in Checkpoint 0.
     * @param savedInstanceState information from the previously terminated instance (unused)
     */
    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(final Bundle savedInstanceState) {
        intent1 = getIntent();
        mode = intent1.getStringExtra("mode");
        // The super.onCreate call is required for all activities
        super.onCreate(savedInstanceState);
        // Load the UI from a layout resource
        setContentView(R.layout.activity_game);
        Log.v(TAG, "Created");

        if (mode.equals("target")) {
            // Load the predefined targets
            targetLats = DefaultTargets.getLatitudes(this);
            targetLngs = DefaultTargets.getLongitudes(this);
            path = new int[targetLats.length];
            Arrays.fill(path, -1); // No targets visited initially
        } else if (mode.equals("area")) {
            areaEast = intent1.getDoubleExtra("areaEast", 0.0);
            areaWest = intent1.getDoubleExtra("areaWest", 0.0);
            areaNorth = intent1.getDoubleExtra("areaNorth", 0.0);
            areaSouth = intent1.getDoubleExtra("areaSouth", 0.0);
            cellSize = intent1.getIntExtra("cellSize", 0);
            theAreaDivider = new AreaDivider(areaNorth, areaEast, areaSouth, areaWest, cellSize);
            checkCaptured = new boolean[theAreaDivider.getXCells()][theAreaDivider.getYCells()];
        }

        // Prepare a handler that will be called when location updates are available
        locationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                Location location = intent.getParcelableExtra(LocationListenerService.UPDATE_DATA_ID);
                if (map != null && location != null && location.hasAccuracy()
                        && location.getAccuracy() < REQUIRED_LOCATION_ACCURACY) {
                    ensureMapCentered(location);
                    onLocationUpdate(location.getLatitude(), location.getLongitude());
                }
            }
        };


        // Register (activate) it
        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver,
                new IntentFilter(LocationListenerService.UPDATE_ACTION));

        // See if we still need the location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // We don't have it yet - request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            Log.v(TAG, "Requested location permission");
        } else {
            // We do have it - activate the features that require location
            Log.v(TAG, "Already had location permission");
            hasLocationPermission = true;
            startLocationWatching();
        }
        // Load the predefined targets
        targetLats = DefaultTargets.getLatitudes(this);
        targetLngs = DefaultTargets.getLongitudes(this);
        path = new int[targetLats.length];
        Arrays.fill(path, -1); // No targets visited initially

        // Start the process of getting a Google Maps object for the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.gameMap);
        mapFragment.getMapAsync(view -> {
            Log.v(TAG, "getMapAsync handler called");

            // Save the newly obtained map
            map = view;
            setUpMap();
        });
        // Prepare a handler that will be called when location updates are available
        locationUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(final Context context, final Intent intent) {
                Location location = intent.getParcelableExtra(LocationListenerService.UPDATE_DATA_ID);
                if (map != null && location != null && location.hasAccuracy()
                        && location.getAccuracy() < REQUIRED_LOCATION_ACCURACY) {
                    ensureMapCentered(location);
                    onLocationUpdate(location.getLatitude(), location.getLongitude());
                }
            }
        };

        // Register (activate) it
        LocalBroadcastManager.getInstance(this).registerReceiver(locationUpdateReceiver,
                new IntentFilter(LocationListenerService.UPDATE_ACTION));

        // See if we still need the location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // We don't have it yet - request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            Log.v(TAG, "Requested location permission");
        } else {
            // We do have it - activate the features that require location
            Log.v(TAG, "Already had location permission");
            hasLocationPermission = true;
            startLocationWatching();
        }
    }
    /**
     * Sets up the Google map.
     * <p>
     * You need to add some code to this function to add the objectives to the map.
     */
    @SuppressWarnings("MissingPermission")
    private void setUpMap() {
        // Enable the My Location blue dot if possible
        if (hasLocationPermission) {
            Log.v(TAG, "setUpMap enabled My Location");
            map.setMyLocationEnabled(true);
        }

        // Remove some UI that gets in the way
        map.getUiSettings().setIndoorLevelPickerEnabled(false);
        map.getUiSettings().setMapToolbarEnabled(false);

        // Use the provided placeMarker function to add a marker at every target's location
        // HINT: onCreate initializes the relevant arrays (targetLats, targetLngs) for you
        if (mode.equals("target")) {
            for (int i = 0; i < targetLats.length; i++) {
                placeMarker(targetLats[i], targetLngs[i]);
            }
        } else {
            theAreaDivider.renderGrid(map);
        }

    }

    /**
     * Called when a high-confidence location update is available.
     * <p>
     * You need to implement this function to make the game work.
     * @param latitude the phone's current latitude
     * @param longitude the phone's current longitude
     */
    @VisibleForTesting // Actually just visible for documentation - not called directly by test suites
    public void onLocationUpdate(final double latitude, final double longitude) {
        // This function is responsible for updating the game state and map according to the user's movements

        // HINT: To operate on the game state, use the three methods you implemented in TargetVisitChecker
        // You can call them by prefixing their names with "TargetVisitChecker." e.g. TargetVisitChecker.visitTarget
        // The arrays to operate on are targetLats, targetLngs, and path

        // When the player gets within the PROXIMITY_THRESHOLD of a target, it should be captured and turned green
        // Sequential captures should create green connecting lines on the map
        // HINT: Use the provided changeMarkerColor and addLine functions to manipulate the map
        // HINT: Use the provided color constants near the top of this file as arguments to those functions
        Intent intent = getIntent();
        proximity = intent.getIntExtra("proximityThreshold", 0);
        if (mode.equals("target")) {
            int targetCandidate = TargetVisitChecker.getVisitCandidate(targetLats, targetLngs, path,
                    latitude, longitude, proximity);
            if (targetCandidate != -1 && path[0] != -1) {
                int finalindex = 0;
                while (path[finalindex] != -1) {
                    finalindex += 1;
                }
                finalindex--;
                if (TargetVisitChecker.checkSnakeRule(targetLats, targetLngs, path, targetCandidate)) {
                    TargetVisitChecker.visitTarget(path, targetCandidate);
                    changeMarkerColor(targetLats[targetCandidate], targetLngs[targetCandidate], CAPTURED_MARKER_HUE);
                    addLine(targetLats[path[finalindex]], targetLngs[path[finalindex]], targetLats[targetCandidate],
                            targetLngs[targetCandidate], PLAYER_COLOR);
                }
            } else if (targetCandidate != -1) {
                TargetVisitChecker.visitTarget(path, targetCandidate);
                changeMarkerColor(targetLats[targetCandidate], targetLngs[targetCandidate], CAPTURED_MARKER_HUE);
            }
        } else if (mode.equals("area")) {
            LatLng position = new LatLng(latitude, longitude);
            int currentX = theAreaDivider.getXIndex(position);
            int currentY = theAreaDivider.getYIndex(position);
            LatLngBounds square = theAreaDivider.getCellBounds(currentX, currentY);
            LatLng sw1 = square.southwest;
            LatLng ne2 = square.northeast;
            LatLng nw3 = new LatLng(ne2.latitude, sw1.longitude);
            LatLng se4 = new LatLng(sw1.latitude, ne2.longitude);
            PolygonOptions newSquare = new PolygonOptions();
            newSquare.add(sw1, nw3, ne2, se4).fillColor(PLAYER_COLOR);
            for (int i = 0; i < checkCaptured.length; i++) {
                for (int j = 0; j < checkCaptured[0].length; j++) {
                    if (checkCaptured[i][j]) {
                        temp = true;
                    }
                }
            }
            if (!temp) {
                map.addPolygon(newSquare);
                checkCaptured[currentX][currentY] = true;
                lastCapturedX = currentX;
                lastCapturedY = currentY;
            } else {
                if ((lastCapturedX == currentX + 1 && lastCapturedY == currentY
                        || lastCapturedX == currentX - 1 && lastCapturedY == currentY
                        || lastCapturedX == currentX && lastCapturedY == currentY + 1
                        || lastCapturedX == currentX && lastCapturedY == currentY - 1)
                        && currentX >= 0 && currentX < checkCaptured.length && currentY >= 0
                        && currentY < checkCaptured[0].length && !checkCaptured[currentX][currentY]) {
                    map.addPolygon(newSquare);
                    checkCaptured[currentX][currentY] = true;
                    lastCapturedX = currentX;
                    lastCapturedY = currentY;
                }
            }
        }

    }
    /**
     * Places a marker on the map at the specified coordinates.
     * @param latitude the marker's latitude
     * @param longitude the marker's longitude
     */
    @VisibleForTesting // For documentation
    public void placeMarker(final double latitude, final double longitude) {
        // Convert the loose coordinates to a Google Maps LatLng object
        LatLng position = new LatLng(latitude, longitude);

        // Create a MarkerOptions object to specify where we want the marker
        MarkerOptions options = new MarkerOptions().position(position);

        // Add it to the map - Google Maps gives us the created Marker
        Marker marker = map.addMarker(options);

        // Keep track of the new marker so changeMarkerColor can adjust it later
        markers.add(marker);
    }

    /**
     * Adds a colored line to the Google map.
     * @param startLat the latitude of one endpoint of the line
     * @param startLng the longitude of that endpoint
     * @param endLat the latitude of the other endpoint of the line
     * @param endLng the longitude of that other endpoint
     * @param color the color to fill the line with
     */
    @VisibleForTesting
    public void addLine(final double startLat, final double startLng,
                        final double endLat, final double endLng, final int color) {
        // Package the loose coordinates into LatLng objects usable by Google Maps
        LatLng start = new LatLng(startLat, startLng);
        LatLng end = new LatLng(endLat, endLng);

        // Configure and add a colored line
        final int lineThickness = 12;
        PolylineOptions fill = new PolylineOptions().add(start, end).color(color).width(lineThickness).zIndex(1);
        map.addPolyline(fill);

        // Polylines don't have a way to set borders, so we create a wider black line under the colored one to fake it
        final int borderThickness = 3;
        PolylineOptions border = new PolylineOptions().add(start, end).width(lineThickness + borderThickness);
        map.addPolyline(border);
    }

    /**
     * Changes the hue of the marker at the specified position.
     * The marker should have been previously added by placeMarker.
     * @param latitude the marker's latitude
     * @param longitude the marker's longitude
     * @param hue the new hue, e.g. a constant from BitmapDescriptorFactory
     */
    @VisibleForTesting
    public void changeMarkerColor(final double latitude, final double longitude, final float hue) {
        // Convert the loose coordinates to a Google Maps LatLng object
        LatLng position = new LatLng(latitude, longitude);

        // Try to find the existing marker (one with the same coordinates)
        for (Marker marker : markers) {
            if (LatLngUtils.same(position, marker.getPosition())) {
                // Create a new icon with the desired hue
                BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(hue);

                // Change the marker's icon
                marker.setIcon(icon);
                return;
            }
        }

        // Didn't find the existing marker
        Log.w(TAG, "No existing marker near " + latitude + ", " + longitude);
    }

    /**
     * Called by the Android system when the activity is shut down and cannot be returned to.
     */
    @Override
    protected void onDestroy() {
        // The super call is required for all activities
        super.onDestroy();
        // Stop the location service
        stopLocationWatching();
        // Unregister this activity's location listener
        LocalBroadcastManager.getInstance(this).unregisterReceiver(locationUpdateReceiver);
        Log.v(TAG, "Destroyed");
    }

    /**
     * Called by the Android system when the user has responded to a permissions request.
     * @param requestCode the request code passed to requestPermissions
     * @param permissions which permission(s) this notification is about
     * @param grantResults whether the user granted the permission(s)
     */
    @Override
    @SuppressLint("MissingPermission")
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions,
                                           final @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Required by Android
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // We only ever request the location permission, so if we got here, the user granted that one
            Log.v(TAG, "User granted location permission");
            hasLocationPermission = true;
            if (map != null) {
                Log.v(TAG, "onRequestPermissionsResult enabled My Location");
                map.setMyLocationEnabled(true);
            }
            // Start the location listener service
            startLocationWatching();
        } else {
            Log.v(TAG, "Location permission was not granted");
        }
    }

    /**
     * Centers the map on the user's location if the map hasn't been centered yet.
     * @param location the current location
     */
    private void ensureMapCentered(final Location location) {
        if (location != null && !centeredMap) {
            final float defaultMapZoom = 18f;
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(location.getLatitude(), location.getLongitude()), defaultMapZoom));
            centeredMap = true;
        }
    }

    /**
     * Starts watching for location changes if possible under the current permissions.
     */
    @SuppressWarnings("MissingPermission")
    private void startLocationWatching() {
        if (!hasLocationPermission) {
            return;
        }
        if (map != null) {
            Log.v(TAG, "startLocationWatching enabled My Location");
            map.setMyLocationEnabled(true);
        }
        ContextCompat.startForegroundService(this, new Intent(this, LocationListenerService.class));
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * Unregisters the location listener.
     */
    private void stopLocationWatching() {
        stopService(new Intent(this, LocationListenerService.class));
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

}
