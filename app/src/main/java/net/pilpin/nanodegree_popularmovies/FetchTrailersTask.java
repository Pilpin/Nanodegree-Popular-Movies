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
import java.util.Vector;

public class FetchTrailersTask extends AsyncTask<Void, Void, Boolean> {
    private final String LOG_TAG = this.getClass().toString();
    private final Context mContext;
    private final String mMovieId;

    public FetchTrailersTask(Context context, long movieApiId){
        mContext = context;
        mMovieId = Long.toString(movieApiId);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean result = false;

        mContext.getContentResolver().delete(
                MovieContract.TrailerEntry.CONTENT_URI,
                MovieContract.TrailerEntry.MOVIE_ID + " = ?",
                new String[]{mMovieId});

        try{
            String reviewsJsonStr = fetchTrailers();
            result = getDataFromJsonTrailerList(reviewsJsonStr);
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

    private String fetchTrailers(){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String result;

        try{
            final String THEMOVIEDB_BASE_URL = "http://api.themoviedb.org";
            final String APIKEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(THEMOVIEDB_BASE_URL).buildUpon()
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(mMovieId)
                    .appendPath("videos")
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

    private boolean getDataFromJsonTrailerList(String trailersJsonStr) throws JSONException {
        final String TRAILER_LIST = "results";

        try{
            JSONObject trailersJson = new JSONObject(trailersJsonStr);
            JSONArray trailersArray = trailersJson.getJSONArray(TRAILER_LIST);

            Vector<ContentValues> cVVector = new Vector<>(trailersArray.length());

            for(int i = 0; i < trailersArray.length(); i++){
                JSONObject trailer = trailersArray.getJSONObject(i);
                cVVector.add(getDataFromJsonTrailerDetails(trailer));
            }

            int insertCount = 0;
            if(cVVector.size() > 0){
                ContentValues[] valuesArray = new ContentValues[cVVector.size()];
                cVVector.toArray(valuesArray);
                insertCount = mContext.getContentResolver().bulkInsert(MovieContract.TrailerEntry.CONTENT_URI, valuesArray);
            }

            return insertCount > 0;
        }catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }

    private ContentValues getDataFromJsonTrailerDetails(JSONObject trailer){
        final String TRAILER_KEY = "key";
        final String TRAILER_NAME = "name";
        final String TRAILER_SITE = "site";
        final String TRAILER_SIZE = "size";
        final String TRAILER_TYPE = "type";

        ContentValues values = new ContentValues();

        try{
            values.put(MovieContract.TrailerEntry.MOVIE_ID, mMovieId);
            values.put(MovieContract.TrailerEntry.KEY, trailer.getString(TRAILER_KEY));
            values.put(MovieContract.TrailerEntry.NAME, trailer.getString(TRAILER_NAME));
            values.put(MovieContract.TrailerEntry.SITE, trailer.getString(TRAILER_SITE));
            values.put(MovieContract.TrailerEntry.SIZE, trailer.getString(TRAILER_SIZE));
            values.put(MovieContract.TrailerEntry.TYPE, trailer.getString(TRAILER_TYPE));
        }catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return values;
    }
}
