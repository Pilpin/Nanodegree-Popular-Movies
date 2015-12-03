package net.pilpin.nanodegree_popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MovieDBHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    static final String DATABASE_NAME = "movies.db";

    public MovieDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private final String SQL_CREATE_MOVIE_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + " (" +
            MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MovieContract.MovieEntry.API_ID + " INTEGER NOT NULL, " +
            MovieContract.MovieEntry.TITLE + " TEXT NOT NULL, " +
            MovieContract.MovieEntry.POSTER + " TEXT, " +
            MovieContract.MovieEntry.SYNOPSIS + " TEXT, " +
            MovieContract.MovieEntry.RELEASE_DATE + " INTEGER, " +
            MovieContract.MovieEntry.POPULARITY + " REAL NOT NULL," +
            MovieContract.MovieEntry.VOTE_AVERAGE + " REAL NOT NULL," +
            MovieContract.MovieEntry.FAVORITE + " INTEGER DEFAULT 0," +
            "UNIQUE (" + MovieContract.MovieEntry.API_ID + ") ON CONFLICT REPLACE);";

    private final String SQL_CREATE_TRAILERS_TABLE = "CREATE TABLE " + MovieContract.TrailerEntry.TABLE_NAME + " (" +
            MovieContract.TrailerEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MovieContract.TrailerEntry.KEY + " TEXT NOT NULL, " +
            MovieContract.TrailerEntry.NAME + " TEXT, " +
            MovieContract.TrailerEntry.SITE + " TEXT NOT NULL, " +
            MovieContract.TrailerEntry.SIZE + " TEXT, " +
            MovieContract.TrailerEntry.MOVIE_ID + " INTEGER NOT NULL);";

    private final String SQL_CREATE_REVIEWS_TABLE = "CREATE TABLE " + MovieContract.ReviewEntry.TABLE_NAME + " (" +
            MovieContract.ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            MovieContract.ReviewEntry.AUTHOR + " TEXT, " +
            MovieContract.ReviewEntry.CONTENT + " TEXT NOT NULL, " +
            MovieContract.ReviewEntry.URL + " TEXT, " +
            MovieContract.ReviewEntry.MOVIE_ID + " INTEGER NOT NULL);";

    private final String SQL_ALTER_MOVIE_TABLE_FAVORITE = "ALTER TABLE " + MovieContract.MovieEntry.TABLE_NAME +
            " ADD COLUMN " + MovieContract.MovieEntry.FAVORITE + " INTEGER DEFAULT 0;";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_MOVIE_TABLE);
        db.execSQL(SQL_CREATE_TRAILERS_TABLE);
        db.execSQL(SQL_CREATE_REVIEWS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion == 1 && newVersion == 2){
            db.execSQL(SQL_ALTER_MOVIE_TABLE_FAVORITE);
            db.execSQL(SQL_CREATE_REVIEWS_TABLE);
            db.execSQL(SQL_CREATE_TRAILERS_TABLE);
        }
    }
}
