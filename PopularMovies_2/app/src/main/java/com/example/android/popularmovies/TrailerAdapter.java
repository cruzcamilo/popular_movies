package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class TrailerAdapter extends RecyclerView.Adapter<TrailerAdapter.TrailerViewHolder> {

    Context context;
    private ArrayList<Movie> trailers;
    final private ListItemClickListener mOnClickListener;
    ImageView playImage;

    public TrailerAdapter(Context context, ArrayList<Movie> trailers, ListItemClickListener listener) {
        this.context = context;
        mOnClickListener = listener;
        this.trailers = trailers;
    }

    class TrailerViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        ImageView trailerThumbnail;

        public TrailerViewHolder(View itemView) {
            super(itemView);
            trailerThumbnail = (ImageView) itemView.findViewById(R.id.iv_trailer_thumbnail);
            playImage = (ImageView) itemView.findViewById(R.id.iv_play_image);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int clickedPosition = getAdapterPosition();
            mOnClickListener.onListTrailerClick(clickedPosition);
        }
    }

    @Override
    public TrailerViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int commentsDetail = R.layout.list_item_trailer;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(commentsDetail, viewGroup, false);
        TrailerViewHolder viewHolder = new TrailerViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TrailerViewHolder holder, int position) {
        Movie trailer = trailers.get(position);
        Log.v("Objeto", trailer.getTrailerThumbnail());

        if(!trailer.getTrailerThumbnail().equals("null")){
            Picasso.with(context)
                    .load(trailer.getTrailerThumbnail())
                    .error(R.drawable.noimageicon)
                    .into(holder.trailerThumbnail);
        } else {
            playImage.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return trailers.size();
    }

    public Movie getItem(int position) {
        return trailers.get(position);
    }


    public void setData(List<Movie> data) {

        if (null != data && !data.isEmpty()) {
            trailers.clear();
            trailers.addAll(data);
            notifyDataSetChanged();
        }
    }





    public void clear() {
        if (!trailers.equals(null)) {
            int size = getItemCount();
            if (size > 0) {
                trailers.clear();
            }
        }
    }

    public interface ListItemClickListener {
        void onListTrailerClick(int clickedItemIndex);

    }
}