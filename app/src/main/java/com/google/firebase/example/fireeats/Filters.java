/**
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 package com.google.firebase.example.fireeats;

import android.content.Context;
import android.text.TextUtils;

import com.google.firebase.example.fireeats.model.Restaurant;
import com.google.firebase.example.fireeats.util.RestaurantUtil;
import com.google.firebase.firestore.Query;

import java.text.DecimalFormat;

import static java.lang.StrictMath.abs;

/**
 * Object for passing filters around.
 */
public class Filters {

    private String category = null;
    private String city = null;
    private int price = -1;
    private String sortBy = null;
    private Query.Direction sortDirection = null;

    private double userCoordinatesLat;// = 1.369053; //1348326103683129L
    private double userCoordinatesLon;// = 103.960883; //103683129

    private long upperLimit = -1;
    private long lowerLimit = -1;

    private double[] calCoordinates = {0,0};

    // Set the upper and lower coordinate limits based on user coordinate
    public Filters() {
        getNewCoordinates(/*latitude*/ getUserCoordinatesLat(),
                /*longitude*/ getUserCoordinatesLon(),
                /*bearing*/315, /*distance(nm)*/3);
        setUpperLimit(getCalCoordinatesLat(), getCalCoodinatesLong());

        getNewCoordinates(/*latitude*/ getUserCoordinatesLat(),
                /*longitude*/ getUserCoordinatesLon(),
                /*bearing*/135, /*distance(nm``)*/3);
        setLowerLimit(getCalCoordinatesLat(), getCalCoodinatesLong());
    }

    public Filters(double userCoorLat, double userCoorLng) {
        this.setUserCoordinatesLat(userCoorLat);
        this.setUserCoordinatesLon(userCoorLng);
        getNewCoordinates(/*latitude*/ getUserCoordinatesLat(),
                /*longitude*/ getUserCoordinatesLon(),
                /*bearing*/315, /*distance(nm)*/3);
        setUpperLimit(getCalCoordinatesLat(), getCalCoodinatesLong());

        getNewCoordinates(/*latitude*/ getUserCoordinatesLat(),
                /*longitude*/ getUserCoordinatesLon(),
                /*bearing*/135, /*distance(nm``)*/3);
        setLowerLimit(getCalCoordinatesLat(), getCalCoodinatesLong());
    }

    public static Filters getDefault() {
        Filters filters = new Filters();
        filters.setSortBy(Restaurant.FIELD_AVG_RATING);
        filters.setSortDirection(Query.Direction.DESCENDING);
        filters.setCity("Nearby");

        return filters;
    }

    public boolean hasCategory() {
        return !(TextUtils.isEmpty(category));
    }

    public boolean hasCity() {
        return !(TextUtils.isEmpty(city));
    }

    public boolean hasPrice() {
        return (price > 0);
    }

    public boolean hasSortBy() {
        return !(TextUtils.isEmpty(sortBy));
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public Query.Direction getSortDirection() {
        return sortDirection;
    }

    public void setSortDirection(Query.Direction sortDirection) {
        this.sortDirection = sortDirection;
    }

    public String getSearchDescription(Context context) {
        StringBuilder desc = new StringBuilder();

        if (category == null && city == null) {
            desc.append("<b>");
            desc.append(context.getString(R.string.all_restaurants));
            desc.append("</b>");
        }

        if (category != null) {
            desc.append("<b>");
            desc.append(category);
            desc.append("</b>");
        }

        if (category != null && city != null) {
            desc.append(" in ");
        }

        if (city != null) {
            desc.append("<b>");
            desc.append(city);
            desc.append("</b>");
        }

        if (price > 0) {
            desc.append(" for ");
            desc.append("<b>");
            desc.append(RestaurantUtil.getPriceString(price));
            desc.append("</b>");
        }

        return desc.toString();
    }

    public String getOrderDescription(Context context) {
        if (Restaurant.FIELD_PRICE.equals(sortBy)) {
            return context.getString(R.string.sorted_by_price);
        } else if (Restaurant.FIELD_POPULARITY.equals(sortBy)) {
            return context.getString(R.string.sorted_by_popularity);
        } else {
            return context.getString(R.string.sorted_by_rating);
        }
    }

    public double getUserCoordinatesLat() {
        return userCoordinatesLat;
    }

    public void setUserCoordinatesLat(double userCoordinatesLat) {
        this.userCoordinatesLat = userCoordinatesLat;
    }

    public double getUserCoordinatesLon() {
        return userCoordinatesLon;
    }

    public void setUserCoordinatesLon(double userCoordinatesLon) {
        this.userCoordinatesLon = userCoordinatesLon;
    }

    public long getUpperLimit() {
        return upperLimit;
    }

    public void setUpperLimit(double lat, double lon) {
        long lat1 = Math.round((lat*Math.pow(10, 6)));
        long lon1 = Math.round((lon*Math.pow(10,6)));
        this.upperLimit = (long) (lon1*Math.pow(10,7) + lat1);
    }

    public long getLowerLimit() {
        return lowerLimit;
    }

    public void setLowerLimit(double lat, double lon) {
        long lat1 = Math.round((lat*Math.pow(10, 6)));
        long lon1 = Math.round((lon*Math.pow(10,6)));
        this.lowerLimit = (long) (lon1*Math.pow(10,7) + lat1);
    }

    // Calculates the new coordinates based
    // tc -> bearing. north (0 degrees), northeast (315 degrees)
    // d -> nautical miles
    private void getNewCoordinates(double lat1, double lon1, double tc, double d) {
        // convert to radians
        lat1 = lat1 * Math.PI / 180;
        lon1 = lon1 * Math.PI / 180;
        tc = tc * Math.PI / 180;
        d = (Math.PI / (180*60)) * d;

        double lat = Math.asin(
                Math.sin(lat1) * Math.cos(d)
                        + Math.cos(lat1) * Math.sin(d) * Math.cos(tc)
        );

        double dlon= Math.atan2(
                Math.sin(tc) * Math.sin(d) * Math.cos(lat1)
                , Math.cos(d) - Math.sin(lat1) * Math.sin(lat));
        double lon = ((lon1 - dlon + Math.PI) % (2 * Math.PI)) - Math.PI;

        // convert to degrees
        lat = lat * 180 / Math.PI;
        lon = lon * 180 / Math.PI;

        setCalCoordinatesLat(lat);
        setCalCoordinatesLong(lon);
    }

    private void setCalCoordinatesLong(double lon) {
        this.calCoordinates[1] = lon;
    }

    private void setCalCoordinatesLat(double lat){
        this.calCoordinates[0] = lat;
    }

    private double getCalCoodinatesLong(){
        return this.calCoordinates[1];
    }

    private double getCalCoordinatesLat(){
        return this.calCoordinates[0];
    }
}
