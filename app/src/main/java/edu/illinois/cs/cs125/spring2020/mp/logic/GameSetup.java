package edu.illinois.cs.cs125.spring2020.mp.logic;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * Provides static methods to convert game information to JSON payloads that can be POSTed
 * to the server's /games/create endpoint to create a multiplayer game.
 */
public class GameSetup {
    /**
     * constructor/caller.
     */
    public GameSetup() {
    }
    /**
     * Creates a JSON object representing the configuration of a multiplayer area mode game.
     * Refer to our API documentation for the structure of the output JSON.
     * The configuration is valid if there is at least one invitee and a positive (larger than zero) cell size.
     * @param invitees all players involved in the game (never null)
     * @param area the area boundaries
     * @param cellSize the desired cell size in meters
     * @return a JSON object usable by the /games/create endpoint or null if the configuration is invalid
     */
    public static com.google.gson.JsonObject areaMode(final java.util.List<Invitee> invitees,
                                                      final com.google.android.gms.maps.model.LatLngBounds area,
                                                      final int cellSize) {
        if (invitees.size() >= 1 && cellSize > 0) {
            JsonObject gameInformation = new JsonObject();
            gameInformation.addProperty("mode", "area");
            gameInformation.addProperty("cellSize", cellSize);
            gameInformation.addProperty("areaNorth", area.northeast.latitude);
            gameInformation.addProperty("areaEast", area.northeast.longitude);
            gameInformation.addProperty("areaSouth", area.southwest.latitude);
            gameInformation.addProperty("areaWest", area.southwest.longitude);
            JsonArray inviteesArray = new JsonArray();
            for (Invitee invitee : invitees) {
                JsonObject inviteeInformation = new JsonObject();
                inviteeInformation.addProperty("email", invitee.getEmail());
                inviteeInformation.addProperty("team", invitee.getTeamId());
                inviteesArray.add(inviteeInformation);
            }
            gameInformation.add("invitees", inviteesArray);
            return gameInformation;
        }
        return null;
    }
    /**
     * Creates a JSON object representing the configuration of a multiplayer target mode game.
     * Refer to our API documentation for the structure of the output JSON.
     * The configuration is valid if there is at least one invitee, at least one target,
     * and a positive (larger than zero) proximity threshold.
     * If the configuration is invalid, this function returns null.
     * @param invitees all players involved in the game (never null)
     * @param targets the positions of all targets (never null)
     * @param proximityThreshold the proximity threshold in meters
     * @return a JSON object usable by the /games/create endpoint or null if the configuration is invalid
     */
    public static com.google.gson.JsonObject targetMode(final java.util.List<Invitee> invitees,
                                                        final java.util.List<com.google.android.gms.maps.model.LatLng>
                                                                targets,
                                                        final int proximityThreshold) {
        if (targets.size() != 0 && invitees.size() != 0) {
            JsonObject gameInformation = new JsonObject();
            gameInformation.addProperty("mode", "target");
            gameInformation.addProperty("proximityThreshold", proximityThreshold);
            JsonArray targetsArray = new JsonArray();
            for (LatLng target : targets) {
                JsonObject targetInformation = new JsonObject();
                targetInformation.addProperty("latitude", target.latitude);
                targetInformation.addProperty("longitude", target.longitude);
                targetsArray.add(targetInformation);
            }
            gameInformation.add("targets", targetsArray);
            JsonArray inviteesArray = new JsonArray();
            for (Invitee invitee : invitees) {
                JsonObject inviteeInformation = new JsonObject();
                inviteeInformation.addProperty("email", invitee.getEmail());
                inviteeInformation.addProperty("team", invitee.getTeamId());
                inviteesArray.add(inviteeInformation);
            }
            gameInformation.add("invitees", inviteesArray);
            return gameInformation;
        }
        return null;
    }
}
