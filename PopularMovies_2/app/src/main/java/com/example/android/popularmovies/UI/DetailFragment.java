package com.example.android.popularmovies.ui;

import android.Manifest;
import android.app.DownloadManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.utils.AppExecutors;
import com.example.android.popularmovies.loader.DetailLoader;
import com.example.android.popularmovies.R;
import com.example.android.popularmovies.model.Review;
import com.example.android.popularmovies.adapter.ReviewAdapter;
import com.example.android.popularmovies.adapter.TrailerAdapter;
import com.example.android.popularmovies.database.AppDatabase;
import com.example.android.popularmovies.model.Movie;
import com.example.android.popularmovies.databinding.FragmentMovieDetailBinding;
import com.example.android.popularmovies.viewmodels.DetailMovieViewModel;
import com.example.android.popularmovies.viewmodels.DetailMovieViewModelFactory;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.android.popularmovies.ui.MainFragment.API_KEY;
import static com.example.android.popularmovies.ui.MainFragment.BASE_QUERY_URL;
import static com.example.android.popularmovies.ui.MainFragment.LOG_TAG;

public class DetailFragment extends Fragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<List<Review>>,
        TrailerAdapter.ListItemClickListener {

    private static final String SAVED_ID_KEY = "SAVED_MOVIE";
    private FragmentMovieDetailBinding mBinding;
    private static final int REVIEW_LOADER_ID = 2;
    private static final int TRAILER_LOADER_ID = 3;
    private static final int IMAGE_LOADER_ID = 4;
    private static final int MY_PERMISSIONS_REQUEST = 22;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String IMAGES_DIR = "/my_images/";
    private ReviewAdapter mAdapter;
    private TrailerAdapter tAdapter;
    private TextView reviewLabel, trailerLabel;
    private Uri pictureUri;
    private int targetW, targetH;
    private String coverImageUri, posterImageUri;
    private List<Movie> imagesUrl;
    private int movieId = 0;
    private Movie myMovie;
    private AppDatabase mDb;
    private Boolean isSortedByFavoriteMovie = false;
    private Movie movieOnDb;

    public DetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        mDb = AppDatabase.getInstance(getActivity().getApplicationContext());

        // If true, we're in tablet mode
        if (getArguments() != null) {
            myMovie = (Movie) getArguments()
                    .getParcelable("Movie");
            movieId = getArguments()
                  .getInt("FavoriteMovieId");
        }
        if (movieId  == 0){
            Intent intent = getActivity().getIntent();
            movieId = intent.getIntExtra("favoriteMovieId", 0);
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(SAVED_ID_KEY)) {
            movieId = savedInstanceState.getInt(SAVED_ID_KEY, 0);
        }

        if (movieId != 0) {
            setupDetailViewModel();
        }

        requestPermissions();
    }

    public void requestPermissions() {
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        MY_PERMISSIONS_REQUEST);
                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(
                inflater, R.layout.fragment_movie_detail, container, false);

        View rootView = mBinding.getRoot();

        if (!isOnline()) {
            mBinding.fragmentMovieLayout.setVisibility(View.INVISIBLE);
            mBinding.emptyDetails.setText(R.string.no_internet);
        }

        mBinding.fragmentMovieLayout.setVisibility(View.INVISIBLE);

        if (myMovie == null) {
            Intent intent = getActivity().getIntent();
            myMovie = intent.getParcelableExtra("Movie");
        }

        if (myMovie != null) {
            loadDetailsViews();
            isSortedByFavoriteMovie = false;
            reviewLabel = (TextView) rootView.findViewById(R.id.reviewsLabel);
            reviewLabel.setVisibility(View.GONE);
            trailerLabel = (TextView) rootView.findViewById(R.id.trailer_label);
            trailerLabel.setVisibility(View.GONE);

            // RecylerView to show user reviews
            RecyclerView reviewsRV = (RecyclerView) rootView.findViewById(R.id.rv_reviews);
            reviewsRV.setLayoutManager(new LinearLayoutManager(getActivity()));
            reviewsRV.setHasFixedSize(true);
            mAdapter = new ReviewAdapter(getActivity(), new ArrayList<Review>());
            reviewsRV.setAdapter(mAdapter);

            // RecylerView to show trailers
            RecyclerView trailersRV = (RecyclerView) rootView.findViewById(R.id.rv_trailers);
            LinearLayoutManager horizontalLayoutManager
                    = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
            trailersRV.setLayoutManager(horizontalLayoutManager);
            reviewsRV.setHasFixedSize(true);
            tAdapter = new TrailerAdapter(getActivity(), new ArrayList<Movie>(), this);
            trailersRV.setAdapter(tAdapter);

            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(TRAILER_LOADER_ID, null, this);
            loaderManager.initLoader(REVIEW_LOADER_ID, null, this);
            loaderManager.initLoader(IMAGE_LOADER_ID, null, this);
            mBinding.emptyDetails.setVisibility(View.GONE);
            mBinding.fragmentMovieLayout.setVisibility(View.VISIBLE);
            isFavoriteMovieCheck(myMovie.getId());
        }

        //Enable HomeAsUpEnable only if we're not on a tablet.
        RecyclerView movieRecycler = (RecyclerView) getActivity().findViewById(R.id.rv_movies);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null && movieRecycler == null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mBinding.favoriteBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (myMovie.getPosterImageUri() == null && movieOnDb==null) {
                        saveFavoriteMovie();
                    }
                } else {
                    removeFavoriteMovie();
                }
            }
        });

        return rootView;
    }

    private void isFavoriteMovieCheck (final int movieId) {
        DetailMovieViewModelFactory detailMovieViewModelFactory =
                new DetailMovieViewModelFactory(mDb, movieId);
        final DetailMovieViewModel detailMovieViewModel =
                ViewModelProviders.of(this, detailMovieViewModelFactory).get(DetailMovieViewModel.class);

        detailMovieViewModel.getMovie().observe(this, new Observer<Movie>() {
            @Override
            public void onChanged(@Nullable Movie movie) {
              detailMovieViewModel.getMovie().removeObserver(this);
                movieOnDb = movie;
                if(movie != null){
                    mBinding.favoriteBtn.setChecked(true);
                }
            }
        });
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    private void populateUI(Movie movie) {
        if (movie == null) {
            return;
        }
        Uri coverUri = Uri.parse(movie.getCoverImageUri());
        Uri posterUri = Uri.parse(movie.getPosterImageUri());

        WindowManager wm = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE);
        Display screen = wm.getDefaultDisplay();
        targetH = screen.getHeight();
        targetW = screen.getWidth();

        mBinding.movieDetailImage.setImageBitmap(getBitmapFromUri(coverUri));
        mBinding.posterDetail.setImageBitmap(getBitmapFromUri(posterUri));
        mBinding.titleInfo.setText(movie.getTitle());
        mBinding.synopsisInfo.setText(movie.getOverview());
        mBinding.ratingInfo.setText(movie.getVoteAverage().toString());
        mBinding.releaseDateInfo.setText(movie.getReleaseDate());
        mBinding.favoriteBtn.setChecked(true);
        mBinding.trailerInclude.trailerLabel.setVisibility(View.GONE);
        mBinding.trailerInclude.rvTrailers.setVisibility(View.GONE);
        mBinding.reviewLayout.reviewsLabel.setVisibility(View.GONE);
        mBinding.reviewLayout.rvReviews.setVisibility(View.GONE);
        mBinding.emptyDetails.setVisibility(View.GONE);
        mBinding.fragmentMovieLayout.setVisibility(View.VISIBLE);
    }

    private void saveFavoriteMovie() {
        Integer id = myMovie.getId();
        String title = myMovie.getTitle();
        String overview = myMovie.getOverview();
        Double voteAverage = myMovie.getVoteAverage();
        String releaseDate = myMovie.getReleaseDate();

        if (imagesUrl != null) {
            String coverImageUrl = imagesUrl.get(0).getCoverImageURL();
            downloadFile(coverImageUrl);
            coverImageUri = pictureUri.toString();
        } else {
            coverImageUri = "null";
        }

        if (myMovie.getPosterThumbnail() != null) {
            Toast.makeText(getActivity(), getString(R.string.adding_favorite_movie), Toast.LENGTH_SHORT).show();
            downloadFile(myMovie.getPosterThumbnail());
            posterImageUri = pictureUri.toString();

        } else {
            posterImageUri = "null";
        }

        final Movie movie = new Movie(id, title, overview, voteAverage, releaseDate, coverImageUri, posterImageUri);
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.movieDao().insertMovie(movie);
                LiveData<Movie> savedMovie = mDb.movieDao().loadMovieById(movie.getId());
                if (savedMovie != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(),
                                    getString(R.string.add_favorite_movie_successful), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    private void removeFavoriteMovie() {
        if (movieOnDb != null){
            myMovie = movieOnDb;
        }
        ContentResolver contentResolver = getActivity().getContentResolver();
        coverImageUri = myMovie.getCoverImageUri();
        posterImageUri = myMovie.getPosterImageUri();

        if (coverImageUri != null || posterImageUri != null) {
            Toast.makeText(getActivity(), getString(R.string.remove_favorite_movie), Toast.LENGTH_SHORT).show();
            if (coverImageUri != null) {
                String coverImagePath = Uri.parse(coverImageUri).getPath();
                contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.ImageColumns.DATA + "=?", new String[]{coverImagePath});
            }

            if (posterImageUri != null) {
                String thumbnailImagePath = Uri.parse(posterImageUri).getPath();
                contentResolver.delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        MediaStore.Images.ImageColumns.DATA + "=?", new String[]{thumbnailImagePath});
            }
        }

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                mDb.movieDao().deleteMovie(myMovie);
                final LiveData<Movie> deletedMovie = mDb.movieDao().loadMovieById(myMovie.getId());

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (deletedMovie.getValue() != null) {
                            Toast.makeText(getActivity(),
                                    getString(R.string.remove_favorite_movie_failed), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(),
                                    getString(R.string.remove_favorite_movie_successful), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                if (isSortedByFavoriteMovie){
                    Intent mainActivity = new Intent(getActivity(), MainActivity.class);
                    startActivity(mainActivity);
                }
            }
        });
    }

    private void downloadFile(String imageUrl) {
        try {
            File f = createImageFile();
            pictureUri = Uri.fromFile(f);
            f.delete();
            DownloadManager mgr = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);

            Uri downloadUri = Uri.parse(imageUrl);
            DownloadManager.Request request = new DownloadManager.Request(
                    downloadUri);

            request.setAllowedNetworkTypes(
                    DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                    .setDestinationUri(pictureUri);

            mgr.enqueue(request);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    private File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            storageDir = new File(Environment.getExternalStorageDirectory()
                    + IMAGES_DIR
                    + getString(R.string.app_name));

            Log.d(LOG_TAG, "Dir: " + storageDir);

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d(LOG_TAG, "failed to create directory");
                        return null;
                    }
                }
            }
        } else {
            Log.d(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }
        return storageDir;
    }

    // Method to get the bitmap resized
    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        Log.d("Target size", String.valueOf(targetW) + " " + String.valueOf(targetW));

        InputStream input = null;
        Bitmap errorImage = BitmapFactory.decodeResource(getResources(), R.drawable.noimageicon);
        try {
            input = getActivity().getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);


            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            Log.d("Photo size", String.valueOf(photoW) + " " + String.valueOf(photoH));

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            input = getActivity().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);

            return errorImage;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return errorImage;
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
                return errorImage;
            }
        }
    }

    private void loadDetailsViews() {
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

        if (myMovie.getVoteAverage() >= 0) {
            mBinding.ratingInfo.setText(String.valueOf(myMovie.getVoteAverage()));
        } else {
            mBinding.ratingInfo.setText(getString(R.string.no_rating));
        }

        if (!myMovie.getOverview().equals("")) {
            mBinding.synopsisInfo.setText(myMovie.getOverview());
        }

        movieId = myMovie.getId();

        Picasso.with(getActivity())
                .load(myMovie.getPosterThumbnail())
                .error(R.drawable.noimageicon)
                .into(mBinding.posterDetail);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent intent = NavUtils.getParentActivityIntent(getActivity());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            NavUtils.navigateUpTo(getActivity(), intent);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<Review>> onCreateLoader(int id, Bundle bundle) {
        String URL = "";
        Uri baseUri = Uri.parse(BASE_QUERY_URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();
        uriBuilder.appendPath("movie");
        uriBuilder.appendPath(String.valueOf(movieId));
        switch (id) {
            case 2:
                uriBuilder.appendPath("reviews");
                uriBuilder.appendQueryParameter("api_key", API_KEY);
                uriBuilder.appendQueryParameter("page", "1");
                URL = uriBuilder.toString();
                break;
            case 3:
                uriBuilder.appendPath("videos");
                uriBuilder.appendQueryParameter("api_key", API_KEY);
                URL = uriBuilder.toString();
                break;
            case 4:
                uriBuilder.appendPath("images");
                uriBuilder.appendQueryParameter("api_key", API_KEY);
                URL = uriBuilder.toString();
        }
        return new DetailLoader(getContext(), URL);
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
                imagesUrl = details;
                displayCoverImage(imagesUrl);
                break;
        }
    }

    private void displayCoverImage(List<Movie> images) {
        if (!images.isEmpty()) {
            Movie firstCoverImage = images.get(0);
            Picasso.with(getActivity())
                    .load(firstCoverImage.getCoverImageURL())
                    .resize(406, 228)
                    .error(R.drawable.noimageicon)
                    .into(mBinding.movieDetailImage);
        } else {
            mBinding.movieDetailImage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Review>> loader) {
        int id = loader.getId();
        switch (id) {
            case 2:
                mAdapter.clear();
                break;
            case 3:
                tAdapter.clear();
                break;
            case 4:
                imagesUrl.clear();
                break;
        }
    }

    @Override
    public void onListTrailerClick(int clickedItemIndex) {
        String trailerURL = tAdapter.getItem(clickedItemIndex).getTrailerURL();
        Intent playVideo = new Intent(Intent.ACTION_VIEW, Uri.parse(trailerURL));
        startActivity(playVideo);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SAVED_ID_KEY, movieId);
    }

    private void setupDetailViewModel() {
        isSortedByFavoriteMovie = true;

        DetailMovieViewModelFactory detailMovieViewModelFactory =
                new DetailMovieViewModelFactory(mDb, movieId);
        final DetailMovieViewModel detailMovieViewModel =
                ViewModelProviders.of(this, detailMovieViewModelFactory).get(DetailMovieViewModel.class);
        detailMovieViewModel.getMovie().observe(this, new Observer<Movie>() {
            @Override
            public void onChanged(@Nullable Movie movie) {
                detailMovieViewModel.getMovie().removeObserver(this);
                myMovie = movie;
                populateUI(movie);
            }
        });
    }
}