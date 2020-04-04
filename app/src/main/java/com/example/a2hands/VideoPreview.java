package com.example.a2hands;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ActionBar;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;
import com.universalvideoview.UniversalMediaController;
import com.universalvideoview.UniversalVideoView;

public class VideoPreview extends AppCompatActivity {
    private UniversalVideoView video;
    private UniversalMediaController controller;
    private FrameLayout container;
    SlidrInterface slidr;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);
        video = findViewById(R.id.videoPreview_video);
        controller = findViewById(R.id.videoPreview_controller);
        container = findViewById(R.id.videoPreview_container);

        String videoPath = getIntent().getStringExtra("VIDEO_PATH");
        video.setVideoURI(Uri.parse(videoPath));

        video.setMediaController(controller);

        video.setVideoViewCallback(new UniversalVideoView.VideoViewCallback() {
            @Override
            public void onScaleChange(boolean isFullscreen) {
                if (isFullscreen) {
                    ViewGroup.LayoutParams layoutParams = video.getLayoutParams();
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    video.setLayoutParams(layoutParams);
                    //GONE the unconcerned views to leave room for video and controller
                    //mBottomLayout.setVisibility(View.GONE);
                    //hide status bar
                    if (Build.VERSION.SDK_INT < 16) {
                        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                                WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    }
                    else {
                        View decorView = getWindow().getDecorView();
                        // Hide the status bar.
                        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
                        decorView.setSystemUiVisibility(uiOptions);
                    }
                } else {
                    ViewGroup.LayoutParams layoutParams = video.getLayoutParams();
                    layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    int width = container.getWidth();
                    int cachedHeight = (int) (width * 405f / 720f);
                    layoutParams.height = cachedHeight;
                    video.setLayoutParams(layoutParams);
                    //mBottomLayout.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onPause(MediaPlayer mediaPlayer) { // Video pause
            }

            @Override
            public void onStart(MediaPlayer mediaPlayer) { // Video start/resume to play
            }

            @Override
            public void onBufferingStart(MediaPlayer mediaPlayer) {// steam start loading
            }

            @Override
            public void onBufferingEnd(MediaPlayer mediaPlayer) {// steam end loading
            }

        });

        //Custom Animation To Activity
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.TOP)
                .sensitivity(1f)
                .scrimColor(Color.BLACK)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0.0f)
                .velocityThreshold(1000)
                .distanceThreshold(0.25f)
                .edge(true|false)
                .edgeSize(0.18f) // The % of the screen that counts as the edge, default 18%
                .build();
        slidr = Slidr.attach(this, config);
        //starting when Intent begins
        overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_up);
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_bottom);
    }
}