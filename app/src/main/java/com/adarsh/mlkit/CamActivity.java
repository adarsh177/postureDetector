package com.adarsh.mlkit;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CamActivity extends AppCompatActivity {
    ImageCapture imageCapture;

    PreviewView previewView;
    Button btnCapture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        initViews();
        checkPermissions();
    }

    private void captureImage(){
//        File photo = new File(getFilesDir(), String.valueOf(System.currentTimeMillis()).concat(".png"));
//
//        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(photo).build();

        imageCapture.takePicture(ActivityCompat.getMainExecutor(CamActivity.this), new ImageCapture.OnImageCapturedCallback() {
            @Override
            @androidx.camera.core.ExperimentalGetImage
            public void onCaptureSuccess(@NonNull ImageProxy image) {
//                super.onCaptureSuccess(image);
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] array = new byte[buffer.capacity()];
                buffer.get(array);
                Bitmap bitmap = BitmapFactory.decodeByteArray(array, 0, array.length, null);

                ApplicationClass.capturedImage = bitmap;
                setResult(200, null);
                finish();
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                super.onError(exception);
                Log.e("debugg", "Error Capturing Image", exception);
                ApplicationClass.capturedImage = null;
                setResult(200, null);
                finish();
            }
        });
//        imageCapture.takePicture(outputFileOptions, ActivityCompat.getMainExecutor(this), new ImageCapture.OnImageSavedCallback() {
//            @Override
//            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
//                Toast.makeText(getApplicationContext(), "Picture Saved!", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onError(@NonNull ImageCaptureException exception) {
//                Log.e("debugg", "Error Saving Picture", exception);
//                Toast.makeText(getApplicationContext(), "Error Saving Picture", Toast.LENGTH_SHORT).show();
//            }
//        });
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
                    provider.bindToLifecycle(CamActivity.this, CameraSelector.DEFAULT_BACK_CAMERA, preview);
                    provider.bindToLifecycle(CamActivity.this, CameraSelector.DEFAULT_BACK_CAMERA, imageCapture);
                    Toast.makeText(getApplicationContext(), "Camera started", Toast.LENGTH_SHORT).show();

                } catch (Exception e) {
                    Log.e("debugg", "Error Getting camera Provider", e);
                    Toast.makeText(getApplicationContext(), "Errror Loading Camera Provider", Toast.LENGTH_SHORT).show();
                }
            }
        },ActivityCompat.getMainExecutor(CamActivity.this));
    }

    private void initViews(){
        btnCapture = findViewById(R.id.camera_capture_button);
        previewView = findViewById(R.id.viewFinder);

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });
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

