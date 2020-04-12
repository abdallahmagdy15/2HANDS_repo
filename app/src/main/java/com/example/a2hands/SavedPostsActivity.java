package com.example.a2hands;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.a2hands.home.posts.PostsFragment;


public class SavedPostsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ChangeLocale.loadLocale(getBaseContext());
        setContentView(R.layout.activity_saved);
        Toolbar toolbar = findViewById(R.id.savedPostsToolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.savedPosts));

        Fragment frg = new PostsFragment();
        Bundle b = new Bundle();
        b.putString("FOR","SAVED_POSTS");
        frg.setArguments(b);
        FragmentTransaction tr = this.getSupportFragmentManager().beginTransaction()
                .replace(R.id.savedPostsContainer,frg,null).addToBackStack(null);
        tr.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


}
