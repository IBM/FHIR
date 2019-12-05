/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.search.location.area;

/**
 * The maximum number of BoundedBoxes for a single coordinate is 4.<br>
 * lat = 0, long 0 with any boundary the result is 4 Bounded Boxes.<br>
 * <br>
 * The bounded boxes must adhere to the following constraints: <br>
 * Latitude: 90 to 0 to -90 <br>
 * Longitude: 180 to 0 -180 <br>
 * 
 * 
 */
public class Point {

    private Double lat;
    private Double lon;

    private Point() {
        // No Operation 
    }

    protected void setLat(Double lat) {
        this.lat = lat;
    }

    protected void setLon(Double lon) {
        this.lon = lon;
    }

    public Double getLat() {
        return lat;
    }

    public Double getLon() {
        return lon;
    }

    public void validate() {
        if( checkNull(lon) || checkNull(lat) || !checkLatValid(lat) || !checkLonValid(lon)) {
            throw new IllegalArgumentException("Null or Invalid number for the lat/lon");
        }
    }
    
    private boolean checkNull(Double val) {
        return val == null;
    }
    
    private boolean checkLatValid(Double lat) {
        return lat >= -90 && lat <= 90.0;
    }
    
    private boolean checkLonValid(Double lon) {
        return lon >= -180 && lon <= 180.0;
    }

    /**
     * Builds the latitude and longitude.
     */
    public static class Builder {
        private Point box = new Point();

        public Builder latitude(Double lat) {
            box.setLat(lat);
            return this;
        }

        public Builder longitude(Double lon) {
            box.setLon(lon);
            return this;
        }

        public Point build() {
            box.validate();
            return box;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}