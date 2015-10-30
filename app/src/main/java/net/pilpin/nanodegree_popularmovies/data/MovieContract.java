package net.pilpin.nanodegree_popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

public class MovieContract {
    public static final String CONTENT_AUTHORITY = "net.pilpin.nanodegree_popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIE = "movie";

    public static final class MovieEntry implements BaseColumns {
        public static final String TABLE_NAME = "movies";

        public static final String API_ID = "api_id";
        public static final String TITLE = "title";
        public static final String POSTER = "poster";
        public static final String SYNOPSIS = "synopsis";
        public static final String POPULARITY = "popularity";
        public static final String VOTE_AVERAGE = "vote_average";
        public static final String RELEASE_DATE = "release_date";

        public static final String ORDER_BY_POPULARITY = POPULARITY + " DESC LIMIT 20";
        public static final String ORDER_BY_VOTE_AVERAGE = VOTE_AVERAGE + " DESC LIMIT 20";

        public static final Uri CONTENT_URI =  BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE +
                "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
