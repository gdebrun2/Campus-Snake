package edu.illinois.cs.cs125.spring2020.mp.logic;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.TEAM_BLUE;
import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.TEAM_GREEN;
import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.TEAM_RED;
import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.TEAM_YELLOW;

/**
 * Represents a target in an ongoing target-mode game and manages the marker displaying it.
 * The marker's color (hue, technically) changes to indicate the team owning it.
 * The Google Maps marker's hue should be BitmapDescriptorFactory.HUE_RED for the red team,
 * BitmapDescriptorFactory.HUE_YELLOW for the yellow team,
 * BitmapDescriptorFactory.HUE_GREEN for the green team,
 * BitmapDescriptorFactory.HUE_BLUE for the blue team,
 * and BitmapDescriptorFactory.HUE_VIOLET if unclaimed.
 */

public class Target {
    /**
     * instance marker variable.
     */
    private Marker marker;
    /**
     * the position of the target.
     */
    private LatLng position;
    /**
     * Creates a target in a target-mode game by placing an appropriately colored marker on the map.
     * The marker's hue should reflect the team (if any) currently owning the target.
     * See the class description for the hue values to use.
     * @param setMap the map to render to
     * @param setPosition the position of the target
     * @param setTeam the TeamID code of the team currently owning the target
     */
    public Target(final com.google.android.gms.maps.GoogleMap setMap,
           final com.google.android.gms.maps.model.LatLng setPosition, final int setTeam) {
        marker = setMap.addMarker(new MarkerOptions().position(setPosition));
        if (setTeam == TEAM_RED) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            marker.setIcon(icon);
        } else if (setTeam == TEAM_YELLOW) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
            marker.setIcon(icon);
        } else if (setTeam == TEAM_GREEN) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            marker.setIcon(icon);
        } else if (setTeam == TEAM_BLUE) {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
            marker.setIcon(icon);
        } else {
            BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET);
            marker.setIcon(icon);
        }
        position = setPosition;
    }
    /**
     * Gets the position of the target.
     * @return the coordinates of the target
     */
    public com.google.android.gms.maps.model.LatLng getPosition() {
        return this.position;
    }
    /**
     * Gets the ID of the team currently owning this target.
     * @return the owning team ID or OBSERVER if unclaimed
     */
    public int getTeam() {
        final int bullshitReturn = 4;
        return bullshitReturn;
    }
    /**
     * Updates the owning team of this target and updates the hue of the marker to match.
     * @param newTeam the ID of the team that captured the target
     */
    public void setTeam(final int newTeam) {

    }

}
