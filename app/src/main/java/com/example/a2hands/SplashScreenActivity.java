package com.example.a2hands;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import gr.net.maroulis.library.EasySplashScreen;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EasySplashScreen config = new EasySplashScreen(SplashScreenActivity.this)
                .withFullScreen()
                .withTargetActivity(LoginActivity.class)
                .withSplashTimeOut(2000)
                .withBackgroundColor(getResources().getColor(R.color.colorPrimary))
                .withLogo(R.mipmap.ic_launcher_2hands_2);

        View easySplashScreen = config.create();
        setContentView(easySplashScreen);
    }
}
