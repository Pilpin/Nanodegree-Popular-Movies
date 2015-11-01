package net.pilpin.nanodegree_popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.pilpin.nanodegree_popularmovies.data.MovieContract;

import java.util.Calendar;
import java.util.Locale;

public class MovieDetails_Fragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final int MOVIE_LOADER = 2000;

    private static final String[] MOVIE_DETAILS_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.TITLE,
            MovieContract.MovieEntry.RELEASE_DATE,
            MovieContract.MovieEntry.SYNOPSIS,
            MovieContract.MovieEntry.POSTER,
            MovieContract.MovieEntry.VOTE_AVERAGE,
    };

    static final int COL_MOVIE_DETAILS_ID = 0;
    static final int COL_MOVIE_DETAILS_TITLE = 1;
    static final int COL_MOVIE_DETAILS_RELEASE_DATE = 2;
    static final int COL_MOVIE_DETAILS_SYNOPSIS = 3;
    static final int COL_MOVIE_DETAILS_POSTER = 4;
    static final int COL_MOVIE_DETAILS_VOTE_AVERAGE = 5;

    private OnFragmentInteractionListener mListener;
    private ImageView poster;
    private TextView release_date;
    private TextView vote_average;
    private TextView synopsis;

    public MovieDetails_Fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_movie_details, container, false);
        poster = (ImageView) v.findViewById(R.id.details_poster);
        release_date = (TextView) v.findViewById(R.id.details_release_date);
        vote_average = (TextView) v.findViewById(R.id.details_vote_average);
        synopsis = (TextView) v.findViewById(R.id.details_synopsis);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListener = (OnFragmentInteractionListener) getActivity();
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if(intent != null) {
            return new CursorLoader(
                    getActivity(),
                    intent.getData(),
                    MOVIE_DETAILS_COLUMNS,
                    null,
                    null,
                    null);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        final String POSTER_URL_BASE_PATH = "http://image.tmdb.org/t/p/" + getResources().getString(R.string.poster_movie_details_size) + "/";

        if(data.moveToFirst()){
            String dateStr;
            Calendar cal = Calendar.getInstance();

            String voteStr = "Average Rate: " + data.getString(COL_MOVIE_DETAILS_VOTE_AVERAGE);
            mListener.setActionBarTitle(data.getString(COL_MOVIE_DETAILS_TITLE));
            vote_average.setText(voteStr);

            if(!data.isNull(COL_MOVIE_DETAILS_RELEASE_DATE)) {
                cal.setTimeInMillis(data.getLong(COL_MOVIE_DETAILS_RELEASE_DATE));
                dateStr = "Released on " + cal.get(Calendar.DAY_OF_MONTH) + " " + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + cal.get(Calendar.YEAR);
            }else{
                dateStr = "Released date unknown";
            }

            release_date.setText(dateStr);


            if(!data.isNull(COL_MOVIE_DETAILS_SYNOPSIS)) {
                synopsis.setText(data.getString(COL_MOVIE_DETAILS_SYNOPSIS));
            }
            if(!data.isNull(COL_MOVIE_DETAILS_POSTER)) {
                Picasso.with(getActivity()).load(POSTER_URL_BASE_PATH + data.getString(COL_MOVIE_DETAILS_POSTER)).into(poster);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public interface OnFragmentInteractionListener {
        void setActionBarTitle(String title);
    }
}
