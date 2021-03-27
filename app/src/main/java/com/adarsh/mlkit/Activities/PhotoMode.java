package com.adarsh.mlkit.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.adarsh.mlkit.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.PoseLandmark;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

public class PhotoMode extends AppCompatActivity {
    EditText etUrl, etResult;
    Button btnProcess;

    PoseDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_mode2);
        initViews();
    }

    private void initViews(){
        etUrl = findViewById(R.id.photoURL);
        etResult = findViewById(R.id.photoResult);
        btnProcess = findViewById(R.id.photoProcess);

        etResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PhotoMode.this, ConfigCreator.class));
            }
        });
        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etUrl.getText().toString().isEmpty()){
                    Picasso.get().load(etUrl.getText().toString()).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            runTest(bitmap);
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                            Log.e("debugg", "Error loading bitmap", e);
                            Toast.makeText(getApplicationContext(), "Error loading bitmap", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {}
                    });
                }else{
                    Toast.makeText(getApplicationContext(), "Please enter URL", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void runTest(Bitmap bitmap){
        if(detector == null){
            AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder().setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE).build();
            detector = PoseDetection.getClient(options);
        }

        detector.process(InputImage.fromBitmap(bitmap, 0)).addOnCompleteListener(new OnCompleteListener<Pose>() {

            @Override
            public void onComplete(@NonNull Task<Pose> task) {
                if(task.isSuccessful()){
                    Pose pose = task.getResult();
                    List<PoseLandmark> landmarks = pose.getAllPoseLandmarks();
                    Log.d("debugg", "Landmarks found : " + landmarks.size());
                    if(landmarks.size() == 0){
                        Toast.makeText(getApplicationContext(), "No Point detected in image, please try another image", Toast.LENGTH_LONG).show();
                        return;
                    }

                    JSONObject object = new JSONObject();
                    try{
                        for(PoseLandmark landmark : landmarks){
                            object.put("lm_" + landmark.getLandmarkType(), new JSONArray(new Double[]{(double) landmark.getPosition().x, (double) landmark.getPosition().y}));
                        }

                        etResult.setText(object.toString(2));
                    }catch (Exception e){
                        Log.e("debugg", "Error serializing pose", e);
                        Toast.makeText(getApplicationContext(), "Error Searializing", Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(getApplicationContext(), "Done!!", Toast.LENGTH_SHORT).show();
                }else{
//                    Toast.makeText(getApplicationContext(), "Error processing test", Toast.LENGTH_LONG).show();
                    Log.e("debugg", "Error in test", task.getException());
                    Toast.makeText(getApplicationContext(), "Error Loading Landmarks", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}