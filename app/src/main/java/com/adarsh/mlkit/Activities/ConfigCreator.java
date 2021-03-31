package com.adarsh.mlkit.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorSpace;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.adarsh.mlkit.Models.CustomPose;
import com.adarsh.mlkit.Models.CustomPoseLandmark;
import com.adarsh.mlkit.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.pose.Pose;
import com.google.mlkit.vision.pose.PoseDetection;
import com.google.mlkit.vision.pose.PoseDetector;
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;

public class ConfigCreator extends AppCompatActivity {
    MediaMetadataRetriever retriever;
    Button btnRewind, btnForward, btnPlay, btnCapture, btnCreate;
    TextView tvTime;
    RecyclerView recyclerView;
    EditText exName;
    VideoView videoView;
    FramesAdapter adapter;
    ProgressDialog progressDialog;
    PoseDetector detector;

    // vars while processing
    boolean anyErrorWhileProcessing = false;
    int totalPosesProcessed = 0;
    JSONArray finalPosesArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_creator);

        initViews();

        loadVideo();
        loadInteractions();
    }

    void loadInteractions(){
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoView.isPlaying()){
                    videoView.pause();
                    btnPlay.setText("Play");
                }else{
                    videoView.start();
                    btnPlay.setText("Pause");
                }
            }
        });

        btnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoView.getCurrentPosition() + 5000 > videoView.getDuration()){
                    videoView.seekTo(videoView.getDuration());
                }else{
                    videoView.seekTo(videoView.getCurrentPosition() + 5000);
                }
            }
        });

        btnRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(videoView.getCurrentPosition() - 5000 < 0){
                    videoView.seekTo(0);
                }else{
                    videoView.seekTo(videoView.getCurrentPosition() - 5000);
                }
            }
        });

        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.addFrame(videoView.getCurrentPosition());
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter.getFrames().size() == 0){
                    Toast.makeText(getApplicationContext(), "Please Mark Frames first!", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(exName.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please Enter exercise name", Toast.LENGTH_SHORT).show();
                    exName.requestFocus();
                    return;
                }

                CreateConfig();
            }
        });

        Handler handler = new Handler(getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                tvTime.setText((videoView.getCurrentPosition() / 1000) + "/" + (videoView.getDuration() / 1000) + " sec");
                handler.postDelayed(this,1000);
            }
        }, 1000);
    }

    void initViews(){
        videoView = findViewById(R.id.videoView);
        recyclerView = findViewById(R.id.recycler_captures);
        btnCapture = findViewById(R.id.btn_capture);
        btnPlay = findViewById(R.id.btn_play);
        btnRewind = findViewById(R.id.btn_rew);
        btnForward = findViewById(R.id.btn_forw);
        btnCreate = findViewById(R.id.btn_create_config);
        tvTime = findViewById(R.id.tv_time);
        exName = findViewById(R.id.et_exercise_name);

        retriever = new MediaMetadataRetriever();
        adapter = new FramesAdapter(this, new FrameClickListener() {
            @Override
            public void Clicked(Integer time) {
                videoView.seekTo(time);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    void loadVideo(){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        startActivityForResult(intent, 255);
    }

    void CreateConfig(){
        showProgressDialog(true);

        if(detector == null){
            AccuratePoseDetectorOptions options = new AccuratePoseDetectorOptions.Builder().setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE).build();
            detector = PoseDetection.getClient(options);
        }

        anyErrorWhileProcessing = false;
        totalPosesProcessed = 0;
        finalPosesArray = new JSONArray();

        for(Integer frameTime : adapter.getFrames()){
            Bitmap bmp = ARGBBitmap(retriever.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST));
            detector
                    .process(InputImage.fromBitmap(bmp, 0))
                    .addOnCompleteListener(new OnCompleteListener<Pose>() {
                        @Override
                        public void onComplete(@NonNull Task<Pose> task) {
                            if(task.isSuccessful()){
                                if(task.getResult().getAllPoseLandmarks().size() == 0){
                                    Log.d("debugg", "NO POSE DETECTED IN THIS BITMAP");
                                    anyErrorWhileProcessing = true;
                                    return;
                                }
                                CustomPose pose = new CustomPose(task.getResult());
                                finalPosesArray.put(new JSONObject(pose.landmarkHashMap));
                            }else{
                                Log.e("debugg", "Error GETTING Pose from bitmap", task.getException());
                                anyErrorWhileProcessing = true;
                            }
                            totalPosesProcessed++;

                            if(totalPosesProcessed == adapter.getFrames().size()){
                                // all done
                                if(anyErrorWhileProcessing){
                                    Toast.makeText(getApplicationContext(), "There was some error, please try again!", Toast.LENGTH_SHORT).show();
                                    Log.e("debugg", "There was some error, please try again!");
                                }else{
                                    SaveToFile();
                                }
                                showProgressDialog(false);
                            }
                        }
                    });
        }
    }

    private Bitmap ARGBBitmap(Bitmap img) {
        return img.copy(Bitmap.Config.ARGB_8888,true);
    }

    void SaveToFile(){
        File downloadsFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String fileName = exName.getText().toString() + "_" + System.currentTimeMillis() + ".json";

        // data
        String data = "";
        JSONObject object = new JSONObject();
        try{
            object.put("name", exName.getText().toString());
            object.put("poses", finalPosesArray);
            data = object.toString(2);
        }catch (Exception e){
            Log.e("debugg", "JSON Error while saving", e);
        }

        if(data.isEmpty()){
            Toast.makeText(getApplicationContext(), "There was some error, please try again!", Toast.LENGTH_SHORT).show();
            Log.e("debugg", "Empty Data while saving!");
            return;
        }

        File file = new File(downloadsFolder, fileName);
        try{
            file.createNewFile();
            OutputStream os = new FileOutputStream(file);
            os.write(data.getBytes());
            os.flush();
            os.close();
            Toast.makeText(getApplicationContext(), "File Saved Successfully under Downloads folder as : " + fileName, Toast.LENGTH_LONG).show();
            Log.d("debugg", "File Saved Successfully under Downloads folder as : " + fileName);
        }catch (Exception e){
            Log.e("debugg", "IO Error While saving FILE!", e);
            Toast.makeText(getApplicationContext(), "There was some error, please try again!", Toast.LENGTH_SHORT).show();
        }

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

                btnPlay.setText("Pause");
                tvTime.setText("0/" + (videoView.getDuration() / 1000) + " sec");

                retriever.setDataSource(getApplicationContext(), fileUri);
            }else{
                Log.d("debugg", "No File Selected");
                Toast.makeText(getApplicationContext(), "No File Selected", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void showProgressDialog(boolean show){
        if(progressDialog == null){
            progressDialog = new ProgressDialog(ConfigCreator.this, 0);
            progressDialog.setTitle("Creating Config File");
            progressDialog.setMessage("Please wait while we create config file and save it under your download's folder");
        }

        if(show){
            progressDialog.show();
        }else{
            progressDialog.dismiss();
        }
    }
}

class FramesAdapter extends RecyclerView.Adapter<FramesAdapter.ViewHolder>{
    Context mContext;
    ArrayList<Integer> frames;
    FrameClickListener listener;

    public FramesAdapter(Context mContext, FrameClickListener listener){
        this.frames = new ArrayList<>();
        this.listener = listener;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.config_entry, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.seq.setText("#" + (position + 1));
        holder.time.setText("Time(millisec) : " + frames.get(position));
        holder.rem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frames.remove(position);
                notifyDataSetChanged();
            }
        });

        holder.time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.Clicked(frames.get(position));
            }
        });
    }

    public void addFrame(Integer time){
        frames.add(time);
        frames.sort(new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1 - o2;
            }
        });
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return frames.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView time, seq, rem;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            time = itemView.findViewById(R.id.frame_time);
            seq = itemView.findViewById(R.id.frame_seq);
            rem = itemView.findViewById(R.id.frame_remove);
        }
    }

    public ArrayList<Integer> getFrames(){
        return frames;
    }
}

interface FrameClickListener{
    void Clicked(Integer time);
}