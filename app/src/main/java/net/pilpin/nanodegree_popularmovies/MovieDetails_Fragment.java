package net.pilpin.nanodegree_popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.pilpin.nanodegree_popularmovies.data.MovieContract;

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
    static final int COL_MOVIE_DETAILS_POSTER = 2;
    static final int COL_MOVIE_DETAILS_RELEASE_DATE = 3;
    static final int COL_MOVIE_DETAILS_SYNOPSIS = 4;
    static final int COL_MOVIE_DETAILS_VOTE_AVERAGE = 5;

    private OnFragmentInteractionListener mListener;

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
        return inflater.inflate(R.layout.fragment_movie_details, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListener = (OnFragmentInteractionListener) getActivity();
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                getActivity().getIntent().getData(),
                MOVIE_DETAILS_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()){
            mListener.setActionBarTitle(data.getString(COL_MOVIE_DETAILS_TITLE));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public interface OnFragmentInteractionListener {
        public void setActionBarTitle(String title);
    }
}
