package com.example.a2hands.LocationSearch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.a2hands.R;

import java.util.Locale;

public class SearchLocation extends AppCompatActivity {

    SearchView search;
    ListView list;
    String arr[];
    ArrayAdapter<String> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        //search Location
        search = findViewById(R.id.search);
        list = findViewById(R.id.list);
        //check local language for device
        if (Locale.getDefault().getDisplayLanguage().equals("العربية"))
            adapter = new ArrayAdapter<>(this,  android.R.layout.simple_expandable_list_item_1, getResources().getStringArray(R.array.Governorates_ar));
        else
            adapter = new ArrayAdapter<>(this,android.R.layout.simple_expandable_list_item_1, getResources().getStringArray(R.array.Governorates_en));

        list.setAdapter(adapter);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                adapter.notifyDataSetChanged();
                return false;
            }
        });
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent = new Intent();
                String city = adapterView.getItemAtPosition(i).toString();
                intent.putExtra("governate",city);
                setResult(Activity.RESULT_OK,intent);
                finish();
                //Toast.makeText(SearchLocation.this,city, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
