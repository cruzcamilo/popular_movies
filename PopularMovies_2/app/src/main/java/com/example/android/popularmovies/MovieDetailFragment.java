package com.example.android.popularmovies;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
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
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
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
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmovies.databinding.FragmentMovieDetailBinding;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.android.popularmovies.MainFragment.API_KEY;
import static com.example.android.popularmovies.MainFragment.BASE_QUERY_URL;
import static com.example.android.popularmovies.MainFragment.LOG_TAG;
import static com.example.android.popularmovies.data.MovieContract.MovieEntry;

public class MovieDetailFragment extends Fragment implements
        android.support.v4.app.LoaderManager.LoaderCallbacks<List<Review>>,
        TrailerAdapter.ListItemClickListener {
    private FragmentMovieDetailBinding mBinding;
    private static final int REVIEW_LOADER_ID = 2;
    private static final int TRAILER_LOADER_ID = 3;
    private static final int IMAGE_LOADER_ID = 4;
    private static final int FAVORITE_MOVIE_LOADER_ID = 5;
    private static final int MY_PERMISSIONS_REQUEST = 22;
    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";
    private static final String IMAGES_DIR = "/my_images/";
    private ReviewAdapter mAdapter;
    private TrailerAdapter tAdapter;
    private TextView reviewLabel, trailerLabel;
    private Uri mCurrentMovieUri;
    private int targetW, targetH;
    private Uri pictureUri;
    private String movieId, coverImageUri, posterImageUri;
    private List<Movie> imagesUrl;

    private Movie myMovie;


    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // If true, we're in tablet mode
        if (getArguments() != null) {
            myMovie = (Movie) getArguments()
                    .getParcelable("Movie");
            mCurrentMovieUri = (Uri) getArguments()
                    .getParcelable("FavoriteMovie");
        }

        if (mCurrentMovieUri == null) {
            Intent intent = getActivity().getIntent();
            mCurrentMovieUri = intent.getData();
        }
        // If true, we're in favorite movie section.
        if (mCurrentMovieUri != null) {
            FavoriteMoviesLoader favoriteMoviesLoader = new FavoriteMoviesLoader();
            favoriteMoviesLoader.useFavoriteLoadManager();
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

            isFavoriteMovieCheck(movieId);
            mBinding.emptyDetails.setVisibility(View.GONE);
            mBinding.fragmentMovieLayout.setVisibility(View.VISIBLE);
        }

        //Enable HomeAsUpEnable only if we're not on a tablet.
        GridView movieGrid = (GridView) getActivity().findViewById(R.id.gridView);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null && movieGrid == null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mBinding.favoriteBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    if (mCurrentMovieUri == null) {
                        saveFavoriteMovie();
                    }
                } else {
                    removeFavoriteMovie();
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
                    MovieEntry.COLUMN_MOVIE_ID,
                    MovieEntry.COLUMN_MOVIE_TITLE,
                    MovieEntry.COLUMN_MOVIE_OVERVIEW,
                    MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE,
                    MovieEntry.COLUMN_MOVIE_RELEASE_DATE,
                    MovieEntry.COLUMN_MOVIE_COVER_IMAGE_URI,
                    MovieEntry.COLUMN_MOVIE_POSTER_URL
            };

            return new CursorLoader(getActivity(),
                    mCurrentMovieUri,
                    projection,
                    null,
                    null,
                    null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

            if (cursor == null || cursor.getCount() < 1) {
                return;
            }

            if (cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_ID);
                int titleColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_TITLE);
                int overviewColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_OVERVIEW);
                int voteAverageColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE);
                int releaseDateColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_RELEASE_DATE);
                int posterImageColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_POSTER_URL);
                int coverImageColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_COVER_IMAGE_URI);

                movieId = cursor.getString(idColumnIndex);
                String title = cursor.getString(titleColumnIndex);
                String overview = cursor.getString(overviewColumnIndex);
                String voteAverage = cursor.getString(voteAverageColumnIndex);
                String releaseDate = cursor.getString(releaseDateColumnIndex);
                coverImageUri = cursor.getString(coverImageColumnIndex);
                posterImageUri = cursor.getString(posterImageColumnIndex);
                Uri coverUri = Uri.parse(coverImageUri);
                Uri posterUri = Uri.parse(posterImageUri);

                WindowManager wm = (WindowManager) getView().getContext().getSystemService(Context.WINDOW_SERVICE);
                Display screen = wm.getDefaultDisplay();
                targetH = screen.getHeight();
                targetW = screen.getWidth();

                mBinding.movieDetailImage.setImageBitmap(getBitmapFromUri(coverUri));
                mBinding.posterDetail.setImageBitmap(getBitmapFromUri(posterUri));

                mBinding.titleInfo.setText(title);
                mBinding.synopsisInfo.setText(overview);
                mBinding.ratingInfo.setText(voteAverage);
                mBinding.releaseDateInfo.setText(releaseDate);
                mBinding.favoriteBtn.setChecked(true);
                mBinding.trailerInclude.trailerLabel.setVisibility(View.GONE);
                mBinding.trailerInclude.rvTrailers.setVisibility(View.GONE);
                mBinding.reviewLayout.reviewsLabel.setVisibility(View.GONE);
                mBinding.reviewLayout.rvReviews.setVisibility(View.GONE);
                mBinding.emptyDetails.setVisibility(View.GONE);
                mBinding.fragmentMovieLayout.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {
            mBinding.titleInfo.setText("");
            mBinding.synopsisInfo.setText("");
            mBinding.ratingInfo.setText("");
            mBinding.releaseDateInfo.setText("");
        }

        public void useFavoriteLoadManager() {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(FAVORITE_MOVIE_LOADER_ID, null, this);
        }
    }

    private void isFavoriteMovieCheck(String movieId) {
        String[] selectionArgs = {""};
        selectionArgs[0] = movieId;
        Cursor cursor = getActivity().getContentResolver().query(MovieEntry.CONTENT_URI,
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
    }

    private void saveFavoriteMovie() {
        ContentValues values = new ContentValues();
        values.put(MovieEntry.COLUMN_MOVIE_ID, myMovie.getID());
        values.put(MovieEntry.COLUMN_MOVIE_TITLE, myMovie.getTitle());
        values.put(MovieEntry.COLUMN_MOVIE_OVERVIEW, myMovie.getOverview());
        values.put(MovieEntry.COLUMN_MOVIE_VOTE_AVERAGE, myMovie.getVoteAverage());
        values.put(MovieEntry.COLUMN_MOVIE_RELEASE_DATE, myMovie.getReleaseDate());


        if (imagesUrl != null) {
            String coverImageUrl = imagesUrl.get(0).getImageURL();
            downloadFile(coverImageUrl);
            coverImageUri = pictureUri.toString();
        } else {
            coverImageUri = "null";
        }
        values.put(MovieEntry.COLUMN_MOVIE_COVER_IMAGE_URI, coverImageUri);

        if (myMovie.getPosterThumbnail() != null) {
            Toast.makeText(getActivity(), getString(R.string.adding_favorite_movie), Toast.LENGTH_SHORT).show();
            downloadFile(myMovie.getPosterThumbnail());
            posterImageUri = pictureUri.toString();

        } else {
            posterImageUri = "null";
        }
        values.put(MovieEntry.COLUMN_MOVIE_POSTER_URL, posterImageUri);

        Uri newUri = getActivity().getContentResolver().insert(MovieEntry.CONTENT_URI, values);
        if (newUri != null) {
            Toast.makeText(getActivity(), getString(R.string.add_favorite_movie_successful), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), getString(R.string.add_favorite_movie_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private void removeFavoriteMovie() {
        ContentResolver contentResolver = getActivity().getContentResolver();

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

        int rowsDeleted = getActivity().getContentResolver().delete(MovieEntry.CONTENT_URI,
                MovieEntry.COLUMN_MOVIE_ID + "=?", new String[]{movieId});

        if (rowsDeleted == 0) {
            Toast.makeText(getActivity(), getString(R.string.remove_favorite_movie_failed), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getActivity(), getString(R.string.remove_favorite_movie_successful), Toast.LENGTH_SHORT).show();
        }

        if (mCurrentMovieUri != null) {
            Intent mainActivity = new Intent(getActivity(), MainActivity.class);
            startActivity(mainActivity);
        }
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

        if (!myMovie.getVoteAverage().equals("")) {
            mBinding.ratingInfo.setText(myMovie.getVoteAverage().toString());
        } else {
            mBinding.ratingInfo.setText(getString(R.string.no_rating));
        }

        if (!myMovie.getOverview().equals("")) {
            mBinding.synopsisInfo.setText(myMovie.getOverview());
        }

        movieId = myMovie.getID();

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
        uriBuilder.appendPath(movieId);
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
            Movie dir = images.get(0);
            Picasso.with(getActivity())
                    .load(dir.getImageURL())
                    .resize(406, 228)
                    .error(R.drawable.noimageicon)
                    .into(mBinding.movieDetailImage);
        } else {
            mBinding.movieDetailImage.setVisibility(View.GONE);
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
}