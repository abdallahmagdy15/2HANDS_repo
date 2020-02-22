package com.example.a2hands;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class SavedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
