package com.adarsh.mlkit.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.adarsh.mlkit.R;

import java.util.HashMap;

public class ConfigCreator extends AppCompatActivity {
    MediaMetadataRetriever retriever;
    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_creator);

        videoView =(VideoView)findViewById(R.id.videoView);

        loadVideo();

        ImageView preview = findViewById(R.id.videoPreview);
        findViewById(R.id.capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("debuggdebugg", "Hello " + videoView.getCurrentPosition());
                preview.setImageBitmap(retriever.getFrameAtTime(videoView.getCurrentPosition()));
            }
        });
    }

    void loadVideo(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        startActivityForResult(intent, 255);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 255){
            if(resultCode == RESULT_OK && data != null){
                Uri fileUri = data.getData();
                videoView.setVideoURI(fileUri);
                videoView.requestFocus();
                videoView.start();

                retriever = new MediaMetadataRetriever();
                retriever.setDataSource(getApplicationContext(), fileUri);
            }else{
                Log.d("debuggdebugg", "No File Selected");
                Toast.makeText(getApplicationContext(), "No File Selected", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //    private fun getScreenShot(view: View): Bitmap {
//        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
//        val canvas = Canvas(returnedBitmap)
//        val bgDrawable = view.background
//        if (bgDrawable != null) bgDrawable.draw(canvas)
//        else canvas.drawColor(Color.WHITE)
//        view.draw(canvas)
//        return returnedBitmap
//    }

    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth() , v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }
}