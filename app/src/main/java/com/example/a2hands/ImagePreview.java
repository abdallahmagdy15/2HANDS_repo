package com.example.a2hands;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;

import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrInterface;
import com.r0adkll.slidr.model.SlidrPosition;
import com.squareup.picasso.Picasso;


public class ImagePreview extends AppCompatActivity {

    String myUid;

    SlidrInterface slidr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_image_preview);

        Toolbar toolbar = findViewById(R.id.imagePreviewToolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        PhotoView image = findViewById(R.id.imagePreview);

        myUid = FirebaseAuth.getInstance().getUid();

        Uri imageUri = Uri.parse(getIntent().getStringExtra("IMAGE_PATH"));
        Picasso.with(ImagePreview.this).load(imageUri).into(image);

        //Custom Animation To Activity
        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.VERTICAL)
                .sensitivity(1f)
                .scrimColor(Color.BLACK)
                .scrimStartAlpha(0.8f)
                .scrimEndAlpha(0f)
                .velocityThreshold(2400)
                .distanceThreshold(0.25f)
                .edge(true)
                .edgeSize(0.5f) // The % of the screen that counts as the edge, default 18%
                .build();

        slidr = Slidr.attach(this, config);

        //starting when Intent begins
        overridePendingTransition(R.anim.slide_in_bottom,R.anim.slide_out_up);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image_preview_options_menu, menu);
        return true;
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_up, R.anim.slide_out_bottom);
    }

    @Override
    protected void onResume() {
        UserStatus.updateOnlineStatus(true, myUid);
        super.onResume();
    }

    @Override
    protected void onStop()
    {
        if(UserStatus.isAppIsInBackground(getApplicationContext())){
            UserStatus.updateOnlineStatus(false, myUid);
        }
        super.onStop();
    }


}
