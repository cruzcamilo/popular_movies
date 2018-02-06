package com.example.android.popularmovies;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.databinding.ActivityMovieDetailBinding;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import static com.example.android.popularmovies.MainActivity.API_KEY;
import static com.example.android.popularmovies.MainActivity.BASE_QUERY_URL;
import static com.example.android.popularmovies.MainActivity.LOG_TAG;
import static com.example.android.popularmovies.data.MovieContract.MovieEntry;

public class MovieDetail extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Review>>,
        TrailerAdapter.ListItemClickListener {

    private ActivityMovieDetailBinding mBinding;
    private String movieId;
    private static final int REVIEW_LOADER_ID = 2;
    private static final int TRAILER_LOADER_ID = 3;
    private static final int IMAGE_LOADER_ID = 4;
    private ReviewAdapter mAdapter;
    private TrailerAdapter tAdapter;
    private TextView reviewLabel, trailerLabel;
    Movie myMovie;
    Uri newUri;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_movie_detail);
        loadDetailsViews();

        reviewLabel = (TextView) findViewById(R.id.reviewsLabel);
        reviewLabel.setVisibility(View.GONE);
        trailerLabel = (TextView) findViewById(R.id.trailer_label);
        trailerLabel.setVisibility(View.GONE);

        // RecylerView to show user reviews
        RecyclerView reviewsRV = (RecyclerView) findViewById(R.id.rv_reviews);
        reviewsRV.setLayoutManager(new LinearLayoutManager(this));
        reviewsRV.setHasFixedSize(true);
        mAdapter = new ReviewAdapter(this, new ArrayList<Review>());
        reviewsRV.setAdapter(mAdapter);

        RecyclerView trailersRV = (RecyclerView) findViewById(R.id.rv_trailers);
        LinearLayoutManager horizontalLayoutManager
                = new LinearLayoutManager(MovieDetail.this, LinearLayoutManager.HORIZONTAL, false);
        trailersRV.setLayoutManager(horizontalLayoutManager);
        reviewsRV.setHasFixedSize(true);
        tAdapter = new TrailerAdapter(this, new ArrayList<Movie>(), this);
        trailersRV.setAdapter(tAdapter);

        LoaderManager loaderManager = getLoaderManager();
        loaderManager.initLoader(TRAILER_LOADER_ID, null, this);
        loaderManager.initLoader(REVIEW_LOADER_ID, null, this);
        loaderManager.initLoader(IMAGE_LOADER_ID, null, this);

        ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        String[] selectionArgs = {""};
        selectionArgs[0] = movieId;
        Cursor cursor = getContentResolver().query(MovieEntry.CONTENT_URI,
                new String[]{MovieEntry.COLUMN_MOVIE_ID},
                MovieEntry.COLUMN_MOVIE_ID + " = ? ",
                selectionArgs,
                null);
        if (cursor.moveToFirst()) {
            mBinding.favoriteBtn.setChecked(true);
        } else {
            mBinding.favoriteBtn.setChecked(false);
        }
        cursor.close();

        mBinding.favoriteBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    saveFavoriteMovie();
                } else {
                    removeFavoriteMovie();
                }
            }
        });
    }

    private void saveFavoriteMovie() {
        ContentValues values = new ContentValues();
        values.put(MovieEntry.COLUMN_MOVIE_ID, myMovie.getID());
        values.put(MovieEntry.COLUMN_MOVIE_TITLE, myMovie.getTitle());
        values.put(MovieEntry.COLUMN_MOVIE_POSTER_URL, myMovie.getPosterThumbnail());
        values.put(MovieEntry.COLUMN_MOVIE_OVERVIEW, myMovie.getOverview());
        values.put(MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE, myMovie.getVoteAverage());
        values.put(MovieEntry.COLUMN_MOVIE_VOTE_RELEASE_DATE, myMovie.getReleaseDate());

        newUri = getContentResolver().insert(MovieEntry.CONTENT_URI, values);
        if (newUri != null) {
            Toast.makeText(this, getString(R.string.add_favorite_movie_successful), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.add_favorite_movie_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeFavoriteMovie() {
        int rowsDeleted = getContentResolver().delete(MovieEntry.CONTENT_URI,
                MovieEntry.COLUMN_MOVIE_ID + "=?", new String[]{myMovie.getID()});
        if (rowsDeleted == 0) {
            Toast.makeText(this, getString(R.string.remove_favorite_movie_failed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.remove_favorite_movie_successful), Toast.LENGTH_SHORT).show();
        }
    }

    private void loadDetailsViews() {
        Intent intent = getIntent();
        myMovie = intent.getParcelableExtra("Movie");

        if (!myMovie.getTitle().equals("")) {
            mBinding.titleInfo.setText(myMovie.getTitle());
        } else {
            mBinding.titleInfo.setText(getString(R.string.no_title));
        }

        if (!myMovie.getReleaseDate().equals("")) {
            String releaseYear = myMovie.getReleaseDate().substring(0, 4);
            String text = "<small><font color='#808080'>(" + releaseYear
                    + ")" + "</font></small>";
            mBinding.titleInfo.append(" ");
            mBinding.titleInfo.append(Html.fromHtml(text));
            mBinding.releaseDateInfo.setText(myMovie.getReleaseDate());
        } else {
            mBinding.releaseDateInfo.setText(getString(R.string.no_info));
        }

        if (!myMovie.getVoteAverage().equals("")) {
            mBinding.ratingInfo.setText(myMovie.getVoteAverage().toString());
        } else {
            mBinding.ratingInfo.setText(getString(R.string.no_rating));
        }

        if (!myMovie.getVoteAverage().equals("")) {
            mBinding.synopsisInfo.setText(myMovie.getOverview());
        }

        movieId = myMovie.getID();

        Picasso.with(this)
                .load(myMovie.getPosterThumbnail())
                .error(R.drawable.noimageicon)
                .into(mBinding.posterDetail);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = NavUtils.getParentActivityIntent(this);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            NavUtils.navigateUpTo(this, intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader onCreateLoader(int id, Bundle bundle) {
        String URL = "";
        Uri baseUri = Uri.parse(BASE_QUERY_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendPath("movie");
        uriBuilder.appendPath(movieId);
        switch (id) {
            case 2:
                uriBuilder.appendPath("reviews");
                uriBuilder.appendQueryParameter("api_key", API_KEY);
                uriBuilder.appendQueryParameter("page", "1");
                Log.v(LOG_TAG + "URL", uriBuilder.toString());
                URL = uriBuilder.toString();
                break;
            case 3:
                uriBuilder.appendPath("videos");
                uriBuilder.appendQueryParameter("api_key", API_KEY);
                Log.v(LOG_TAG + "URL", uriBuilder.toString());
                URL = uriBuilder.toString();
                break;
            case 4:
                uriBuilder.appendPath("images");
                uriBuilder.appendQueryParameter("api_key", API_KEY);
                Log.v(LOG_TAG + "URL", uriBuilder.toString());
                URL = uriBuilder.toString();
        }
        return new DetailLoader(this, URL);
    }

    @Override
    public void onLoadFinished(Loader loader, List details) {
        int id = loader.getId();
        switch (id) {
            case 2:
                mAdapter.setData(details);

                if (details.isEmpty()) {
                    reviewLabel.setText(R.string.no_reviews);
                }
                reviewLabel.setVisibility(View.VISIBLE);
                break;
            case 3:
                tAdapter.setData(details);

                if (details.isEmpty()) {
                    trailerLabel.setText(R.string.no_trailers);
                }
                trailerLabel.setVisibility(View.VISIBLE);
                break;
            case 4:

                List <Movie> url = details;

                if (!url.isEmpty()) {
                    Movie dir = url.get(0);
                    Picasso.with(getBaseContext())
                            .load(dir.getImageURL())
                            .error(R.drawable.noimageicon)
                            .into(mBinding.movieDetailImage);
                    Log.v("Check 2", dir.getImageURL());
                } else {
                    mBinding.movieDetailImage.setVisibility(View.GONE);
                }
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Review>> loader) {
        int id = loader.getId();
        switch (id) {
            case 2:
                mAdapter.clear();
                break;
            case 3:
                tAdapter.clear();
                break;
            case 4:
                break;
        }
    }

    @Override
    public void onListTrailerClick(int clickedItemIndex) {
        String trailerURL = tAdapter.getItem(clickedItemIndex).getTrailerURL();
        Intent playVideo = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerURL));
        startActivity(playVideo);
    }
}