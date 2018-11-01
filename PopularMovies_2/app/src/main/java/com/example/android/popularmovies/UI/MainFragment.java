package com.example.android.popularmovies.ui;

import android.app.LoaderManager;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.popularmovies.BuildConfig;
import com.example.android.popularmovies.loader.MovieLoader;
import com.example.android.popularmovies.adapter.MovieRecyclerAdapter;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.viewmodels.MainViewModel;

import java.util.ArrayList;
import java.util.List;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;
import static android.content.res.Configuration.ORIENTATION_SQUARE;

public class MainFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<Movie>>,MovieRecyclerAdapter.ListItemClickListener{

    public static final String LOG_TAG = MainFragment.class.getName();
    private static final String MOVIE_LIST_KEY = "MOVIE_LIST";
    private MovieRecyclerAdapter mAdapter;
    private TextView mEmptyStateTextView;
    public static final String BASE_QUERY_URL = "https://api.themoviedb.org/3";
    // ENTER API KEY
    public static final String API_KEY = BuildConfig.ApiKey;
    private static final int MOVIE_LOADER_ID = 1;
    private List<Movie> savedMovies;
    public View circle;
    private String sortBy;
    private Boolean favoriteView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sortBy = sharedPrefs.getString(getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        int currentOrientation = getScreenOrientation();
        int mNoOfColumns;

        RecyclerView recyclerView = rootView.findViewById(R.id.rv_movies);
        if (!isTablet || currentOrientation == Configuration.ORIENTATION_LANDSCAPE){
            mNoOfColumns = 2;
        } else {
            mNoOfColumns = 1;
        }
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), mNoOfColumns);
        recyclerView.setLayoutManager(gridLayoutManager);
        mAdapter = new MovieRecyclerAdapter(getActivity(), new ArrayList<Movie>(), this);
        recyclerView.setAdapter(mAdapter);

        circle = rootView.findViewById(R.id.loading_spinner);
        circle.setVisibility(View.VISIBLE);
        favoriteView = false;
        mEmptyStateTextView = rootView.findViewById(R.id.empty);

        if (sortBy.equals(getString(R.string.settings_order_by_favorites_value))) {
            favoriteView = true;
            setupViewModel();
        } else {
            if (savedMovies != null && !savedMovies.isEmpty()) {
                circle.setVisibility(View.GONE);
                savedMovies = savedInstanceState.getParcelableArrayList(MOVIE_LIST_KEY);
                if (savedMovies != null) {
                    mAdapter.setData(savedMovies);
                }
            } else {

                if (isOnline()) {
                    LoaderManager loaderManager = getActivity().getLoaderManager();
                    loaderManager.initLoader(MOVIE_LOADER_ID, null, this);
                } else {
                    View circle = rootView.findViewById(R.id.loading_spinner);
                    circle.setVisibility(View.GONE);
                    mEmptyStateTextView.setText(R.string.no_internet);
                }
            }
        }
        return rootView;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private void setupViewModel() {
        MainViewModel viewModel = ViewModelProviders.of(this).get(MainViewModel.class);
        viewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                circle.setVisibility(View.GONE);
                if (movies != null && movies.size() == 0) {
                    mEmptyStateTextView.setVisibility(View.VISIBLE);
                    mEmptyStateTextView.setText(R.string.empty_favorite_movies);
                }
                mAdapter.setData(movies);
            }
        });
    }

    @Override
    public Loader<List<Movie>> onCreateLoader(int id, Bundle bundle) {
        Uri baseUri = Uri.parse(BASE_QUERY_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendPath("movie");
        uriBuilder.appendPath(sortBy);
        uriBuilder.appendQueryParameter("api_key", API_KEY);
        uriBuilder.appendQueryParameter("page", "1");
        Log.d(LOG_TAG + "URL", uriBuilder.toString());

        return new MovieLoader(getActivity(), uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Movie>> loader, List<Movie> movies) {
        circle.setVisibility(View.GONE);
        mEmptyStateTextView.setText(R.string.empty_state);
        savedMovies = movies;
        mAdapter.clear();

        if (movies != null && !movies.isEmpty()) {
            mAdapter.setData(savedMovies);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        mAdapter.clear();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_LIST_KEY, (ArrayList<? extends Parcelable>) savedMovies);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings, menu);
    }

    @Override
    public void onMovieClick(int clickedItemIndex) {
        Boolean mTablet = ((MainActivity) getActivity()).isTablet();
        Intent detailActivity = new Intent(getActivity(), DetailActivity.class);
        Movie selectedMovie = mAdapter.getItem(clickedItemIndex);
        if (!favoriteView) {
            if (!mTablet) {
                detailActivity.putExtra("Movie", selectedMovie);
                startActivity(detailActivity);
            } else {
                ((MainActivity) getActivity()).replaceFragment(selectedMovie);
            }
        } else {
            if (!mTablet) {
                detailActivity.putExtra("favoriteMovieId", selectedMovie.getId());
                startActivity(detailActivity);
            } else {
                ((MainActivity) getActivity()).replaceFavoriteMovieFragment(selectedMovie.getId());
            }
        }
    }

    //Taken from https://stackoverflow.com/questions/33575731
    public int getScreenOrientation(){
        Display getOrient = getActivity().getWindowManager().getDefaultDisplay();
        int orientation;
        if(getOrient.getWidth()==getOrient.getHeight()){
            orientation = ORIENTATION_SQUARE;
        } else{
            if(getOrient.getWidth() < getOrient.getHeight()){
                orientation = ORIENTATION_PORTRAIT;
            }else {
                orientation = ORIENTATION_LANDSCAPE;
            }
        }
        return orientation;
    }
}