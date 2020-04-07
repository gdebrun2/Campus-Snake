package edu.illinois.cs.cs125.spring2020.mp.logic;

import android.content.Context;
import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.neovisionaries.ws.client.WebSocket;

/**
 * Represents an area mode game. Keeps track of cells and the player's most recent capture.
 * <p>
 * All these functions are stubs that you need to implement.
 * Feel free to add any private helper functions that would be useful.
 * See {@link TargetGame} for an example of how multiplayer games are handled.
 */
public final class AreaGame extends Game {

    // You will probably want some instance variables to keep track of the game state
    // (similar to the area mode gameplay logic you previously wrote in GameActivity)
    /**
     * fdasf.
     */
    private GoogleMap maps;
    /**
     * fdas.
     */
    private double areaNorth;
    /**
     * fds.
     */
    private double areaSouth;
    /**
     * fdsa.
     */
    private double areaEast;
    /**
     * fdas.
     */
    private double areaWest;
    /**
     * fdas.
     */
    private int lastCapturedX;
    /**
     * fads.
     */
    private int lastCapturedY;
    /**
     * .
     */
    private int cellSize;
    /**
     * fda.
     */
    private AreaDivider ad;


    /**
     * Creates a game in area mode.
     * <p>
     * Loads the current game state from JSON into instance variables and populates the map
     * to show existing cell captures.
     * @param email the user's email
     * @param map the Google Maps control to render to
     * @param webSocket the websocket to send updates to
     * @param fullState the "full" update from the server
     * @param context the Android UI context
     */
    public AreaGame(final String email, final GoogleMap map, final WebSocket webSocket,
                    final JsonObject fullState, final Context context) {
        super(email, map, webSocket, fullState, context);
        maps = map;
        lastCapturedX = -1;
        lastCapturedY = -1;
        areaNorth = fullState.get("areaNorth").getAsDouble();
        areaSouth = fullState.get("areaSouth").getAsDouble();
        areaEast = fullState.get("areaEast").getAsDouble();
        areaWest = fullState.get("areaWest").getAsDouble();
        cellSize = fullState.get("cellSize").getAsInt();
        ad = new AreaDivider(areaNorth, areaEast, areaSouth, areaWest, cellSize);
        ad.renderGrid(map);
        JsonArray arr = fullState.get("players").getAsJsonArray();
        for (JsonElement element : arr) {
            JsonObject obj = element.getAsJsonObject();
            JsonArray jsonArray = obj.get("path").getAsJsonArray();
            int team = obj.get("team").getAsInt();
            for (JsonElement jsonElement : jsonArray) {
                JsonObject temp = jsonElement.getAsJsonObject();
                int x = temp.get("x").getAsInt();
                int y = temp.get("y").getAsInt();
                helpFunction(x, y, team);
            }
        }
    }
    /**
     * fdafd.
     * @param x fdfd
     * @param y fdsafa
     * @param team fdasf
     */
    public void helpFunction(final int x, final int y, final int team) {
        LatLngBounds llb = ad.getCellBounds(x, y);
        double n = llb.northeast.latitude;
        double e = llb.northeast.longitude;
        double s = llb.southwest.latitude;
        double w = llb.southwest.longitude;
        LatLng nw = new LatLng(n, w);
        LatLng se = new LatLng(s, e);
        PolygonOptions po = new PolygonOptions();
        po.add(llb.northeast, nw, llb.southwest, se);
        if (team == TeamID.TEAM_RED) {
            po.fillColor(Color.RED);
        } else if (team == TeamID.TEAM_YELLOW) {
            po.fillColor(Color.YELLOW);
        } else if (team == TeamID.TEAM_GREEN) {
            po.fillColor(Color.GREEN);
        } else if (team == TeamID.TEAM_BLUE) {
            po.fillColor(Color.BLUE);
        }
        maps.addPolygon(po);
        JsonObject newObj = new JsonObject();
        newObj.addProperty("type", "cellCapture");
        newObj.addProperty("x", x);
        newObj.addProperty("y", y);
        sendMessage(newObj);
    }

    /**
     * Called when the user's location changes.
     * <p>
     * Area mode games detect whether the player is in an uncaptured cell. Capture is possible if
     * the player has no captures yet or if the cell shares a side with the previous cell captured by
     * the player. If capture occurs, a polygon with the team color is added to the cell on the map
     * and a cellCapture update is sent to the server.
     * @param location the player's most recently known location
     */
    @Override
    public void locationUpdated(final LatLng location) {
        super.locationUpdated(location);
    }

    /**
     * Processes an update from the server.
     * <p>
     * Since playerCellCapture events are specific to area mode games, this function handles those
     * by placing a polygon of the capturing player's team color on the newly captured cell and
     * recording the cell's new owning team.
     * All other message types are delegated to the superclass.
     * @param message JSON from the server (the "type" property indicates the update type)
     * @return whether the message type was recognized
     */
    @Override
    public boolean handleMessage(final JsonObject message) {
        super.handleMessage(message);
        return false;
    }

    /**
     * Gets a team's score in this area mode game.
     * @param teamId the team ID
     * @return the number of cells owned by the team
     */
    @Override
    public int getTeamScore(final int teamId) {
        return 0;
    }

}
