package net.pilpin.nanodegree_popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import net.pilpin.nanodegree_popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

public class FetchMoviesTask extends AsyncTask<Void, Void, Boolean> {
    private final String LOG_TAG = this.getClass().toString();
    private final Context mContext;

    public FetchMoviesTask(Context context){
        mContext = context;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean result = false;

        mContext.getContentResolver().delete(
                MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.FAVORITE + " = ?",
                new String[]{MovieContract.MovieEntry.NOT_FAVORITED});

        try{
            result = updateFavoriteMovies();
            String mostPopularMoviesJsonStr = fetchPopularMovies();
            result = result && getDataFromJsonMovieList(mostPopularMoviesJsonStr);
            String highestRatedMoviesJsonStr = fetchHighestRatedMovies();
            result = result && getDataFromJsonMovieList(highestRatedMoviesJsonStr);
        }catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(Boolean results) {
        super.onPostExecute(results);
        if(results != null && results) {
            Calendar cal = Calendar.getInstance();
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor prefEditor = preferences.edit();
            prefEditor.putLong(mContext.getResources().getString(R.string.pref_last_updated_key), cal.getTimeInMillis());
            prefEditor.apply();
        }
    }

    private boolean updateFavoriteMovies(){
        HttpURLConnection urlConnection = null;
        int count = 0;
        BufferedReader reader = null;

        Cursor c = mContext.getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                null,
                MovieContract.MovieEntry.FAVORITE + " = ?",
                new String[]{MovieContract.MovieEntry.FAVORITED},
                null);

        if(c == null){
            // NO FAVORITE MOVIES
            return true;
        }

        while (c.moveToNext()){
            String movieId = c.getString(c.getColumnIndex(MovieContract.MovieEntry.API_ID));

            try{
                final String THEMOVIEDB_BASE_URL = "http://api.themoviedb.org";
                final String APPID_PARAM = "api_key";

                Uri builtUri = Uri.parse(THEMOVIEDB_BASE_URL).buildUpon()
                        .appendPath("3")
                        .appendPath("movie")
                        .appendPath(movieId)
                        .appendQueryParameter(APPID_PARAM, BuildConfig.THEMOVIE_DB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());


                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if(inputStream == null){
                    return false;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while((line = reader.readLine()) != null){
                    buffer.append(line).append("\n");
                }

                if(buffer.length() == 0){
                    return false;
                }
                String movieJsonStr = buffer.toString();

                JSONObject movie = new JSONObject(movieJsonStr);
                count += mContext.getContentResolver().update(
                        MovieContract.MovieEntry.CONTENT_URI,
                        getDataFromJsonMovieDetails(movie),
                        MovieContract.MovieEntry.API_ID + " = ?",
                        new String[]{movieId});

            } catch (IOException | JSONException e) {
                Log.e(LOG_TAG, "Error " + e);
                return false;
            } finally {
                if(urlConnection != null){
                    urlConnection.disconnect();
                }

                if(reader != null){
                    try{
                        reader.close();
                    }catch (final IOException e){
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }
        }

        boolean result = count == c.getCount();
        c.close();

        return result;
    }

    private String fetchPopularMovies(){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String sortOrder = "desc";
        String popularity = "popularity" + "." + sortOrder;
        String result;

        try{
            final String THEMOVIEDB_BASE_URL = "http://api.themoviedb.org";
            final String SORT_PARAM = "sort_by";
            final String APPID_PARAM = "api_key";

            Uri builtUri = Uri.parse(THEMOVIEDB_BASE_URL).buildUpon()
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter(SORT_PARAM, popularity)
                    .appendQueryParameter(APPID_PARAM, BuildConfig.THEMOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null){
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line = reader.readLine()) != null){
                buffer.append(line).append("\n");
            }

            if(buffer.length() == 0){
                return null;
            }
            result = buffer.toString();
        }catch (IOException e){
            Log.e(LOG_TAG, "Error " + e);
            return null;
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }

            if(reader != null){
                try{
                    reader.close();
                }catch (final IOException e){
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return result;
    }

    private String fetchHighestRatedMovies(){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String sortOrder = "desc";
        String voteAverage = "vote_average" + "." + sortOrder;
        String result;

        try{
            final String THEMOVIEDB_BASE_URL = "http://api.themoviedb.org";
            final String SORT_PARAM = "sort_by";
            final String APIKEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(THEMOVIEDB_BASE_URL).buildUpon()
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter(SORT_PARAM, voteAverage)
                    .appendQueryParameter(APIKEY_PARAM, BuildConfig.THEMOVIE_DB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if(inputStream == null){
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while((line = reader.readLine()) != null){
                buffer.append(line).append("\n");
            }

            if(buffer.length() == 0){
                return null;
            }
            result = buffer.toString();
        }catch (IOException e){
            Log.e(LOG_TAG, "Error " + e);
            return null;
        }finally {
            if(urlConnection != null){
                urlConnection.disconnect();
            }

            if(reader != null){
                try{
                    reader.close();
                }catch (final IOException e){
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return result;
    }


    private boolean getDataFromJsonMovieList(String moviesJsonStr) throws JSONException {
        final String MOVIE_LIST = "results";

        if(moviesJsonStr == null){
            return false;
        }

        try{
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(MOVIE_LIST);

            Vector<ContentValues> cVVector = new Vector<>(moviesArray.length());

            for(int i = 0; i < moviesArray.length(); i++){
                JSONObject movie = moviesArray.getJSONObject(i);
                cVVector.add(getDataFromJsonMovieDetails(movie));
            }

            int insertCount = 0;
            if(cVVector.size() > 0){
                ContentValues[] valuesArray = new ContentValues[cVVector.size()];
                cVVector.toArray(valuesArray);
                insertCount = mContext.getContentResolver().bulkInsert(MovieContract.MovieEntry.CONTENT_URI, valuesArray);
            }

            return insertCount > 0;
        }catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }

    private ContentValues getDataFromJsonMovieDetails(JSONObject movie){
        final String MOVIE_ID = "id";
        final String MOVIE_TITLE = "title";
        final String MOVIE_POSTER = "poster_path";
        final String MOVIE_SYNOPSIS = "overview";
        final String MOVIE_RELEASE_DATE = "release_date";
        final String MOVIE_POPULARITY = "popularity";
        final String MOVIE_VOTE_AVERAGE = "vote_average";

        final String DATEFORMAT = "yyyy-MM-dd";

        ContentValues values = new ContentValues();

        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
            String poster = movie.getString(MOVIE_POSTER);
            String synopsis = movie.getString(MOVIE_SYNOPSIS);
            String release_date = movie.getString(MOVIE_RELEASE_DATE);

            values.put(MovieContract.MovieEntry.API_ID, movie.getLong(MOVIE_ID));
            values.put(MovieContract.MovieEntry.TITLE, movie.getString(MOVIE_TITLE));
            if(!synopsis.equals("null")) {
                values.put(MovieContract.MovieEntry.SYNOPSIS, synopsis);
            }
            if(!poster.equals("null")) {
                values.put(MovieContract.MovieEntry.POSTER, poster);
            }
            if(!(release_date.equals("null") || release_date.equals(""))){
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateFormat.parse(release_date));
                values.put(MovieContract.MovieEntry.RELEASE_DATE, cal.getTimeInMillis());
            }
            values.put(MovieContract.MovieEntry.POPULARITY, movie.getDouble(MOVIE_POPULARITY));
            values.put(MovieContract.MovieEntry.VOTE_AVERAGE, movie.getDouble(MOVIE_VOTE_AVERAGE));
        }catch (JSONException | ParseException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return values;
    }
}
