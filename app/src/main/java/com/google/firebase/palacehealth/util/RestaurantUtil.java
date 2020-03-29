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
 package com.google.firebase.palacehealth.util;

import com.google.firebase.palacehealth.model.Restaurant;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

// Utilities for Restaurants.
public class RestaurantUtil {

    private static final String TAG = "RestaurantUtil";

    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(2, 4, 60,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    // Get price represented as dollar signs.
    public static String getPriceString(Restaurant restaurant) {
        return getPriceString(restaurant.getPrice());
    }

    // Get price represented as dollar signs.
    public static String getPriceString(int priceInt) {
        switch (priceInt) {
            case 1:
                return "$";
            case 2:
                return "$$";
            case 3:
            default:
                return "$$$";
        }
    }
}
