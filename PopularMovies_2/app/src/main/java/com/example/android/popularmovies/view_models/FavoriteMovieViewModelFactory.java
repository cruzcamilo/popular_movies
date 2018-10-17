package com.example.android.popularmovies.view_models;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.android.popularmovies.database.AppDatabase;

public class FavoriteMovieViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private final AppDatabase mDb;
    private final int mMovieId;

    public FavoriteMovieViewModelFactory(AppDatabase mDb, int mMovieId) {
        this.mDb = mDb;
        this.mMovieId = mMovieId;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        //noinspection unchecked
        return (T) new FavoriteMovieViewModel(mDb, mMovieId);
    }
}
