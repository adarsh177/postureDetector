package com.adarsh.mlkit;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.adarsh.mlkit.Activities.ConfigCreator;
import com.adarsh.mlkit.Activities.ExerciseActivity;
import com.adarsh.mlkit.Activities.PhotoMode;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Button btnCreateConfig, btnSingleImage, btnSelectFile;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
        loadInteractions();
    }

    private void loadInteractions(){
        btnCreateConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ConfigCreator.class));
            }
        });

        btnSingleImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, PhotoMode.class));
            }
        });

        btnSelectFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("*/*");
                startActivityForResult(intent, 55);
            }
        });
    }

    private void initViews(){
        btnCreateConfig = findViewById(R.id.btn_create_config);
        btnSingleImage = findViewById(R.id.btn_get_single_pose);
        btnSelectFile = findViewById(R.id.btn_select_file);
    }

    private void handleUri(Uri uri){
        try{
            InputStream stream = getContentResolver().openInputStream(uri);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            while(stream.available() > 0){
                os.write(stream.read());
            }
            String str = new String(os.toByteArray());
            Log.d("debugg", "File We Got: " + str);
            Intent intent = new Intent(MainActivity.this, ExerciseActivity.class);
            intent.putExtra("config", str);
            startActivity(intent);
        }catch (Exception e){
            Log.e("debugg", "Error Opening File", e);
            Toast.makeText(getApplicationContext(), "Error Opening File", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 55){
            if(resultCode == RESULT_OK && data != null){
                handleUri(data.getData());
            }else{
                Log.d("debugg", "No File Selected");
                Toast.makeText(getApplicationContext(), "No File Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
