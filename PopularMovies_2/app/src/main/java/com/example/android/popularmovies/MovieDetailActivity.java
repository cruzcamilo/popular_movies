package com.example.android.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MovieDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        if (getIntent() != null) {
            Intent movieIntent = getIntent();
            Movie movie = movieIntent.getParcelableExtra("Movie");
            Bundle bundle = new Bundle();
            bundle.putParcelable("Movie", movie);
            MovieDetailFragment detailFragment = new MovieDetailFragment();
            detailFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.container, detailFragment).commit();
        }


    }
}