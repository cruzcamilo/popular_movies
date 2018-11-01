package com.example.android.popularmovies.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.model.Movie;

public class DetailActivity extends AppCompatActivity {

    DetailFragment detailFragment = new DetailFragment();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        if (savedInstanceState != null) {
            detailFragment = (DetailFragment) getSupportFragmentManager().getFragment(savedInstanceState, "detailSavedFragment");
            getSupportFragmentManager().beginTransaction().replace(R.id.container, detailFragment).commit();
        }

        if (getIntent() != null) {
            Intent movieIntent = getIntent();
            Movie movie = movieIntent.getParcelableExtra("Movie");
            Bundle bundle = new Bundle();
            bundle.putParcelable("Movie", movie);
            detailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, detailFragment).commit();
        }
    }

    //Taken from https://stackoverflow.com/questions/15313598
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "detailSavedFragment", detailFragment);
    }
}