package com.google.firebase.example.fireeats.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Utilities to query coordinates of available taxis from govt data Available Taxi API
 */
public class ApiUtil {
    private String result;
    private JSONObject resultObj;
    private JSONArray resultArray;
    private double userLat;
    private double userLng;

    /**
     * Constructor for ApiUtil
     * @param result is the json string returned from govt data api
     * @param userLat is the user's current lat
     * @param userLng is the user's current long
     */
    public ApiUtil(String result, double userLat, double userLng){
        this.result = result;
        this.userLat = userLat;
        this.userLng = userLng;
    }

    /**
     * Queries json string for coordinates of available taxis
     * @return JSONArray of available taxi coordinates
     */
    public JSONArray convertToObj(){
        try{
            resultObj = new JSONObject(result);
            resultArray = resultObj.getJSONArray("features").getJSONObject(0).getJSONObject("geometry").getJSONArray("coordinates");
        } catch (JSONException err) {
            Log.d("Error", err.toString());
        }
        return resultArray;
    }
}
