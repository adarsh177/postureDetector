package com.adarsh.mlkit.Utils;

import android.util.Log;

import com.adarsh.mlkit.Models.CustomPoseLandmark;
import com.google.mlkit.vision.pose.PoseLandmark;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;

public class PoseUtils {
    private final double ALLOWED_RADIAN_DEVIATION = 0.15;
    public final static int MINIMUM_MATCH_PERCENT = 70;

    private final int[][] comparisionPoints = {
            {PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW},
            {PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST},
            {PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW},
            {PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST},
            {PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE},
            {PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE},
            {PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE},
            {PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE},
    };

    // returns true if pose is similar else returns ArrayList<CustomePoseLandmark> with point of errors
    public ArrayList<CustomPoseLandmark> ComparePose(HashMap<String, CustomPoseLandmark> sample, HashMap<String, CustomPoseLandmark> test){
        boolean allMatching;
        double angleSample, angleTest;
        ArrayList<CustomPoseLandmark> wrongPoints = new ArrayList<>();
        float totalPercent = 0;

        Vector2d sampleSpine = getSpineVector(sample);
        Vector2d testSpine = getSpineVector(test);

        Vector2d samplePart, testPart;

        for(int[] couple : comparisionPoints){
            samplePart = new Vector2d(sample.get("lm_" + couple[1]).x, sample.get("lm_" + couple[1]).y, sample.get("lm_" + couple[0]).x, sample.get("lm_" + couple[0]).y);
            testPart = new Vector2d(test.get("lm_" + couple[1]).x, test.get("lm_" + couple[1]).y, test.get("lm_" + couple[0]).x, test.get("lm_" + couple[0]).y);

            angleSample = samplePart.getThisVectorsAngleOnAnother(sampleSpine);
            if(sample.get("lm_" + couple[1]).x < sample.get("lm_" + couple[0]).x) angleSample = (Math.PI * 2) - angleSample;

            angleTest = testPart.getThisVectorsAngleOnAnother(testSpine);
            if(test.get("lm_" + couple[1]).x < test.get("lm_" + couple[0]).x) angleTest = (Math.PI * 2) - angleTest;

            Log.d("debugg", "ANGLESWEGOT(" + couple[0] + ") : " + angleSample + ", " + angleTest);
            allMatching = compareAngleWithRange(angleSample, angleTest, ALLOWED_RADIAN_DEVIATION);

            double match = getPercentMatch(angleSample, angleTest);
            totalPercent += match;

            Log.d("debugg", "ANGLEDIFF(" + couple[0] + ") :" + (100 - match));

            Log.d("debugg", "INDIVIDUAL MATCH(" + couple[0] + "): " + getPercentMatch(angleSample, angleTest));

            if(!allMatching){
                if(test.containsKey("lm_" + couple[0])){
                    wrongPoints.add(test.get("lm_" + couple[0]));
                }else Log.d("debugg", "COMPAREPOSE: TEST HAVING NULL AT: " + couple[1]);
            }
        }

        Log.d("debugg", "Percent Match : " + (totalPercent / ((float)comparisionPoints.length)));

        wrongPoints.add(new CustomPoseLandmark((totalPercent / ((float)comparisionPoints.length)), 0));

        return wrongPoints;
    }

    private double getPercentMatch(double angleSample, double angleTest){
        if(Math.abs(angleSample - angleTest) > ((2 * Math.PI) - (2 * ALLOWED_RADIAN_DEVIATION))){
            double diffPercent = ((2 * ALLOWED_RADIAN_DEVIATION) / (2 * Math.PI)) * 100d;
            return (100d - diffPercent);
        }else{
            double diffPercent = (Math.abs(angleSample - angleTest) / angleSample) * 100d;
            return (100d - diffPercent);
        }
    }

    // compares angleTest with angleSample in given percent range and returns true if equality holds else returns false
    private boolean compareAngleWithRange(double angleSample, double angleTest, double allowedDeviation){
        double ll = angleSample - allowedDeviation;
        double ul = angleSample + allowedDeviation;

        if(ll >= 0 && ul <= (2 * Math.PI)){
            return (ll <= angleTest && angleTest <= ul);
        }else{
            if(ll < 0){
                return angleTest >= ((2 * Math.PI) + ll);
            }else{
                return angleTest <= ul - (2 * Math.PI);
            }
        }

//        return (ll <= angleTest && angleTest <= ul);
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

    private Vector2d getSpineVector(HashMap<String, CustomPoseLandmark> landmarkHashMap){
        double topX = (landmarkHashMap.get("lm_" + PoseLandmark.LEFT_SHOULDER).x + landmarkHashMap.get("lm_" + PoseLandmark.RIGHT_SHOULDER).x) / 2;
        double topY = (landmarkHashMap.get("lm_" + PoseLandmark.LEFT_SHOULDER).y + landmarkHashMap.get("lm_" + PoseLandmark.RIGHT_SHOULDER).y) / 2;
        double btmX = (landmarkHashMap.get("lm_" + PoseLandmark.LEFT_HIP).x + landmarkHashMap.get("lm_" + PoseLandmark.RIGHT_HIP).x) / 2;
        double btmY = (landmarkHashMap.get("lm_" + PoseLandmark.LEFT_HIP).y + landmarkHashMap.get("lm_" + PoseLandmark.RIGHT_HIP).y) / 2;

        return new Vector2d(btmX, btmY, topX, topY);
    }
}
