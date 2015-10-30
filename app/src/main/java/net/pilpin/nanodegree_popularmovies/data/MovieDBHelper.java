package net.pilpin.nanodegree_popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "movies.db";

    public MovieDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.MovieEntry.API_ID + " INTEGER NOT NULL, " +
                MovieContract.MovieEntry.TITLE + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.POSTER + " TEXT, " +
                MovieContract.MovieEntry.SYNOPSIS + " TEXT, " +
                MovieContract.MovieEntry.RELEASE_DATE + " INTEGER, " +
                MovieContract.MovieEntry.POPULARITY + " REAL, " +
                MovieContract.MovieEntry.VOTE_AVERAGE + " REAL, " +
                "UNIQUE (" + MovieContract.MovieEntry.API_ID + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_MOVIE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
