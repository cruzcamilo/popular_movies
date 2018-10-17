package com.example.android.popularmovies.database;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "movie")
public class Movie implements Parcelable {

    @PrimaryKey
    private Integer id;
    private String title;
    private String posterThumbnail;
    private String overview;
    private Double voteAverage;
    private String releaseDate;
    private String coverImageUri, posterImageUri;
    @Ignore
    private String trailerURL;
    @Ignore
    private String trailerThumbnail;
    @Ignore
    private String coverImageURL;

    @Ignore
    public Movie(String title, String posterThumbnail, String overview, Double voteAverage,
                 String releaseDate, Integer id) {
        this.title = title;
        this.posterThumbnail = posterThumbnail;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.id = id;
    }

    public Movie(Integer id, String title, String overview, Double voteAverage, String releaseDate, String coverImageUri, String posterImageUri) {
        this.id = id;
        this.title = title;
        this.overview = overview;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.coverImageUri = coverImageUri;
        this.posterImageUri = posterImageUri;
    }

    @Ignore
    public Movie(String trailerURL, String trailerThumbnail) {
        this.trailerURL = trailerURL;
        this.trailerThumbnail = trailerThumbnail;
    }

    public Movie(String coverImageURL) {
        this.coverImageURL = coverImageURL;
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

    public Integer getId() {
        return id;
    }

    public String getTrailerURL() {
        return trailerURL;
    }

    public String getTrailerThumbnail() {
        return trailerThumbnail;
    }

    public String getCoverImageURL() {
        return coverImageURL;
    }

    public String getCoverImageUri() {
        return coverImageUri;
    }

    public String getPosterImageUri() {
        return posterImageUri;
    }

    public void setPosterThumbnail(String posterThumbnail) {
        this.posterThumbnail = posterThumbnail;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", posterThumbnail='" + posterThumbnail + '\'' +
                ", overview='" + overview + '\'' +
                ", voteAverage=" + voteAverage +
                ", releaseDate='" + releaseDate + '\'' +
                ", coverImageUri='" + coverImageUri + '\'' +
                ", posterImageUri='" + posterImageUri + '\'' +
                ", trailerURL='" + trailerURL + '\'' +
                ", trailerThumbnail='" + trailerThumbnail + '\'' +
                ", coverImageURL='" + coverImageURL + '\'' +
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
        dest.writeInt(id);
    }

    public Movie(Parcel parcel){
        title = parcel.readString();
        posterThumbnail = parcel.readString();
        overview = parcel.readString();
        voteAverage = parcel.readDouble();
        releaseDate = parcel.readString();
        id = parcel.readInt();
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