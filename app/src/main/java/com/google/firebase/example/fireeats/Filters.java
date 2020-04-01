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

    private long userCoordinatesX = 1405256000000000L;
    private long userCoordinatesY = 103902334;

    private long xup = 1550278000000000L + userCoordinatesY ; //1398333000000000L;
    private long xdown = 1260278000000000L + userCoordinatesY; //130833300000000L;
    private long yup = userCoordinatesX + 104047200; //103727800;
    private long ydown = userCoordinatesX + 103557200; //103637800;

    public Filters() {}

    public static Filters getDefault() {
        Filters filters = new Filters();
        filters.setSortBy(Restaurant.FIELD_AVG_RATING);
        filters.setSortDirection(Query.Direction.DESCENDING);

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
        switch(city){
            case "Nearby":
                this.xup = 1450278000000000L + userCoordinatesY ; //1398333000000000L;
                this.xdown = 1360278000000000L + userCoordinatesY; //130833300000000L;
                this.yup = userCoordinatesX + 103947200; //103727800;
                this.ydown = userCoordinatesX + 103857200; //103637800;
                break;

        }
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

    public double getXup() {
        return xup;
    }

    public void setXup(long xup) {
        this.xup = xup;
    }

    public double getXdown() {
        return xdown;
    }

    public void setXdown(long xdown) {
        this.xdown = xdown;
    }

    public double getYup() {
        return yup;
    }

    public void setYup(long yup) {
        this.yup = yup;
    }

    public double getYdown() {
        return ydown;
    }

    public void setYdown(long ydown) {
        this.ydown = ydown;
    }
}
