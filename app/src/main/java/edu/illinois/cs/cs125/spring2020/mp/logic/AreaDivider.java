package edu.illinois.cs.cs125.spring2020.mp.logic;

import android.graphics.Color;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PolylineOptions;

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
        double lng = location.longitude;
        int xindex;
        double cellWidth;
        double gridWidth = LatLngUtils.distance(north, east, north, west);
        if (lng <= east && lng >= west) {
            double xdistance = LatLngUtils.distance(north, lng, north, west);
            cellWidth = gridWidth / getXCells();
            xindex = (int) (xdistance / cellWidth);
            return xindex;
        } else {
            return -1;
        }

    }

    /**
     * Gets the Y coordinate of the cell containing the specified location.
     * @param location the location.
     * @return the Y coordinate of the cell containing the lat-long point.
     */
    public final int getYIndex(final com.google.android.gms.maps.model.LatLng location) {
        double lat = location.latitude;
        int yindex;
        double cellHeight;
        double gridHeight = LatLngUtils.distance(north, east, south, east);
        if (lat <= north && lat >= south) {
            double ydistance = LatLngUtils.distance(lat, east, south, east);
            cellHeight = gridHeight / getYCells();
            yindex = (int) (ydistance / cellHeight);
            return yindex;
        } else {
            return -1;
        }

    }

    /**
     * Gets the boundaries of the specified cell as a Google Maps LatLngBounds object.
     * @param x the cell's X coordinate.
     * @param y the cell's Y coordinate
     * @return the boundaries of the cell.
     */
    public final LatLngBounds getCellBounds(final int x, final int y) {
        double totalLng = east - west;
        double lngCell = totalLng / getXCells();
        double southwestLng = west + x * lngCell;
        double northeastLng = west + x * lngCell + lngCell;
        double totalLat = north - south;
        double latCell = totalLat / getYCells();
        double southwestLat = south + y * latCell;
        double northeastLat = south + y * latCell + latCell;
        LatLng southwest = new LatLng(southwestLat, southwestLng);
        LatLng northeast = new LatLng(northeastLat, northeastLng);
        LatLngBounds cellBounds = new LatLngBounds(southwest, northeast);
        return cellBounds;
    }

    /**
     * Returns whether the configuration provided to the constructor is valid.
     * @return whether this AreaDivider can divide a valid area.
     */
    public final boolean isValid() {
        if (north <= south) {
            return false;
        } else if (east <= west) {
            return false;
        } else if (cellSize <= 0) {
            return false;
        }
        return true;
    }

    /**
     * Draws the grid to a map using solid black polylines.
     * @param map the Google map to draw on.
     */
    public final void renderGrid(final GoogleMap map) {
        double gridWidthLng = east - west;
        double cellLng = gridWidthLng / getXCells();
        for (int i = 0; i <= getXCells(); i++) {
            LatLng start = new LatLng(south, i * cellLng + west);
            LatLng end = new LatLng(north, i * cellLng + west);
            PolylineOptions fill = new PolylineOptions().add(start, end).color(Color.BLACK).width(1);
            map.addPolyline(fill);
        }
        double gridHeightLat = north - south;
        double cellLat = gridHeightLat / getYCells();
        for (int i = 0; i <= getYCells(); i++) {
            LatLng start = new LatLng(i * cellLat + south, west);
            LatLng end = new LatLng(i * cellLat + south, east);
            PolylineOptions fill = new PolylineOptions().add(start, end).color(Color.BLACK).width(1);
            map.addPolyline(fill);
        }
    }

}
