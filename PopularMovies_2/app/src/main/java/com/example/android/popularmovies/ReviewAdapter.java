package com.example.android.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.CommentViewHolder> {

    Context context;
    private ArrayList<Review> reviews;

    public ReviewAdapter(Context context, ArrayList<Review> reviews) {
        this.context = context;
        this.reviews = reviews;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        TextView authorContent, commentContent;

        public CommentViewHolder(View itemView) {
            super(itemView);
            authorContent = (TextView) itemView.findViewById(R.id.comment_author);
            commentContent = (TextView) itemView.findViewById(R.id.comment_content);
        }
    }

    @Override
    public CommentViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int commentsDetail = R.layout.list_item_review;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(commentsDetail, viewGroup, false);
        CommentViewHolder viewHolder = new CommentViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(CommentViewHolder holder, int position) {
        Review review = reviews.get(position);
        String reviewContent = review.getContent().trim();
        holder.authorContent.setText(review.getAuthor());
        holder.commentContent.setText(reviewContent);
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }

    public void setData(List<Review> data) {
        if (null != data && !data.isEmpty()) {
            reviews.clear();
            reviews.addAll(data);
            notifyDataSetChanged();
        }
    }

    public void clear() {
        if (!reviews.equals(null)) {
            int size = getItemCount();
            if (size > 0) {
                reviews.clear();
            }
        }
    }
}