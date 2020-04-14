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
     * the current google map to render to.
     */
    private GoogleMap currentMap;
    /**
     * Northern Latitude.
     */
    private double areaNorth;
    /**
     * Southern Latitude.
     */
    private double areaSouth;
    /**
     * eastern longitude.
     */
    private double areaEast;
    /**
     * western longitude.
     */
    private double areaWest;
    /**
     * the size of the cell in meters.
     */
    private int cellSize;
    /**
     * the game play area.
     */
    private AreaDivider area;
    /**
     * game state.
     */
    private JsonObject gameState;
    /**
     * fdasf.
     */
    private int lastXIndex;
    /**
     * fdasfdsaf.
     */
    private int lastYIndex;
    /**
     * fdasfdsa.
     */
    private String playerEmail;
    /**
     * fdasfds.
     */
    private int playerPathSize;
    /**
     * fdasf.
     */
    private JsonObject thisPlayer;
    /**
     * fdasf.
     */
    private JsonArray thisPlayerPath;
    /**
     * fdasf.
     */
    private JsonArray cells;
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
        playerEmail = email;
        gameState = fullState;
        currentMap = map;
        cells = fullState.get("cells").getAsJsonArray();
        areaNorth = fullState.get("areaNorth").getAsDouble();
        areaSouth = fullState.get("areaSouth").getAsDouble();
        areaEast = fullState.get("areaEast").getAsDouble();
        areaWest = fullState.get("areaWest").getAsDouble();
        cellSize = fullState.get("cellSize").getAsInt();
        area = new AreaDivider(areaNorth, areaEast, areaSouth, areaWest, cellSize);
        area.renderGrid(map);
        JsonArray playerArray = fullState.get("players").getAsJsonArray();
        for (JsonElement player : playerArray) {
            if (player.getAsJsonObject().get("email").getAsString().equals(playerEmail)) {
                thisPlayerPath = player.getAsJsonObject().get("path").getAsJsonArray();
                playerPathSize = thisPlayerPath.size();
                player.getAsJsonObject().get("path").getAsJsonArray();
                thisPlayer = player.getAsJsonObject();
                if (playerPathSize > 1) {
                    JsonElement lastCellCapture = thisPlayer.get("path").getAsJsonArray().get(playerPathSize - 1);
                    lastXIndex = lastCellCapture.getAsJsonObject().get("x").getAsInt();
                    lastYIndex = lastCellCapture.getAsJsonObject().get("y").getAsInt();
                } else if (playerPathSize == 1) {
                    lastXIndex = thisPlayerPath.get(0).getAsJsonObject().get("x").getAsInt();
                    lastYIndex = thisPlayerPath.get(0).getAsJsonObject().get("y").getAsInt();
                }
            }
            JsonArray playerPath = player.getAsJsonObject().get("path").getAsJsonArray();
            int team = player.getAsJsonObject().get("team").getAsInt();
            for (JsonElement capturedCell : playerPath) {
                int x = capturedCell.getAsJsonObject().get("x").getAsInt();
                int y = capturedCell.getAsJsonObject().get("y").getAsInt();
                populateMap(x, y, team);
            }
        }
    }
    /**
     * Renders the map with captured cells.
     * @param x The x coordinate of the cell to capture.
     * @param y The y coordinate of the cell to capture.
     * @param team The Capturing team.
     */
    public void populateMap(final int x, final int y, final int team) {
        LatLngBounds cellBounds = area.getCellBounds(x, y);
        double north = cellBounds.northeast.latitude;
        double east = cellBounds.northeast.longitude;
        double south = cellBounds.southwest.latitude;
        double west = cellBounds.southwest.longitude;
        LatLng northWest = new LatLng(north, west);
        LatLng southEast = new LatLng(south, east);
        PolygonOptions cellCapture = new PolygonOptions();
        cellCapture.add(cellBounds.northeast, northWest, cellBounds.southwest, southEast);
        if (team == TeamID.TEAM_RED) {
            cellCapture.fillColor(Color.RED);
        } else if (team == TeamID.TEAM_YELLOW) {
            cellCapture.fillColor(Color.YELLOW);
        } else if (team == TeamID.TEAM_GREEN) {
            cellCapture.fillColor(Color.GREEN);
        } else if (team == TeamID.TEAM_BLUE) {
            cellCapture.fillColor(Color.BLUE);
        }
        currentMap.addPolygon(cellCapture);
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
        int thisXIndex = area.getXIndex(location);
        int thisYIndex = area.getYIndex(location);
        for (JsonElement cell : cells) {
            if (cell.getAsJsonObject().get("x").getAsInt() == thisXIndex
                    && cell.getAsJsonObject().get("y").getAsInt() == thisYIndex) {
                return;
            }
        }
        if (location.longitude > areaEast || location.longitude < areaWest) {
            return;
        }
        if (location.latitude > areaNorth || location.latitude < areaSouth) {
            return;
        }
        if (playerPathSize == 0
                || Math.abs(lastXIndex - thisXIndex) == 1 && Math.abs(lastYIndex - thisYIndex) == 0
                || Math.abs(lastYIndex - thisYIndex) == 1 && Math.abs(lastXIndex - thisXIndex) == 0) {
            lastXIndex = thisXIndex;
            lastYIndex = thisYIndex;
            playerPathSize++;
            JsonObject addToPath = new JsonObject();
            addToPath.addProperty("x", thisXIndex);
            addToPath.addProperty("y", thisYIndex);
            thisPlayerPath.add(addToPath);
            JsonObject addToCells = new JsonObject();
            addToCells.addProperty("x", thisXIndex);
            addToCells.addProperty("y", thisYIndex);
            addToCells.addProperty("email", playerEmail);
            addToCells.addProperty("team", getMyTeam());
            cells.add(addToCells);
            JsonObject areaUpdate = new JsonObject();
            areaUpdate.addProperty("type", "cellCapture");
            areaUpdate.addProperty("x", thisXIndex);
            areaUpdate.addProperty("y", thisYIndex);
            sendMessage(areaUpdate);
            populateMap(thisXIndex, thisYIndex, getMyTeam());
        }
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
        if (super.handleMessage(message)) {
            return true;
        }
        if (message.get("type").getAsString().equals("playerCellCapture")) {
            populateMap(message.get("x").getAsInt(),
                    message.get("y").getAsInt(), message.get("team").getAsInt());
            JsonObject playerCapture = new JsonObject();
            playerCapture.addProperty("x", message.get("x").getAsInt());
            playerCapture.addProperty("y", message.get("y").getAsInt());
            playerCapture.addProperty("email", message.get("email").getAsString());
            playerCapture.addProperty("team", message.get("team").getAsInt());
            cells.add(playerCapture);
            return true;
        }
        return false;
    }

    /**
     * Gets a team's score in this area mode game.
     * @param teamId the team ID
     * @return the number of cells owned by the team
     */
    @Override
    public int getTeamScore(final int teamId) {
        int count = 0;
        for (JsonElement cell : cells) {
            if (cell.getAsJsonObject().get("team").getAsInt() == teamId) {
                count += 1;
            }
        }
        return count;
    }

}
