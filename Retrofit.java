package com.example.grim.tutmap;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.android.AndroidLog;
import retrofit.converter.GsonConverter;
import retrofit.http.GET;
import retrofit.http.Query;
import retrofit.http.QueryMap;

/**
 * Created by Grim on 05.02.2016.
 */
public class Retrofit {
    private static final String API_KEY = "AIzaSyBEchazmtBPqERNwAzqOaoDwwOegjIpRAA";
    private static final String ENDPOINT = "https://maps.googleapis.com/maps/api";
    private static ApiInterface apiInterface;
    private static Map.RouteApi routeApi;
    private Location location;
    private String provider;
    private final String TAG = "LOOOOOOOOOOOOOOG:";
    static Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();


    static {
        initialize();
    }


    interface ApiInterface {
        @GET("/place/search/json")
        void getCurrentUser(@QueryMap HashMap <String, Object> map, Callback <List <Placer>> callback);

    }

    private static void initialize() {

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(ENDPOINT)
                .setLogLevel(RestAdapter.LogLevel.FULL).setLog(new AndroidLog("TAG"))
                .setConverter(new GsonConverter(gson))
                .build();
        apiInterface = restAdapter.create(ApiInterface.class);
    }
    public static void getPlaces(double latitude, double longitude, String place, Callback  <List <Placer>> callback){
        HashMap <String, Object> map = new HashMap<>();
        map.put("&location", Double.toString(latitude) + "," + Double.toString(longitude));
        map.put("&radius", 5000);
        map.put("&types", place);
        map.put("&sensor", false);
        map.put("&key", API_KEY);
        apiInterface.getCurrentUser(map, callback);
    }
}
