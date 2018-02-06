package com.example.android.popularmovies;

import android.os.Parcel;
import android.os.Parcelable;

public class Movie implements Parcelable {

    private String title;
    private String posterThumbnail;
    private String overview;
    private Double voteAverage;
    private String releaseDate;
    private String ID;
    private String trailerURL;
    private String trailerThumbnail;
    private String imageURL;

    public Movie(String title, String posterThumbnail, String overview, Double voteAverage,
                 String releaseDate, String ID) {
        this.title = title;
        this.posterThumbnail = posterThumbnail;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.ID = ID;
    }

    public Movie(String trailerURL, String trailerThumbnail) {
        this.trailerURL = trailerURL;
        this.trailerThumbnail = trailerThumbnail;
    }

    public Movie(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterThumbnail() {
        return posterThumbnail;
    }

    public String getOverview() {
        return overview;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getID() {
        return ID;
    }

    public String getTrailerURL() {
        return trailerURL;
    }

    public String getTrailerThumbnail() {
        return trailerThumbnail;
    }

    public String getImageURL() {
        return imageURL;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "imageURL='" + imageURL + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeString(title);
        dest.writeString(posterThumbnail);
        dest.writeString(overview);
        dest.writeDouble(voteAverage);
        dest.writeString(releaseDate);
        dest.writeString(ID);
    }

    public Movie(Parcel parcel){
        title = parcel.readString();
        posterThumbnail = parcel.readString();
        overview = parcel.readString();
        voteAverage = parcel.readDouble();
        releaseDate = parcel.readString();
        ID = parcel.readString();
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {

        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}