package com.adarsh.mlkit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    PreviewView previewView;
    PoseDetector detector;
    EditText etResults;
    ImageView guidelineView;
    ImageCapture imageCapture;

    Canvas guidelineCanvas;
    Bitmap guidelineBmp, tempBitmap;
    Paint guidePointPaint, guidePaint, transPaint;

    private final int UPDATE_TIME = 40;
    private boolean isFrameBeingTested = false, canvasAlreadyClear = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        checkPermissions();
    }

    private void loadGuidelines(Bitmap bmp, Pose pose){
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                guidelineBmp = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
                guidelineCanvas = new Canvas(guidelineBmp);

                if(transPaint == null || guidePaint == null){
                    transPaint = new Paint();
                    transPaint.setColor(Color.TRANSPARENT);
                    transPaint.setStyle(Paint.Style.FILL_AND_STROKE);

                    guidePointPaint = new Paint();
                    guidePointPaint.setColor(Color.RED);
                    guidePointPaint.setStrokeWidth(10f);
                    guidePointPaint.setStrokeCap(Paint.Cap.BUTT);
                    guidePointPaint.setStyle(Paint.Style.FILL_AND_STROKE);

                    guidePaint = new Paint();
                    guidePaint.setColor(Color.WHITE);
                    guidePaint.setStrokeWidth(3f);
                    guidePaint.setStrokeCap(Paint.Cap.BUTT);
                    guidePaint.setStyle(Paint.Style.STROKE);
                }

                // setting everything as transparent
                guidelineCanvas.drawColor(Color.TRANSPARENT);
//                guidelineCanvas.drawRect(0, 0, guidelineBmp.getWidth(), guidelineBmp.getHeight(), transPaint);

                // drawing just a rect
                if(pose != null){
                    for(PoseLandmark landmark : pose.getAllPoseLandmarks()){
                        guidelineCanvas.drawCircle(landmark.getPosition().x, landmark.getPosition().y, 6f, guidePointPaint);
                    }

                    // drawing lines
                    // TORSO
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition().y, pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition().y, guidePaint);
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition().y, pose.getPoseLandmark(PoseLandmark.LEFT_HIP).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_HIP).getPosition().y, guidePaint);
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).getPosition().y, pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition().y, guidePaint);
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_HIP).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_HIP).getPosition().y, pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).getPosition().y, guidePaint);

                    //limbs
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER).getPosition().y, pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW).getPosition().y, guidePaint);
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW).getPosition().y, pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER).getPosition().y, guidePaint);
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW).getPosition().y, pose.getPoseLandmark(PoseLandmark.LEFT_WRIST).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_WRIST).getPosition().y, guidePaint);
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW).getPosition().y, pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST).getPosition().y, guidePaint);
                    //
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_HIP).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_HIP).getPosition().y, pose.getPoseLandmark(PoseLandmark.LEFT_KNEE).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_KNEE).getPosition().y, guidePaint);
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_HIP).getPosition().y, pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE).getPosition().y, guidePaint);
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_KNEE).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_KNEE).getPosition().y, pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE).getPosition().y, guidePaint);
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE).getPosition().y, pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE).getPosition().y, guidePaint);

                    //MOUTH
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_MOUTH).getPosition().y, pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_MOUTH).getPosition().y, guidePaint);
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_EAR).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_EAR).getPosition().y, pose.getPoseLandmark(PoseLandmark.LEFT_EYE).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_EYE).getPosition().y, guidePaint);
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.RIGHT_EAR).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_EAR).getPosition().y, pose.getPoseLandmark(PoseLandmark.RIGHT_EYE).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_EYE).getPosition().y, guidePaint);
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.LEFT_EYE).getPosition().x, pose.getPoseLandmark(PoseLandmark.LEFT_EYE).getPosition().y, pose.getPoseLandmark(PoseLandmark.NOSE).getPosition().x, pose.getPoseLandmark(PoseLandmark.NOSE).getPosition().y, guidePaint);
                    guidelineCanvas.drawLine(pose.getPoseLandmark(PoseLandmark.RIGHT_EYE).getPosition().x, pose.getPoseLandmark(PoseLandmark.RIGHT_EYE).getPosition().y, pose.getPoseLandmark(PoseLandmark.NOSE).getPosition().x, pose.getPoseLandmark(PoseLandmark.NOSE).getPosition().y, guidePaint);
                    canvasAlreadyClear = false;
                }else{
                    canvasAlreadyClear = true;
                }

                guidelineView.invalidate();
                guidelineView.setImageBitmap(guidelineBmp);
                Log.d("debugg", "New Guidelines Drawn");
            }
        });

    }

    private void initViews(){
        previewView = findViewById(R.id.viewFinder);
        etResults = findViewById(R.id.etResults);
        guidelineView = findViewById(R.id.canvas);
    }

    private void runTest(){
        if(detector == null){
            AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder().setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE).build();
            detector = PoseDetection.getClient(options);
        }

        tempBitmap = previewView.getBitmap();
        if(previewView.getBitmap() == null){
//            Toast.makeText(getApplicationContext(), "No Photo Visible", Toast.LENGTH_SHORT).show();
            return;
        }

        isFrameBeingTested = true;
        detector.process(InputImage.fromBitmap(tempBitmap, 0)).addOnCompleteListener(new OnCompleteListener<Pose>() {

            @Override
            public void onComplete(@NonNull Task<Pose> task) {
                if(task.isSuccessful()){
                    Pose pose = task.getResult();
                    List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
                    Log.d("debugg", "Landmarks found : " + landmarks.size());
                    if(landmarks.size() == 0){
//                        Toast.makeText(getApplicationContext(), "No Point detected in image, please try another image", Toast.LENGTH_LONG).show();
                        isFrameBeingTested = false;
                        if(!canvasAlreadyClear)
                            loadGuidelines(tempBitmap, null);
                        return;
                    }

                    String text = "";

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

//                    Toast.makeText(getApplicationContext(), "Results Loaded", Toast.LENGTH_SHORT).show();
                    etResults.setText(text);

                    loadGuidelines(tempBitmap, pose);
                    isFrameBeingTested = false;
                }else{
//                    Toast.makeText(getApplicationContext(), "Error processing test", Toast.LENGTH_LONG).show();
                    Log.e("debugg", "Error in test", task.getException());
                    loadGuidelines(tempBitmap, null);
                    isFrameBeingTested = false;
                }
            }
        });
    }

    private void startAnalysis(){
        Handler handler = new Handler(getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                if(!isFrameBeingTested){
                    runTest();
                }
                handler.postDelayed(this, UPDATE_TIME);
            }
        });
    }

    private void startInit(){
        ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider provider = cameraProviderFuture.get();

                    Preview preview = new Preview.Builder().build();
                    preview.setSurfaceProvider(previewView.createSurfaceProvider());
                    imageCapture = new ImageCapture.Builder().build();

                    provider.unbindAll();
                    provider.bindToLifecycle(MainActivity.this, CameraSelector.DEFAULT_BACK_CAMERA, preview);
                    Toast.makeText(getApplicationContext(), "Camera started", Toast.LENGTH_SHORT).show();

                    startAnalysis();
                } catch (Exception e) {
                    Log.e("debugg", "Error Getting camera Provider", e);
                    Toast.makeText(getApplicationContext(), "Errror Loading Camera Provider, Restart App", Toast.LENGTH_SHORT).show();
                }
            }
        },ActivityCompat.getMainExecutor(MainActivity.this));
    }

    private void checkPermissions(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "Camera Permission Request", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 455);
        }else{
            Toast.makeText(getApplicationContext(), "Camera Permission Granted", Toast.LENGTH_SHORT).show();
            startInit();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 455){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(), "Permission not granted !", Toast.LENGTH_SHORT).show();
                finish();
            }else{
                Toast.makeText(getApplicationContext(), "Camera Permission Granted", Toast.LENGTH_SHORT).show();
                startInit();
            }
        }
    }
}