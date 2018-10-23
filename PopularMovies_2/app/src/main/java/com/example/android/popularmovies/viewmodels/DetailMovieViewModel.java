package com.example.android.popularmovies.viewmodels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.example.android.popularmovies.database.AppDatabase;
import com.example.android.popularmovies.model.Movie;

public class DetailMovieViewModel extends ViewModel {
    private LiveData<Movie> movie;

    public DetailMovieViewModel(AppDatabase database, int movieId) {
        movie = database.movieDao().loadMovieById(movieId);
        Log.v("FavoriteViewModel", "Retrieving from database");
    }

    public LiveData<Movie> getMovie() {
        return movie;
    }
}
