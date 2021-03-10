package com.adarsh.mlkit;

import android.util.Log;

import com.google.mlkit.vision.pose.PoseLandmark;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PoseUtils {
    private final double PERCENT_RANGE = 25;

    private final int[][] comparisionPoints = {
            {PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_SHOULDER},  // right elbow joint
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST},     // left elbow joint
            {PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW},    // right shoulder joint
            {PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP},       // left shoulder joint
            {PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_SHOULDER},     // right hip joint
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE},        // left hip joint
            {PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE},           // left knee joint
            {PoseLandmark.RIGHT_ANKLE, PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_HIP},        // right knee joint
    };

    // returns true if pose is similar else returns ArrayList<CustomePoseLandmark> with point of errors
    public ArrayList<CustomPoseLandmark> ComparePose(HashMap<String, CustomPoseLandmark> sample, HashMap<String, CustomPoseLandmark> test){
        boolean allMatching;
        double angleSample, angleTest;
        ArrayList<CustomPoseLandmark> wrongPoints = new ArrayList<>();
        for(int[] truple : comparisionPoints){
            angleSample = getAngleFromPoints(sample.get("lm_" + truple[0]), sample.get("lm_" + truple[1]), sample.get("lm_" + truple[2]));
            angleTest = getAngleFromPoints(test.get("lm_" + truple[0]), test.get("lm_" + truple[1]), test.get("lm_" + truple[2]));
            allMatching = compareAngleWithRange(angleSample, angleTest, PERCENT_RANGE);

            if(!allMatching){
                if(test.containsKey("lm_" + truple[1])){
                    wrongPoints.add(test.get("lm_" + truple[1]));
                }else Log.d("debugg", "COMPAREPOSE: TEST HAVING NULL AT: " + truple[1]);
            }
        }

        return wrongPoints;
    }

    // compares angleTest with angleSample in given percent range and returns true if equality holds else returns false
    private boolean compareAngleWithRange(double angleSample, double angleTest, double percentRange){
        double ll = angleSample * (100 - percentRange) / 100d;
        double ul = angleSample * (100 + percentRange) / 100d;

        return (ll <= angleTest && angleTest <= ul);
    }

    // angle at point B will be returned in radians
    private double getAngleFromPoints(@NotNull CustomPoseLandmark pointA, @NotNull CustomPoseLandmark pointB, @NotNull CustomPoseLandmark pointC){
        double sideAB = Math.sqrt(Math.pow(pointA.x - pointB.x, 2) + Math.pow(pointA.y - pointB.y, 2));
        double sideBC = Math.sqrt(Math.pow(pointB.x - pointC.x, 2) + Math.pow(pointB.y - pointC.y, 2));
        double sideAC = Math.sqrt(Math.pow(pointA.x - pointC.x, 2) + Math.pow(pointA.y - pointC.y, 2));

        double cosVal = (Math.pow(sideAB, 2) + Math.pow(sideBC, 2) - Math.pow(sideAC, 2)) / (2 * sideAB * sideBC);
        if(cosVal > 1){
            Log.d("posedebug", "Invalid cos : " + sideAB + ", " + sideBC + ", " + sideAC + "|| " + pointB.x + ", " + pointB.y);
        }
        return Math.acos(cosVal);
    }
}
