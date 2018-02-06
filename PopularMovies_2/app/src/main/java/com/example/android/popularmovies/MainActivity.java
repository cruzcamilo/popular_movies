package com.example.android.popularmovies;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.popularmovies.data.MovieContract.MovieEntry;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<Movie>> {

    public static final String LOG_TAG = MainActivity.class.getName();
    private MovieAdapter mAdapter;
    private MovieCursorAdapter mCursorAdapter;
    private TextView mEmptyStateTextView;
    public static final String BASE_QUERY_URL = "https://api.themoviedb.org/3";
    // ENTER API KEY
    public static final String API_KEY = "";
    private static final int MOVIE_LOADER_ID = 1;
    private static final int FAVORITE_MOVIES_LOADER_ID = 2;
    private List<Movie> savedMovies;
    public View circle;
    String sortBy;
    SharedPreferences sharedPrefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        sortBy = sharedPrefs.getString(getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        GridView gridView = (GridView) findViewById(R.id.gridView);
        circle = findViewById(R.id.loading_spinner);

        if (sortBy.equals(getString(R.string.settings_order_by_favorites_value))) {
            mCursorAdapter = new MovieCursorAdapter(this, null);
            gridView.setAdapter(mCursorAdapter);
            FavoriteMoviesLoader favoriteMoviesLoader = new FavoriteMoviesLoader();
            favoriteMoviesLoader.useFavoriteLoadManager();
        } else {
            if (savedInstanceState != null) {
                circle.setVisibility(View.GONE);
                savedMovies = savedInstanceState.getParcelableArrayList("MOVIE_LIST");
                mAdapter = new MovieAdapter(this, new ArrayList<Movie>());
                gridView.setAdapter(mAdapter);
                mAdapter.addAll(savedMovies);
            } else {
                mEmptyStateTextView = (TextView) findViewById(R.id.empty);
                gridView.setEmptyView(mEmptyStateTextView);
                mAdapter = new MovieAdapter(this, new ArrayList<Movie>());
                gridView.setAdapter(mAdapter);

                if (isOnline()) {
                    LoaderManager loaderManager = getLoaderManager();
                    loaderManager.initLoader(MOVIE_LOADER_ID, null, this);
                } else {
                    View circle = findViewById(R.id.loading_spinner);
                    circle.setVisibility(View.GONE);
                    mEmptyStateTextView.setText(R.string.no_internet);
                }
            }

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    Movie selectedMovie = (Movie) adapterView.getItemAtPosition(position);

                    Intent detailActivity = new Intent(MainActivity.this, MovieDetail.class);
                    detailActivity.putExtra("Movie", selectedMovie);
                    startActivity(detailActivity);
                }
            });

        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private class FavoriteMoviesLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String[] projection = {
                    MovieEntry._ID,
                    MovieEntry.COLUMN_MOVIE_POSTER_URL};

            return new CursorLoader(MainActivity.this,
                    MovieEntry.CONTENT_URI,
                    projection,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
            mCursorAdapter.swapCursor(data);
            circle.setVisibility(View.GONE);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mCursorAdapter.swapCursor(null);
        }

        public void useFavoriteLoadManager() {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(FAVORITE_MOVIES_LOADER_ID, null, this);
        }
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle bundle) {

        Uri baseUri = Uri.parse(BASE_QUERY_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendPath("discover");
        uriBuilder.appendPath("movie");
        uriBuilder.appendQueryParameter("api_key", API_KEY);
        uriBuilder.appendQueryParameter("sort_by", sortBy);
        uriBuilder.appendQueryParameter("page", "1");
        Log.v(LOG_TAG + "URL", uriBuilder.toString());

        return new MovieLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        circle.setVisibility(View.GONE);
        mEmptyStateTextView.setText(R.string.empty_state);
        savedMovies = movies;
        mAdapter.clear();

        if (movies != null && !movies.isEmpty()) {
            mAdapter.addAll(savedMovies);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        mAdapter.clear();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("MOVIE_LIST", (ArrayList<? extends Parcelable>) savedMovies);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings, menu);
        return true;
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
}