package com.example.a2hands;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


import android.os.Bundle;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Spinner;


import com.example.a2hands.dummy.DummyContent;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;


public class MainActivity extends AppCompatActivity implements PostFragment.OnListFragmentInteractionListener ,  BottomNavigationView.OnNavigationItemSelectedListener {


    private BottomNavigationView nav;
    private int navItemId;
    Spinner cats;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//will hide the title
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);
        nav = findViewById(R.id.bottom_navigation);
        nav.setOnNavigationItemSelectedListener(this);
        BadgeDrawable badge = nav.getOrCreateBadge(nav.getMenu().getItem(3).getItemId());
        badge.setVisible(true);
        cats = findViewById(R.id.spinner);
        navigateHome();
    }
    public void navigateHome(){
        cats.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Fragment frg = new PostFragment();
                Bundle bundle = new Bundle();
                String[] cats = getResources().getStringArray(R.array.categories);
                bundle.putString("category", cats[position]);
                frg.setArguments(bundle);
                final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container,frg).addToBackStack(null);
                ft.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        cats.setSelection(0);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int pos =0;
        navItemId = item.getItemId();
        for (int i = 0; i < nav.getMenu().size(); i++) {
            if(navItemId == nav.getMenu().getItem(i).getItemId()){
                pos = i;
                break;
            }
        }

        switch (pos) {
            case 0: navigateHome();
                break;
            case 1: ;
                break;
            case 2: ;
                break;
            case 3: ;
                break;
            case 4: ;
                break;
        }
        return true;
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }


}
