package com.adarsh.mlkit;

import androidx.appcompat.app.AppCompatActivity;

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

import java.util.HashMap;

public class ConfigCreator extends AppCompatActivity {
    MediaMetadataRetriever retriever;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_creator);

        VideoView videoView =(VideoView)findViewById(R.id.videoView);

        //Creating MediaController
        MediaController mediaController= new MediaController(this);
        mediaController.setAnchorView(videoView);

        //specify the location of media file
        Uri uri=Uri.parse("https://file-examples-com.github.io/uploads/2017/04/file_example_MP4_480_1_5MG.mp4");
        getResources().openRawResourceFd(R.raw.samplevideo);

        //Setting MediaController and URI, then starting the videoView
        videoView.setMediaController(mediaController);
        int rawId = getResources().getIdentifier("samplevideo",  "raw", getPackageName());
        String path = "android.resource://" + getPackageName() + "/" + rawId;
        videoView.setVideoURI(Uri.parse(path));
        videoView.requestFocus();
        videoView.start();

        retriever = new MediaMetadataRetriever();
        retriever.setDataSource(path, new HashMap<>());

        ImageView preview = findViewById(R.id.videoPreview);
        findViewById(R.id.capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("debuggdebugg", "Hello " + videoView.getCurrentPosition());
                preview.setImageBitmap(retriever.getFrameAtTime(videoView.getCurrentPosition()));
            }
        });
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