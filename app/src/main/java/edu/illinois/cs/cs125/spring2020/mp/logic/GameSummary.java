package edu.illinois.cs.cs125.spring2020.mp.logic;


import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import static edu.illinois.cs.cs125.spring2020.mp.logic.GameStateID.ENDED;
import static edu.illinois.cs.cs125.spring2020.mp.logic.GameStateID.PAUSED;
import static edu.illinois.cs.cs125.spring2020.mp.logic.GameStateID.RUNNING;
import static edu.illinois.cs.cs125.spring2020.mp.logic.PlayerStateID.INVITED;
import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.TEAM_BLUE;
import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.TEAM_GREEN;
import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.TEAM_RED;
import static edu.illinois.cs.cs125.spring2020.mp.logic.TeamID.TEAM_YELLOW;

/**
 * Extracts summary information about a game from JSON provided by the server.
 * One GameSummary instance corresponds to one object from the games array in the response from the server's /games
 * endpoint.
**/
public class GameSummary {
    /**
     * .
     */
    private com.google.gson.JsonObject info;

    /**
     * Creates a game summary from JSON from the server.
     *
     * @param infoFromServer one object from the array in the /games response.
     */
    public GameSummary(final com.google.gson.JsonObject infoFromServer) {
        info = infoFromServer;

    }

    /**
     * Gets the unique, server-assigned ID of this game.
     *
     * @return the game ID.
     */

    public java.lang.String getId() {

        String gameId = info.get("id").getAsString();
        return gameId;

    }

    /**
     * Gets the mode of this game, either area or target.
     *
     * @return the game mode.
     */
    public java.lang.String getMode() {
        String mode = info.get("mode").getAsString();
        return mode;

    }

    /**
     * Gets the owner/creator of this game.
     *
     * @return the email of the game's owner.
     */
    public java.lang.String getOwner() {
        String owner = info.get("owner").getAsString();
        return owner;

    }

    /**
     * Gets the name of the user's team/role.
     *
     * @param userEmail the logged-in user's email.
     * @param context   an Android context (for access to resources).
     * @return the human-readable team/role name of the user in this game.
     */
    public java.lang.String getPlayerRole(final java.lang.String userEmail,
                                          final android.content.Context context) {
        int playerRole;
        JsonArray players = info.get("players").getAsJsonArray();
        for (JsonElement i : players) {
            if (userEmail.equals(i.getAsJsonObject().get("email").getAsString())) {
                playerRole = i.getAsJsonObject().get("team").getAsInt();
                if (playerRole == 0) {
                    return "Observer";
                } else if (playerRole == TEAM_RED) {
                    return "Red";
                } else if (playerRole == TEAM_YELLOW) {
                    return "Yellow";
                } else if (playerRole == TEAM_GREEN) {
                    return "Green";
                } else if (playerRole == TEAM_BLUE) {
                    return "Blue";
                }
            }
        }
        return "NO ROLE";
    }

    /**
     * Determines whether this game is an invitation to the user.
     *
     * @param userEmail the logged-in user's email.
     * @return whether the user is invited to this game.
     */
    public boolean isInvitation(final java.lang.String userEmail) {
        int isInvited;
        int gameStatus = info.get("state").getAsInt();
        if (gameStatus == ENDED) {
            return false;
        }
        if (gameStatus == PAUSED || gameStatus == RUNNING) {
            JsonArray players = info.get("players").getAsJsonArray();
            for (JsonElement i : players) {
                if (userEmail.equals(i.getAsJsonObject().get("email").getAsString())) {
                    isInvited = i.getAsJsonObject().get("state").getAsInt();
                    if (isInvited == INVITED) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines whether the user is currently involved in this game.
     * For a game to be ongoing, it must not be over and the user must have accepted their invitation to it.
     *
     * @param userEmail the logged-in user's email.
     * @return whether this game is ongoing for the user.
     */
    public boolean isOngoing(final java.lang.String userEmail) {
        String ongoing;
        JsonArray players = info.get("players").getAsJsonArray();
        for (JsonElement i : players) {
            if (userEmail.equals(i.getAsJsonObject().get("email").getAsString())) {
                ongoing = i.getAsJsonObject().get("state").getAsString();
                String gameState = info.get("state").getAsString();
                if (gameState.equals("0") || gameState.equals("1")) {
                    if (ongoing.equals("0")) {
                        return false;
                    } else if (ongoing.equals("1")) {
                        return false;
                    } else if (ongoing.equals("2")) {
                        return true;
                    } else if (ongoing.equals("3")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
