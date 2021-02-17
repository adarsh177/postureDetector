package com.adarsh.mlkit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Button btnFetch, btnTest;
    EditText etUrl, etResult;

    PoseDetector detector;
    boolean imageLoadedSuccessfully = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        loadInteractions();
    }

    private void loadInteractions(){
        btnFetch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Picasso.get().load(etUrl.getText().toString()).into(imageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        imageLoadedSuccessfully = true;
                        Toast.makeText(getApplicationContext(), "Image Fetched!", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(Exception e) {
                        imageLoadedSuccessfully = false;
                        Log.e("debugg", "Error loading image", e);
                        Toast.makeText(getApplicationContext(), "Error Fetching Image, please check the url provided", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        btnTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(imageLoadedSuccessfully){
                    runTest();
                }else{
                    Toast.makeText(getApplicationContext(), "Image not loaded", Toast.LENGTH_LONG).show();
                }
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, CamActivity.class), 100);
            }
        });
    }

    private void useImage(){
        imageView.setImageBitmap(ApplicationClass.capturedImage);
        imageLoadedSuccessfully = true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if(ApplicationClass.capturedImage == null){
                Toast.makeText(getApplicationContext(), "No Image Captured", Toast.LENGTH_SHORT).show();
            }else{
                useImage();
            }
        }
    }

    private void initViews(){
        imageView = findViewById(R.id.image);
        btnTest = findViewById(R.id.btnTest);
        btnFetch = findViewById(R.id.btnFetch);
        etUrl = findViewById(R.id.imageUrl);
        etResult = findViewById(R.id.result);
    }

    private void runTest(){
        if(detector == null){
            AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder().setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE).build();
            detector = PoseDetection.getClient(options);
        }

        // running test
        imageView.invalidate();
        Bitmap bmp = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        Toast.makeText(getApplicationContext(), "Detecting, please wait", Toast.LENGTH_LONG).show();
        detector.process(InputImage.fromBitmap(bmp, 0)).addOnCompleteListener(new OnCompleteListener<Pose>() {
            @Override
            public void onComplete(@NonNull Task<Pose> task) {
                if(task.isSuccessful()){
                    Pose pose = task.getResult();
                    List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
                    if(landmarks.size() == 0){
                        Toast.makeText(getApplicationContext(), "No Point detected in image, please try another image", Toast.LENGTH_LONG).show();
                    }

                    String text = "Image Dimensions(w/h) : " + ApplicationClass.capturedImage.getWidth() + "/" + ApplicationClass.capturedImage.getHeight() + "\n\n\nResponse\n\n";

                    text += "LEFT_SHOULDER: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition().y) + "\n";
                    text += "RIGHT_SHOULDER: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition().y) + "\n";
                    text += "LEFT_ELBOW: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW).getPosition().y) + "\n";
                    text += "RIGHT_ELBOW: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW).getPosition().y) + "\n";
                    text += "LEFT_WRIST: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_WRIST).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_WRIST).getPosition().y) + "\n";
                    text += "RIGHT_WRIST: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).getPosition().y) + "\n";
                    text += "LEFT_HIP: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_HIP).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_HIP).getPosition().y) + "\n";
                    text += "RIGHT_HIP: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).getPosition().y) + "\n";
                    text += "LEFT_KNEE: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_KNEE).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_KNEE).getPosition().y) + "\n";
                    text += "RIGHT_KNEE: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE).getPosition().y) + "\n";
                    text += "LEFT_ANKLE: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE).getPosition().y) + "\n";
                    text += "RIGHT_ANKLE: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE).getPosition().y) + "\n";
                    text += "LEFT_PINKY: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_PINKY).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_PINKY).getPosition().y) + "\n";
                    text += "RIGHT_PINKY: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_PINKY).getPosition().y) + "\n";
                    text += "LEFT_INDEX: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_INDEX).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_INDEX).getPosition().y) + "\n";
                    text += "RIGHT_INDEX: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX).getPosition().y) + "\n";
                    text += "LEFT_THUMB: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_THUMB).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_THUMB).getPosition().y) + "\n";
                    text += "RIGHT_THUMB: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_THUMB).getPosition().y) + "\n";
                    text += "LEFT_HEEL: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_HEEL).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_HEEL).getPosition().y) + "\n";
                    text += "RIGHT_HEEL: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_HEEL).getPosition().y) + "\n";
                    text += "LEFT_FOOT_INDEX: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX).getPosition().y) + "\n";
                    text += "RIGHT_FOOT_INDEX: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX).getPosition().y) + "\n";
                    text += "NOSE: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.NOSE).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.NOSE).getPosition().y) + "\n";
                    text += "LEFT_EYE_INNER: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER).getPosition().y) + "\n";
                    text += "LEFT_EYE: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_EYE).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_EYE).getPosition().y) + "\n";
                    text += "LEFT_EYE_OUTER: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER).getPosition().y) + "\n";
                    text += "RIGHT_EYE_INNER: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER).getPosition().y) + "\n";
                    text += "RIGHT_EYE: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE).getPosition().y) + "\n";
                    text += "RIGHT_EYE_OUTER: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER).getPosition().y) + "\n";
                    text += "LEFT_EAR: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_EAR).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_EAR).getPosition().y) + "\n";
                    text += "RIGHT_EAR: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_EAR).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_EAR).getPosition().y) + "\n";
                    text += "LEFT_MOUTH: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH).getPosition().y) + "\n";
                    text += "RIGHT_MOUTH: " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition().x) + ", " + String.valueOf(pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition().y) + "\n";


                    etResult.setText(text);

                    Toast.makeText(getApplicationContext(), "Results Loaded", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Error processing test", Toast.LENGTH_LONG).show();
                    Log.e("debugg", "Error in test", task.getException());
                }
            }
        });
    }
}