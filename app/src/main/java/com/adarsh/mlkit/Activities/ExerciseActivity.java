package com.adarsh.mlkit.Activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;

import com.adarsh.mlkit.Models.CustomPose;
import com.adarsh.mlkit.Models.CustomPoseLandmark;
import com.adarsh.mlkit.Utils.PoseUtils;
import com.adarsh.mlkit.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ExerciseActivity extends AppCompatActivity {
    PreviewView previewView;
    PoseDetector detector;
    ImageView guidelineView;
    ImageCapture imageCapture;
    TextView btmLable, exName;
    Button photo;

    Canvas guidelineCanvas;
    Bitmap guidelineBmp, tempBitmap;
    Paint guidePointPaint, guidePaint, transPaint, wrongPointPaint;
    ArrayList<CustomPose> configPoses;
    int currentPoseIndex = 0; // pose index to be matched

    PoseUtils poseUtils;

    private final int UPDATE_TIME = 25;
    private boolean isFrameBeingTested = false, canvasAlreadyClear = true, testCompleted = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exercise);
        poseUtils = new PoseUtils();
        initViews();
        loadConfig();
    }

    private void loadConfig(){
        if(!getIntent().hasExtra("config")){
            Log.e("debugg", "Config Not FOund");
            Toast.makeText(getApplicationContext(), "Config Not Found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            String str = getIntent().getStringExtra("config");
            JSONObject obj = new JSONObject(str);
            JSONArray posesArray = obj.getJSONArray("poses");
            configPoses = new ArrayList<>();
            for(int i = 0; i < posesArray.length(); i++){
                configPoses.add(new CustomPose(posesArray.getJSONObject(i)));
            }
            exName.setText(obj.getString("name"));
            btmLable.setText("0/" + configPoses.size());
        } catch (Exception e) {
            Log.e("debugg", "Invalid Config File provided!", e);
            Toast.makeText(getApplicationContext(), "Invalid Config File provided!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        checkPermissions();
    }

    private void loadGuidelines(Bitmap bmp, Pose pose, ArrayList<CustomPoseLandmark> wrongPoints){
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
                    guidePointPaint.setColor(Color.BLUE);
                    guidePointPaint.setStrokeWidth(5f);
                    guidePointPaint.setStrokeCap(Paint.Cap.BUTT);
                    guidePointPaint.setStyle(Paint.Style.STROKE);

                    wrongPointPaint = new Paint();
                    wrongPointPaint.setColor(Color.RED);
                    wrongPointPaint.setStrokeWidth(10f);
                    wrongPointPaint.setStrokeCap(Paint.Cap.BUTT);
                    wrongPointPaint.setStyle(Paint.Style.FILL_AND_STROKE);

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
                        guidelineCanvas.drawCircle(landmark.getPosition().x, landmark.getPosition().y, 8f, guidePointPaint);
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

                    //wrong poses
                    if(wrongPoints != null){
                        for(CustomPoseLandmark landmark : wrongPoints){
                            guidelineCanvas.drawCircle((float)landmark.x, (float)landmark.y, 15f, wrongPointPaint);
                        }
                    }

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
        btmLable = findViewById(R.id.comment);
        exName = findViewById(R.id.exName);
        guidelineView = findViewById(R.id.canvas);
        photo = findViewById(R.id.photo);

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ExerciseActivity.this, PhotoMode.class));
            }
        });

        btmLable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(testCompleted){
                    Toast.makeText(getApplicationContext(), "Restarting Exercise", Toast.LENGTH_SHORT).show();
                    currentPoseIndex = 0;
                    testCompleted = false;
                    isFrameBeingTested = false;
                    startAnalysis();
                }
            }
        });
    }

    private void runTest(){
        if(detector == null){
            AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder().setDetectorMode(AccuratePoseDetectorOptions.STREAM_MODE).build();
            detector = PoseDetection.getClient(options);
        }

        tempBitmap = previewView.getBitmap();
        if(previewView.getBitmap() == null){
            return;
        }

        isFrameBeingTested = true;
        detector.process(InputImage.fromBitmap(tempBitmap, 0)).addOnCompleteListener(new OnCompleteListener<Pose>() {

            @Override
            public void onComplete(@NonNull Task<Pose> task) {
                if(task.isSuccessful()){
                    Pose pose = task.getResult();
                    List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
                    if(landmarks.size() == 0){
                        isFrameBeingTested = false;
                        if(!canvasAlreadyClear)
                            loadGuidelines(tempBitmap, null, null);
                        return;
                    }

                    if(!testCompleted){
                        HashMap<String, Object> result = poseUtils.ComparePose(configPoses.get(currentPoseIndex).landmarkHashMap, new CustomPose(pose).landmarkHashMap, currentPoseIndex);
                        ArrayList<CustomPoseLandmark> wrongPoints = (ArrayList<CustomPoseLandmark>) result.get("wrongPoints");
                        Integer returnId = (Integer) result.get("id");

                        if(returnId == currentPoseIndex){
                            if(wrongPoints.size() == 1){
                                currentPoseIndex++;
                                btmLable.setText( currentPoseIndex + "/ " + configPoses.size() + "\n (" + Math.round(wrongPoints.get(0).x) + ")");
                                Log.d("debugg", "POSE MATCHED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                            }else{
                                if(Math.round(wrongPoints.get(wrongPoints.size() - 1).x) >= PoseUtils.MINIMUM_MATCH_PERCENT){
                                    currentPoseIndex++;
                                    Log.d("debugg", "POSE MATCHED!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                                }else
                                    Log.d("debugg", "POSE NOT MATCHED!!");

                                btmLable.setText( currentPoseIndex + "/ " + configPoses.size() + "\n (" + Math.round(wrongPoints.get(wrongPoints.size() - 1).x) + ")");
                            }
                            wrongPoints.remove(wrongPoints.size() - 1);
                        }

                        loadGuidelines(tempBitmap, pose, wrongPoints);
                    }else loadGuidelines(tempBitmap, pose, null);

                    isFrameBeingTested = false;
                    if(currentPoseIndex == configPoses.size()){
                        testCompleted = true;
                        btmLable.setText("EXERCISE COMPLETED! CLICK TO RESET");
                    }
                }else{
                    Log.e("debugg", "Error in test", task.getException());
                    loadGuidelines(tempBitmap, null, null);
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
                if(!isFrameBeingTested)
                    runTest();

                if(!testCompleted)
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
                    provider.bindToLifecycle(ExerciseActivity.this, CameraSelector.DEFAULT_BACK_CAMERA, preview);

                    startAnalysis();
                } catch (Exception e) {
                    Log.e("debugg", "Error Getting camera Provider", e);
                    Toast.makeText(getApplicationContext(), "Errror Loading Camera Provider, Restart App", Toast.LENGTH_SHORT).show();
                }
            }
        },ActivityCompat.getMainExecutor(ExerciseActivity.this));
    }

    private void checkPermissions(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(getApplicationContext(), "Camera Permission Request", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 455);
        }else{
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