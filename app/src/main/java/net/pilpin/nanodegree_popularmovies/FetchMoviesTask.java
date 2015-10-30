package net.pilpin.nanodegree_popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
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
        Boolean result = true;

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String mostPopularMoviesJsonStr = null;
        String highestRatedMoviesJsonStr = null;

        String sortOrder = "desc";
        String popularity = "popularity" + "." + sortOrder;
        String voteAverage = "vote_average" + "." + sortOrder;

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
                buffer.append(line + "\n");
            }

            if(buffer.length() == 0){
                return null;
            }
            mostPopularMoviesJsonStr = buffer.toString();
        }catch (IOException e){
            Log.e("", "Error " + e);
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

        try{
            result = result && getMovieDataFromJson(mostPopularMoviesJsonStr);
        }catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        try{
            final String THEMOVIEDB_BASE_URL = "http://api.themoviedb.org";
            final String SORT_PARAM = "sort_by";
            final String APPID_PARAM = "api_key";

            Uri builtUri = Uri.parse(THEMOVIEDB_BASE_URL).buildUpon()
                    .appendPath("3")
                    .appendPath("discover")
                    .appendPath("movie")
                    .appendQueryParameter(SORT_PARAM, voteAverage)
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
                buffer.append(line + "\n");
            }

            if(buffer.length() == 0){
                return null;
            }
            highestRatedMoviesJsonStr = buffer.toString();
        }catch (IOException e){
            Log.e("", "Error " + e);
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

        try{
            result = result && getMovieDataFromJson(highestRatedMoviesJsonStr);
        }catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(Boolean results) {
        super.onPostExecute(results);
    }


    public boolean getMovieDataFromJson(String moviesJsonStr) throws JSONException {
        final String MOVIE_LIST = "results";

        final String MOVIE_ID = "id";
        final String MOVIE_TITLE = "title";
        final String MOVIE_POSTER = "poster_path";
        final String MOVIE_SYNOPSIS = "overview";
        final String MOVIE_RELEASE_DATE = "release_date";
        final String MOVIE_POPULARITY = "popularity";
        final String MOVIE_VOTE_AVERAGE = "vote_average";

        final String DATEFORMAT = "yyyy-MM-dd";

        try{
            JSONObject moviesJson = new JSONObject(moviesJsonStr);
            JSONArray moviesArray = moviesJson.getJSONArray(MOVIE_LIST);

            Vector<ContentValues> cVVector = new Vector<ContentValues>(moviesArray.length());
            Calendar cal = Calendar.getInstance();

            for(int i = 0; i < moviesArray.length(); i++){
                JSONObject movie = moviesArray.getJSONObject(i);
                SimpleDateFormat dateFormat = new SimpleDateFormat(DATEFORMAT);
                String release_date = movie.getString(MOVIE_RELEASE_DATE);

                ContentValues values = new ContentValues();
                values.put(MovieContract.MovieEntry.API_ID, movie.getLong(MOVIE_ID));
                values.put(MovieContract.MovieEntry.TITLE, movie.getString(MOVIE_TITLE));
                values.put(MovieContract.MovieEntry.SYNOPSIS, movie.getString(MOVIE_SYNOPSIS));
                values.put(MovieContract.MovieEntry.POSTER, movie.getString(MOVIE_POSTER));
                if(release_date != "null"){
                    cal.setTime(dateFormat.parse(release_date));
                    values.put(MovieContract.MovieEntry.RELEASE_DATE, cal.getTimeInMillis());
                }
                values.put(MovieContract.MovieEntry.POPULARITY, movie.getDouble(MOVIE_POPULARITY));
                values.put(MovieContract.MovieEntry.VOTE_AVERAGE, movie.getDouble(MOVIE_VOTE_AVERAGE));

                cVVector.add(values);
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
        }catch (ParseException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }
}
