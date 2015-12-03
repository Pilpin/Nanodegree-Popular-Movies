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

public class FetchReviewsTask extends AsyncTask<Void, Void, Boolean> {
    private final String LOG_TAG = this.getClass().toString();
    private final Context mContext;
    private final long mMovieId;
    private final long mMovieApiId;

    public FetchReviewsTask(Context context, long movieId, long movieApiId){
        mContext = context;
        mMovieId = movieId;
        mMovieApiId = movieApiId;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        Boolean result = false;

        mContext.getContentResolver().delete(
                MovieContract.ReviewEntry.buildMovieUri(mMovieId),
                null,
                null);

        try{
            String reviewsJsonStr = fetchReviews();
            result = getDataFromJsonReviewList(reviewsJsonStr);
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

    private String fetchReviews(){
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String result;

        try{
            final String THEMOVIEDB_BASE_URL = "http://api.themoviedb.org";
            final String APIKEY_PARAM = "api_key";

            Uri builtUri = Uri.parse(THEMOVIEDB_BASE_URL).buildUpon()
                    .appendPath("3")
                    .appendPath("movie")
                    .appendPath(Long.toString(mMovieApiId))
                    .appendPath("reviews")
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

    private boolean getDataFromJsonReviewList(String reviewsJsonStr) throws JSONException {
        final String REVIEW_LIST = "results";

        try{
            JSONObject reviewsJson = new JSONObject(reviewsJsonStr);
            JSONArray reviewsArray = reviewsJson.getJSONArray(REVIEW_LIST);

            Vector<ContentValues> cVVector = new Vector<>(reviewsArray.length());

            for(int i = 0; i < reviewsArray.length(); i++){
                JSONObject review = reviewsArray.getJSONObject(i);
                cVVector.add(getDataFromJsonReviewDetails(review));
            }

            int insertCount = 0;
            if(cVVector.size() > 0){
                ContentValues[] valuesArray = new ContentValues[cVVector.size()];
                cVVector.toArray(valuesArray);
                insertCount = mContext.getContentResolver().bulkInsert(MovieContract.ReviewEntry.buildMovieUri(mMovieId), valuesArray);
            }

            return insertCount > 0;
        }catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }

    private ContentValues getDataFromJsonReviewDetails(JSONObject review){
        final String REVIEW_AUTHOR = "author";
        final String REVIEW_CONTENT = "content";
        final String REVIEW_URL = "url";

        ContentValues values = new ContentValues();

        try{
            values.put(MovieContract.ReviewEntry.MOVIE_ID, mMovieId);
            values.put(MovieContract.ReviewEntry.AUTHOR, review.getString(REVIEW_AUTHOR));
            values.put(MovieContract.ReviewEntry.CONTENT, review.getString(REVIEW_CONTENT));
            values.put(MovieContract.ReviewEntry.URL, review.getString(REVIEW_URL));
        }catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return values;
    }
}
