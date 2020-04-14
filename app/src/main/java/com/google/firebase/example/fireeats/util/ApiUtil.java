package com.google.firebase.example.fireeats.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

public class ApiUtil {
    private String result;
    private JSONObject resultObj;
    private JSONArray resultArray;
    private double userLat;
    private double userLng;

    public ApiUtil(String result, double userLat, double userLng){
        this.result = result;
        this.userLat = userLat;
        this.userLng = userLng;
    }

    // Filters and returns the coordinates of all taxi
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
