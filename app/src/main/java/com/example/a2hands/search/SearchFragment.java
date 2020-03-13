package com.example.a2hands.search;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.a2hands.R;


public class SearchFragment extends Fragment {

    private LinearLayout recentTitleContainer;

    public SearchFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        String query = getArguments().getString("search_query");

        recentTitleContainer = view.findViewById(R.id.recentTitleContainer);
        if(query.equals(""))
            recentTitleContainer.setVisibility(View.VISIBLE);
        else
            recentTitleContainer.setVisibility(View.GONE);


        //start the searchResultsFragment
        Fragment frg = new searchItemFragment();
        Bundle b = new Bundle();
        b.putString("search_query",query);
        frg.setArguments(b);
        final FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.searchResultsContainer,frg);
        ft.commit();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }



}
