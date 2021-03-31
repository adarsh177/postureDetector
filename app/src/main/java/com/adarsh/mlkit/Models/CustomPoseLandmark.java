package com.adarsh.mlkit.Models;

import android.util.Log;

import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseLandmark;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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

