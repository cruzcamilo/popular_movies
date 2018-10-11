package com.example.android.popularmovies;

import android.app.LoaderManager;
import android.content.ContentUris;
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
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.popularmovies.data.MovieContract.MovieEntry;

public class MainFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<List<Movie>> {

    public static final String LOG_TAG = MainFragment.class.getName();
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
    private Boolean mTablet;
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

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sortBy = sharedPrefs.getString(getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));

        GridView gridView = (GridView) rootView.findViewById(R.id.gridView);
        circle = rootView.findViewById(R.id.loading_spinner);
        favoriteView = false;

        if (sortBy.equals(getString(R.string.settings_order_by_favorites_value))) {
            favoriteView = true;
            mCursorAdapter = new MovieCursorAdapter(getActivity(), null);
            gridView.setAdapter(mCursorAdapter);
            FavoriteMoviesLoader favoriteMoviesLoader = new FavoriteMoviesLoader();
            favoriteMoviesLoader.useFavoriteLoadManager();
        } else {
            if(savedMovies != null && !savedMovies.isEmpty()) {
                circle.setVisibility(View.GONE);
                savedMovies = savedInstanceState.getParcelableArrayList("MOVIE_LIST");
                if (savedMovies != null) {
                    mAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());
                    gridView.setAdapter(mAdapter);
                    mAdapter.addAll(savedMovies);
                }
            } else {
                mEmptyStateTextView = (TextView) rootView.findViewById(R.id.empty);

                gridView.setEmptyView(mEmptyStateTextView);
                mAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());
                gridView.setAdapter(mAdapter);

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



        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                mTablet = ((MainActivity) getActivity()).isTablet();
                Intent detailActivity = new Intent(getActivity(), MovieDetailActivity.class);

                if (!favoriteView) {
                    Movie selectedMovie = mAdapter.getItem(position);
                    if (!mTablet) {
                        detailActivity.putExtra("Movie", selectedMovie);
                        startActivity(detailActivity);
                    } else {
                        ((MainActivity) getActivity()).replaceFragment(selectedMovie);
                    }
                } else {
                    Uri currentMovieUri = ContentUris.withAppendedId(MovieEntry.CONTENT_URI, id);
                    if (!mTablet) {
                        detailActivity.setData(currentMovieUri);
                        startActivity(detailActivity);
                    } else {
                        ((MainActivity) getActivity()).replaceFavoriteMovieFragment(currentMovieUri);
                    }
                }
            }
        });
        return rootView;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private class FavoriteMoviesLoader implements LoaderManager.LoaderCallbacks<Cursor> {

        @Override
        public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
            String[] projection = {
                    MovieEntry._ID,
                    MovieEntry.COLUMN_MOVIE_POSTER_URL};

            return new CursorLoader(getActivity(),
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
            LoaderManager loaderManager = getActivity().getLoaderManager();
            loaderManager.initLoader(FAVORITE_MOVIES_LOADER_ID, null, this);
        }
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
            mAdapter.addAll(savedMovies);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Movie>> loader) {
        mAdapter.clear();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("MOVIE_LIST", (ArrayList<? extends Parcelable>) savedMovies);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings, menu);
    }
}