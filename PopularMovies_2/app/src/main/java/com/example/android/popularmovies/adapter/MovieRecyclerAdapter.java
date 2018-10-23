package com.example.android.popularmovies.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmovies.R;
import com.example.android.popularmovies.model.Movie;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MovieRecyclerAdapter extends RecyclerView.Adapter<MovieRecyclerAdapter.MovieViewHolder> {

    private Context context;
    private ArrayList<Movie> movies;
    final private ListItemClickListener mOnClickListener;

    public MovieRecyclerAdapter(Context context, ArrayList<Movie> movies,
                                ListItemClickListener listener) {
        this.context = context;
        this.movies = movies;
        mOnClickListener = listener;
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView moviePoster;

        public MovieViewHolder(View itemView) {
            super(itemView);
            moviePoster = (ImageView) itemView.findViewById(R.id.movie_poster);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onMovieClick(clickedPosition);
        }
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int commentsDetail = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(commentsDetail, viewGroup, false);
        MovieViewHolder viewHolder = new MovieViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder holder, int position) {
        Movie currentMovie = movies.get(position);

        if (currentMovie.getCoverImageUri() == null) {
            Picasso.with(context)
                    .load(currentMovie.getPosterThumbnail())
                    .resize(540, 810)
                    .error(R.drawable.noimageicon)
                    .into(holder.moviePoster);
        } else {
            Picasso.with(context)
                    .load(currentMovie.getPosterImageUri())
                    .resize(540, 810)
                    .error(R.drawable.noimageicon)
                    .into(holder.moviePoster);
        }

    }

    @Override
    public int getItemCount() {
        return movies.size();
    }

    public void setData(List<Movie> data) {
        if (data != null && !data.isEmpty()) {
            movies.clear();
            movies.addAll(data);
            notifyDataSetChanged();
        }
    }

    public void clear() {
        int size = getItemCount();
        if (size > 0) {
            movies.clear();
        }
    }

    public Movie getItem(int position) {
        return movies.get(position);
    }

    public interface ListItemClickListener {
        void onMovieClick(int clickedItemIndex);
    }
}