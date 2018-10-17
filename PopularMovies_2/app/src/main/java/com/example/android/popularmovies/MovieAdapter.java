package com.example.android.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.example.android.popularmovies.database.Movie;
import com.squareup.picasso.Picasso;

import java.util.List;

public class MovieAdapter extends ArrayAdapter<Movie> {

    public MovieAdapter(Context context, List<Movie> movies) {
        super(context, 0, movies);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item_movie, parent, false);
        }

        Movie currentMovie = getItem(position);
        ImageView moviePoster = (ImageView) listItemView.findViewById(R.id.movie_poster);

        if (currentMovie.getCoverImageUri() == null) {
            Picasso.with(getContext())
                    .load(currentMovie.getPosterThumbnail())
                    .resize(540, 810)
                    .error(R.drawable.noimageicon)
                    .into(moviePoster);
        } else {
            Picasso.with(getContext())
                    .load(currentMovie.getPosterImageUri())
                    .resize(540, 810)
                    .error(R.drawable.noimageicon)
                    .into(moviePoster);
        }

        return listItemView;
    }
}