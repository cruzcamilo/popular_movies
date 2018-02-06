package com.example.android.popularmovies;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class DetailLoader extends AsyncTaskLoader<List<Review>> {

    /** Query URL */
    private String mUrl;

    public DetailLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List loadInBackground() {
        int id = getId();
        List details = new ArrayList();
        switch (id){
            case 2:
                details = QueryUtils.fetchReviewData(mUrl);
                break;
            case 3:
                details = QueryUtils.fetchTrailersData(mUrl);
                break;
            case 4:
                details = QueryUtils.fetchImageData(mUrl);
                break;
        }
        return details;
    }

    @Override
    public void deliverResult(List<Review> reviews) {
        super.deliverResult(reviews);
    }
}