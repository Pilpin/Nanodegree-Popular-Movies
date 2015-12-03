package net.pilpin.nanodegree_popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

public class MovieProvider extends ContentProvider {
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDBHelper mOpenHelper;

    static final int MOVIES = 1000;
    static final int MOVIE = 1001;
    static final int TRAILERS = 1002;
    static final int REVIEWS = 1003;

    public MovieProvider() {
    }

    static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE, MOVIES);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE + "/#", MOVIE);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_TRAILER + "/#", TRAILERS);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEW + "/#", REVIEWS);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        switch(sUriMatcher.match(uri)){
            case MOVIES:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case TRAILERS:
                return MovieContract.TrailerEntry.CONTENT_TYPE;
            case REVIEWS:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown Uri: " + uri);
        }

    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor cursor = null;

        switch (sUriMatcher.match(uri)){
            case MOVIES:
                cursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case MOVIE:
                cursor = db.query(MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        MovieContract.MovieEntry._ID + " = ?",
                        new String[]{uri.getLastPathSegment()},
                        null,
                        null,
                        sortOrder);
                break;
            case TRAILERS:
                cursor = db.query(MovieContract.TrailerEntry.TABLE_NAME,
                        projection,
                        MovieContract.TrailerEntry.MOVIE_ID + " = ?",
                        new String[]{uri.getLastPathSegment()},
                        null,
                        null,
                        sortOrder);
                break;
            case REVIEWS:
                cursor = db.query(MovieContract.ReviewEntry.TABLE_NAME,
                        projection,
                        MovieContract.ReviewEntry.MOVIE_ID + " = ?",
                        new String[]{uri.getLastPathSegment()},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Query - Unknown Uri: " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        Uri returnUri;
        long id;
        switch (sUriMatcher.match(uri)){
            case MOVIES:
                id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if(id != -1){
                    returnUri = MovieContract.MovieEntry.buildMovieUri(id);
                }else{
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case TRAILERS:
                id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, values);
                if(id != -1){
                    returnUri = uri;
                }else{
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case REVIEWS:
                id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, values);
                if(id != -1){
                    returnUri = uri;
                }else{
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Insert - Unknown Uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)){
            case MOVIES:
                count = db.update(
                        MovieContract.MovieEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case MOVIE:
                count = db.update(
                        MovieContract.MovieEntry.TABLE_NAME,
                        values,
                        MovieContract.MovieEntry._ID + " = ?",
                        new String[]{uri.getLastPathSegment()});
                break;
            default:
                throw new UnsupportedOperationException("Update - Unknown Uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count;
        switch (sUriMatcher.match(uri)){
            case MOVIES:
                selection = selection == null ? "1" : selection;
                count = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            case MOVIE:
                count = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME,
                        MovieContract.MovieEntry._ID + " = ?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case TRAILERS:
                count = db.delete(
                        MovieContract.TrailerEntry.TABLE_NAME,
                        MovieContract.TrailerEntry.MOVIE_ID + " = ?",
                        new String[]{uri.getLastPathSegment()});
                break;
            case REVIEWS:
                count = db.delete(
                        MovieContract.ReviewEntry.TABLE_NAME,
                        MovieContract.ReviewEntry.MOVIE_ID + " = ?",
                        new String[]{uri.getLastPathSegment()});
                break;
            default:
                throw new UnsupportedOperationException("Delete - Unknown Uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = 0;

        switch (sUriMatcher.match(uri)) {
            case MOVIES:
                db.beginTransaction();
                for (ContentValues value : values) {
                    long id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                    if(id != -1){
                        count++;
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case TRAILERS:
                db.beginTransaction();
                for (ContentValues value : values) {
                    long id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, value);
                    if(id != -1){
                        count++;
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            case REVIEWS:
                db.beginTransaction();
                for (ContentValues value : values) {
                    long id = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, value);
                    if(id != -1){
                        count++;
                    }
                }
                db.setTransactionSuccessful();
                db.endTransaction();
                getContext().getContentResolver().notifyChange(uri, null);
                return count;
            default:
                return super.bulkInsert(uri, values);
        }
    }
}
