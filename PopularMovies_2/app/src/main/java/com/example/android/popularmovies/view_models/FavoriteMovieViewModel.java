package com.example.android.popularmovies.view_models;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.popularmovies.database.AppDatabase;
import com.example.android.popularmovies.database.Movie;

public class FavoriteMovieViewModel extends ViewModel {
    private LiveData<Movie> movie;

    public FavoriteMovieViewModel(AppDatabase database, int movieId) {
        movie = database.movieDao().loadMovieById(movieId);
    }

    public LiveData<Movie> getMovie() {
        return movie;
    }
}
