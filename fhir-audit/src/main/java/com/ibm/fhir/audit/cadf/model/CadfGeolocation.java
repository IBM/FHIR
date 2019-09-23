/*
 * (C) Copyright IBM Corp. 2019
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package com.ibm.fhir.audit.cadf.model;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Representation of the CADF Geolocation type. Geolocation information, which
 * reveals a resource’s physical location, is obtained by using tracking
 * technologies such as global positioning system (GPS) devices, or IP
 * geolocation by using databases that map IP addresses to geographic locations.
 * Geolocation information is widely used in context-sensitive content delivery,
 * enforcing location-based access restrictions on services, and fraud detection
 * and prevention. Due to the intense concerns about security and privacy,
 * countries and regions introduced various legislation and regulation. To
 * determine whether an event is compliant sometimes depends on the geolocation
 * of the event. Therefore, it is crucial to report geolocation information
 * unambiguously in an audit trail.
 */
public final class CadfGeolocation {
    private final String id;
    private final String latitude;
    private final String longitude;
    private final Double elevation;
    private final Double accuracy;
    private final String city;
    private final String state;
    private final String regionICANN;
    private final ArrayList<CadfMapItem> annotations;

    private CadfGeolocation(Builder builder) {
        this.id = builder.id;
        this.latitude = builder.latitude;
        this.longitude = builder.longitude;
        this.elevation = builder.elevation;
        this.accuracy = builder.accuracy;
        this.city = builder.city;
        this.state = builder.state;
        this.regionICANN = builder.regionICANN;
        this.annotations = builder.annotations;
    }

    /**
     * Validate contents of the geolocation type.
     * 
     * The logic is determined by the CADF specification. In short, either
     * longitude/latitude or city and region must be present.
     * 
     * @throws IllegalStateException when the properties do not meet the
     *                               specification.
     */
    private void validate() throws IllegalStateException {
        if (arePresent(this.latitude, this.longitude) || arePresent(this.city, this.regionICANN)) {
            return;
        } else {
            throw new IllegalStateException("missing required location information");
        }
    }

    private boolean isPresent(String s) {
        if (s != null && !s.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    private boolean arePresent(String s1, String s2) {
        if (isPresent(s1) && isPresent(s2)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * @return the elevation
     */
    public Double getElevation() {
        return elevation;
    }

    /**
     * @return the accuracy
     */
    public Double getAccuracy() {
        return accuracy;
    }

    /**
     * @return the state
     */
    public String getState() {
        return state;
    }

    /**
     * @return the annotations
     */
    public ArrayList<CadfMapItem> getAnnotations() {
        return annotations;
    }

    /**
     * Builder for the immutable CADF Geolocation object
     */
    public static class Builder {
        private String id;
        private String latitude;
        private String longitude;
        private Double elevation;
        private Double accuracy;
        private String city;
        private String state;
        private String regionICANN;
        private ArrayList<CadfMapItem> annotations;

        /**
         * Geolocation builder using latitude/longitude values.
         * 
         * @param latitude  -- String. Latitude values adhere to the format based on ISO
         *                  6709:2008 Annex H.3.1 – H.3.3. [ISO-6709-2008]
         * @param longitude -- String. Longitude values adhere to the format based on
         *                  ISO 6709:2008 Annex H.3.1 – H.3.3. [ISO-6709-2008]
         * @param elevation -- Double. Elevation in meters.
         * @param accuracy  -- Double. Accuracy of geolocation, in meters.
         */
        public Builder(String latitude, String longitude, Double elevation, Double accuracy) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.elevation = elevation;
            this.accuracy = accuracy;
        }

        /**
         * Geolocation builder using city/state/region.
         * 
         * @param city        - String. Location city.
         * @param state       - String. Location state or province, optional.
         * @param regionICANN - String. Location region -- ICANN country code per top
         *                    level domain (ccTLD) naming convention [IANA-ccTLD]. May
         *                    be upper- or lowercase.
         * @param accuracy    -- Double. Accuracy of geolocation, in meters.
         */
        public Builder(String city, String state, String regionICANN, Double accuracy) {
            this.city = city;
            this.state = state;
            this.regionICANN = regionICANN;
            this.accuracy = accuracy;
        }

        /**
         * Optionally add ICANN regioin data to the location created using
         * latitude/longitude values.
         * 
         * @param regionICANN - String. ICANN country code per top level domain (ccTLD)
         *                    naming convention [IANA-ccTLD]. May be upper- or
         *                    lowercase.
         * @return Builder
         */
        public Builder withRegion(String regionICANN) {
            this.regionICANN = regionICANN;
            return this;
        }

        /**
         * Optionally set the location identifier.
         * 
         * @param id - String. URI of the location.
         * @return This builder
         */
        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        /**
         * Optionally set arbitrary annotations for this location
         * 
         * @param annotations An array of key-value annotations
         * @return This builder
         */
        public Builder withAnnotations(CadfMapItem[] annotations) {
            this.annotations = new ArrayList<CadfMapItem>(Arrays.asList(annotations));
            return this;
        }

        /**
         * Optionally set arbitrary annotations for this location
         * 
         * @param annotations An array of key-value annotations
         * @return This builder
         */
        public Builder withAnnotations(ArrayList<CadfMapItem> annotations) {
            this.annotations = annotations;
            return this;
        }

        /**
         * Build an immutable geolocation object
         * 
         * @return CadfGeolocation
         * @throws IllegalStateException when the properties do not meet the
         *                               specification.
         */
        public CadfGeolocation build() throws IllegalStateException {
            CadfGeolocation loc = new CadfGeolocation(this);
            loc.validate();
            return loc;
        }
    }

    public String getCity() {
        return city;
    }

    public String getRegionICANN() {
        return regionICANN;
    }
}
