package com.adarsh.mlkit;

import android.util.Log;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

// as landmark type is an int and for unique id string is preferrable hence
// all landmark type are now a string : lm_{landmarkType}
// example lm_1, lm_2, lm_5 etc
public class CustomPose{
    public HashMap<String, CustomPoseLandmark> landmarkHashMap;

    public CustomPose(Pose pose){
        landmarkHashMap = new HashMap<>();
        for(PoseLandmark landmark : pose.getAllPoseLandmarks()){
            landmarkHashMap.put("lm_".concat(String.valueOf(landmark.getLandmarkType())),new CustomPoseLandmark(landmark));
        }
    }

    public CustomPose(JSONObject object){
        landmarkHashMap = new HashMap<>();
        try{
            JSONArray coords;
            for (Iterator<String> it = object.keys(); it.hasNext(); ) {
                String key = it.next();
                coords = object.getJSONArray(key);
                landmarkHashMap.put(key, new CustomPoseLandmark(Double.parseDouble(String.valueOf(coords.get(0))), Double.parseDouble(String.valueOf(coords.get(1)))));
                Log.d("debugg", "CUSTOMPOSE: " + Double.parseDouble(String.valueOf(coords.get(0))) + ", " +  Double.parseDouble(String.valueOf(coords.get(1))));
            }
        }catch (Exception e){
            Log.e("debugg", "Error Loading JSON object: CustomPose", e);
        }
    }

    public CustomPose(){
        landmarkHashMap = new HashMap<>();
    }

}

public class CustomPoseLandmark {
    public double x, y;

    public CustomPoseLandmark(PoseLandmark landmark){
        this.x = landmark.getPosition().x;
        this.y = landmark.getPosition().y;
    }

    public CustomPoseLandmark(double x, double y){
        this.x = x;
        this.y = y;
    }
}

