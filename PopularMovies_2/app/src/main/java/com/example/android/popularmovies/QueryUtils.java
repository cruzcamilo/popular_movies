package com.example.android.popularmovies;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import static com.example.android.popularmovies.MainActivity.LOG_TAG;


public final class QueryUtils {

    private static final String BASE_IMAGE_URL = "https://image.tmdb.org/t/p/w780";
    public static List<Movie> fetchMovieData(String requestUrl) {
        String jsonResponse = getJson(requestUrl);
        List<Movie> movies = extractMovies(jsonResponse);
        return movies;
    }

    public static List<Review> fetchReviewData(String requestUrl) {
        String jsonResponse = getJson(requestUrl);
        List<Review> reviews = extractReviews(jsonResponse);
        return reviews;
    }

    public static List<Movie> fetchTrailersData(String requestUrl) {
        String jsonResponse = getJson(requestUrl);
        List<Movie> trailers = extractTrailers(jsonResponse);
        return trailers;
    }

    public static List<Movie> fetchImageData(String requestUrl) {
        String jsonResponse = getJson(requestUrl);
        List<Movie> images = extractImage(jsonResponse);
        return images;
    }

    public static String getJson(String requestUrl){
        URL url = createUrl(requestUrl);

        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }
        return jsonResponse;
    }

    private QueryUtils() {
    }

    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    public static ArrayList<Movie> extractMovies(String publicationJSON) {

        ArrayList<Movie> movies = new ArrayList<>();
        try {

            JSONObject listMoviesJSON = new JSONObject(publicationJSON);
            JSONArray results = listMoviesJSON.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject movie = results.getJSONObject(i);

                String title = movie.getString("title");
                String thumbnail = movie.getString("poster_path");
                String id = movie.getString("id");
                String overview = movie.getString("overview");
                Double voteAverage = movie.getDouble("vote_average");
                String releaseDate = movie.getString("release_date");

                if(!thumbnail.equals("null")){
                    thumbnail = BASE_IMAGE_URL + thumbnail;
                } else {
                    thumbnail = "no_poster";
                }
                movies.add(new Movie(title, thumbnail, overview, voteAverage, releaseDate, id));
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the movie JSON results", e);
        }
        return movies;
    }

    public static ArrayList<Review> extractReviews(String publicationJSON) {
        ArrayList<Review> reviews = new ArrayList<>();

        try {
            JSONObject listMoviesJSON = new JSONObject(publicationJSON);
            JSONArray results = listMoviesJSON.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject review = results.getJSONObject(i);

                String author = review.getString("author");
                String content = review.getString("content");

                reviews.add(new Review(author, content));
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the movie JSON results", e);
        }
        return reviews;
    }

    public static ArrayList<Movie> extractTrailers(String publicationJSON) {
        ArrayList<Movie> trailers = new ArrayList<>();

        try {
            JSONObject listMoviesJSON = new JSONObject(publicationJSON);
            JSONArray results = listMoviesJSON.getJSONArray("results");

            for (int i = 0; i < results.length(); i++) {
                JSONObject review = results.getJSONObject(i);

                // Build trailer URL
                String trailerID = review.getString("key");
                String trailerBaseURL = "https://www.youtube.com/watch";
                Uri trailerBaseUri = Uri.parse(trailerBaseURL);
                Uri.Builder trailerURLBuilder = trailerBaseUri.buildUpon();
                trailerURLBuilder.appendQueryParameter("v", trailerID);
                String trailerURL = trailerURLBuilder.build().toString();

                //Build trailer thumbnail
                String thumbnailBaseURL = "https://img.youtube.com/";
                Uri thumbnailBaseUri = Uri.parse(thumbnailBaseURL);
                Uri.Builder trailerThumbnailBuilder = thumbnailBaseUri.buildUpon();
                trailerThumbnailBuilder.appendPath("vi");
                trailerThumbnailBuilder.appendPath(trailerID);
                trailerThumbnailBuilder.appendPath("0.jpg");
                String thumbnailURL = trailerThumbnailBuilder.build().toString();

                trailers.add(new Movie(trailerURL, thumbnailURL));
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the movie JSON results", e);
        }
        return trailers;
    }

    public static ArrayList<Movie> extractImage(String publicationJSON) {
        ArrayList<Movie> images = new ArrayList<>();

        try {
            JSONObject listMoviesJSON = new JSONObject(publicationJSON);
            JSONArray backdrops = listMoviesJSON.getJSONArray("backdrops");

            for (int i = 0; i < backdrops.length(); i++) {
                JSONObject image = backdrops.getJSONObject(i);

                String imagePath = image.getString("file_path");

                if(!imagePath.equals("null")){
                    imagePath = BASE_IMAGE_URL + imagePath;
                } else {
                    imagePath = "no_image";
                }
                images.add(new Movie(imagePath));
            }

        } catch (JSONException e) {
            Log.e("QueryUtils", "Problem parsing the movie JSON results", e);
        }
        Log.v("Images check 1", images.toString());
        return images;
    }
}