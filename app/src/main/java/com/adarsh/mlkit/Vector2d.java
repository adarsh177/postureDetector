package com.adarsh.mlkit;

import android.util.Log;

import com.google.mlkit.vision.pose.PoseLandmark;

import java.util.HashMap;

public class Vector2d {
    double x, y;

    public Vector2d(double x, double y, double z){
        this.x = x;
        this.y = y;
    }

    public Vector2d(double x1, double y1, double x2, double y2){
        this.x = x2 - x1;
        this.y = y2 - y1;
    }

    public void ConvertThisToUnitVector(){
        double d = Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
        x /= d;
        y /= d;
    }

    // this vector being treated as starting point for anticlock-swipe
    public double getThisVectorsAngleOnAnother(Vector2d other){
        // making sure it's in unit vector form
        ConvertThisToUnitVector();
        other.ConvertThisToUnitVector();

        double dotProd =  (this.x * other.x) + (other.y * this.y);

        Log.d("debugg", "DOTPROD: " + dotProd);

        return Math.acos(dotProd);
    }
}
