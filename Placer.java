package com.example.grim.tutmap;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Grim on 05.02.2016.
 */
public class Placer {
    public List <Placer> placerList;
    @SerializedName("id")
    public String id;
    @SerializedName("icon")
    public String icon;
    @SerializedName("name")
    public String name;
    @SerializedName("latitude")
    public Double latitude;
    @SerializedName("longitude")
    public Double longitude;

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getIcon() {
        return icon;
    }
    public void setIcon(String icon) {
        this.icon = icon;
    }
    public Double getLatitude() {
        return latitude;
    }
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    public Double getLongitude() {
        return longitude;
    }
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


//
//
//    static Placer jsonDetailGetting(JSONObject detailGetting) {
//        try {
//            Placer result = new Placer();
//            JSONObject geometry = (JSONObject) detailGetting.get("geometry");
//            JSONObject location = (JSONObject) geometry.get("location");
//            result.setLatitude((Double) location.get("lat"));
//            result.setLongitude((Double) location.get("lng"));
//            result.setIcon(detailGetting.getString("icon"));
//            result.setName(detailGetting.getString("name"));
//            result.setId(detailGetting.getString("id"));
//            return result;
//        } catch (JSONException ex) {
//            Logger.getLogger(Placer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return null;
//    }
//
//    @Override
//    public String toString() {
//        return "Place{" + "id=" + id + ", icon=" + icon + ", name=" + name + ", latitude=" + latitude + ", longitude=" + longitude + '}';
//    }
}
