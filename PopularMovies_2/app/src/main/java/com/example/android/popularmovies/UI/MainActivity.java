package com.example.android.popularmovies.UI;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.SettingsActivity;
import com.example.android.popularmovies.database.Movie;


public class MainActivity extends AppCompatActivity {
    public Boolean mTabletMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if(findViewById(R.id.container)!= null){
            mTabletMode = true;
            DetailFragment detailFragment = new DetailFragment();
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
        DetailFragment detailFragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putParcelable("Movie", movie);
        detailFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, detailFragment).commit();
    }

    public void replaceFavoriteMovieFragment(int movieId) {
        DetailFragment detailFragment = new DetailFragment();
        Bundle args = new Bundle();
        args.putInt("FavoriteMovieId", movieId);
        detailFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, detailFragment).commit();
    }
}