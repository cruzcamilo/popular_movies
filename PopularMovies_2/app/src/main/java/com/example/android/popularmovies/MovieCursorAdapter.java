package com.example.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import static com.example.android.popularmovies.data.MovieContract.MovieEntry;

public class MovieCursorAdapter extends CursorAdapter {

    public MovieCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item_movie, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ImageView moviePoster = (ImageView) view.findViewById(R.id.movie_poster);
        int posterColumnIndex = cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_POSTER_URL);
        String posterURL = cursor.getString(posterColumnIndex);

        Picasso.with(context)
                .load(posterURL)
                .resize(450, 675)
                .error(R.drawable.noimageicon)
                .into(moviePoster);
    }
}