package com.example.a2hands.homePackage;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.example.a2hands.Post;
import com.example.a2hands.ProfileActivity;
import com.example.a2hands.R;
import com.google.android.material.circularreveal.cardview.CircularRevealCardView;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeFragment extends Fragment implements PostFragment.OnListFragmentInteractionListener {

    private OnFragmentInteractionListener mListener;
    String selectedCat;
    Spinner catsSpinner;
    FragmentActivity homeContext;
    String[] catsStrings;
    CircleImageView profile_image ;
    public HomeFragment() {
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
        View view =  inflater.inflate(R.layout.fragment_home, container, false);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            selectedCat = bundle.getString("category", "General");
        }
        catsStrings = getResources().getStringArray(R.array.categories);
        catsSpinner = view.findViewById(R.id.catsSpinner);
        profile_image = view.findViewById(R.id.profile_image);
        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                startActivity(intent);
            }
        });
        loadPostsFrag();

        return view;
    }
    public void loadPostsFrag(){
        catsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Fragment frg = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putString("category", catsStrings[position]);
                frg.setArguments(bundle);
                final FragmentTransaction ft = homeContext.getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.postsFragment,frg).addToBackStack(null);
                ft.commit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        catsSpinner.setSelection(0);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        homeContext = (FragmentActivity)context;
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onListFragmentInteraction(Post item) {

    }
}
