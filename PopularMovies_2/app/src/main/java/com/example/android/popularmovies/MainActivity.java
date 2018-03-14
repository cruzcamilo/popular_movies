package com.example.android.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity {
    public Boolean mTabletMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(findViewById(R.id.container)!= null){
            mTabletMode = true;
            MovieDetailFragment detailFragment = new MovieDetailFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, detailFragment).commit();
        }
    }

    public boolean isTablet() {
        return mTabletMode;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();

            if (id == R.id.moviesSettings) {
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            }
        return super.onOptionsItemSelected(item);
    }

    public void replaceFragment(Movie movie) {
        MovieDetailFragment detailFragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("Movie", movie);
        detailFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, detailFragment).commit();
    }

    public void replaceFavoriteMovieFragment(Uri movieUri) {
        MovieDetailFragment detailFragment = new MovieDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("FavoriteMovie", movieUri);
        detailFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, detailFragment).commit();
    }
}