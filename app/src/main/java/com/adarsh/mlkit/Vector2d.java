package com.adarsh.mlkit;

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

        double crossProd = (other.x * this.y) - (this.x * other.y);

        return Math.asin(crossProd);
    }
}
