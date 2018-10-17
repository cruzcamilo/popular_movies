package com.example.android.popularmovies;

import android.content.Context;
import android.content.AsyncTaskLoader;
import android.util.Log;

import com.example.android.popularmovies.database.Movie;

import java.util.List;

public class MovieLoader extends AsyncTaskLoader<List<Movie>> {

    private static final String LOG_TAG = MovieLoader.class.getName();

    /** Query URL */
    private String mUrl;
    private List<Movie> savedMovies;

    public MovieLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {

        if(savedMovies!=null){
            deliverResult(savedMovies);
            Log.v(LOG_TAG, " using cache");
        } else if (takeContentChanged() || savedMovies == null) {
            forceLoad();
        }

    }

    @Override
    public List<Movie> loadInBackground() {
        List<Movie> movies = QueryUtils.fetchMovieData(mUrl);
        return movies;
    }

    @Override
    public void deliverResult(List<Movie> movies) {
        savedMovies = movies;
        super.deliverResult(savedMovies);
    }
}