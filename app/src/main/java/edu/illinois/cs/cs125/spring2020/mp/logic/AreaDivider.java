package edu.illinois.cs.cs125.spring2020.mp.logic;

import com.google.android.gms.maps.model.LatLngBounds;

/**
 * Creates an AreaDivider for an area.
 */
public class AreaDivider {
    /**
     * latitude of the north boundary.
     */
    private double north;
    /**
     * longitude of the east boundary.
     */
    private double east;
    /**
     * latitude of the south boundary.
     */
    private double south;
    /**
     * longitude of the west bonudary.
     */
    private double west;
    /**
     * length of each cell.
     */
    private int cellSize;

    /**
     * Creates an AreaDivider for an area.
     * @param setNorth latitude of the north boundary.
     * @param setEast longitude of the east boundary.
     * @param setSouth latitude of the south boundary.
     * @param setWest longitude of the east boundary.
     * @param setCellSize the requested side length of each cell, in meters.
     */
    public AreaDivider(final double setNorth,
                       final double setEast,
                       final double setSouth,
                       final double setWest,
                       final int setCellSize) {
        north = setNorth;
        east = setEast;
        south = setSouth;
        west = setWest;
        cellSize = setCellSize;
    }

    /**
     * Gets the number of cells between the west and east boundaries.
     * @return the number of cells in the X direction.
     */
    public final int getXCells() {
        double xDistance = LatLngUtils.distance(north, east, north, west);
        double numberOfXCells = xDistance / cellSize;
        return (int) Math.ceil(numberOfXCells);
    }

    /**
     * Gets the number of cells between the south and north boundaries.
     * @return the number of cells in the Y direction.
     */
    public final int getYCells() {
        double yDistance = LatLngUtils.distance(north, east, south, east);
        double numberOfYCells = yDistance / cellSize;
        return (int) Math.ceil(numberOfYCells);
    }

    /**
     * Gets the X coordinate of the cell containing the specified location.
     * @param location the location.
     * @return the X coordinate of the cell containing the lat-long point.
     */
    public final int getXIndex(final com.google.android.gms.maps.model.LatLng location) {
        return -1;
    }

    /**
     * Gets the Y coordinate of the cell containing the specified location.
     * @param location the location.
     * @return the Y coordinate of the cell containing the lat-long point.
     */
    public final int getYIndex(final com.google.android.gms.maps.model.LatLng location) {
        return -1;
    }

    /**
     * Gets the boundaries of the specified cell as a Google Maps LatLngBounds object.
     * @param x the cell's X coordinate.
     * @param y the cell's Y coordinate
     * @return the boundaries of the cell.
     */
    public final LatLngBounds getCellBounds(final int x, final int y) {
        return null;
    }

    /**
     * Returns whether the configuration provided to the constructor is valid.
     * @return whether this AreaDivider can divide a valid area.
     */
    public final boolean isValid() {
        return true;
    }

    /**
     * Draws the grid to a map using solid black polylines.
     * @param map the Google map to draw on.
     */
    public final void renderGrid(final com.google.android.gms.maps.GoogleMap map) {
    }

}
