package net.pilpin.nanodegree_popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import net.pilpin.nanodegree_popularmovies.data.MovieContract;

public class MovieGrid_Fragment extends Fragment implements AdapterView.OnItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    public static final int MOVIE_LOADER_POPULARITY = 10000;
    public static final int MOVIE_LOADER_VOTE = 10101;
    public static final int MOVIE_LOADER_FAVORITE = 11111;

    private static final String[] MOVIES_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.API_ID,
            MovieContract.MovieEntry.TITLE,
            MovieContract.MovieEntry.POSTER,
    };

    static final int COL_MOVIES_ID = 0;
    static final int COL_MOVIES_API_ID = 1;
    static final int COL_MOVIES_TITLE = 2;
    static final int COL_MOVIES_POSTER = 3;

    private OnFragmentInteractionListener mListener;
    private MovieAdapter mAdapter;

    public MovieGrid_Fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mAdapter = new MovieAdapter(getActivity(), null, true);
        GridView view = (GridView) inflater.inflate(R.layout.fragment_movie_grid, container, false);
        view.setAdapter(mAdapter);
        view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mListener.onFragmentInteraction(MovieContract.MovieEntry.buildMovieUri(id), (long) view.getTag(R.integer.movie_api_id));
            }
        });
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mListener = (OnFragmentInteractionListener) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (id == 0) {
            getLoaderManager().restartLoader(MOVIE_LOADER_POPULARITY, null, this);
        } else if (id == 1) {
            getLoaderManager().restartLoader(MOVIE_LOADER_VOTE, null, this);
        } else if (id == 2) {
            getLoaderManager().restartLoader(MOVIE_LOADER_FAVORITE, null, this);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch(id){
            case MOVIE_LOADER_POPULARITY:
                return new CursorLoader(
                        getActivity(),
                        MovieContract.MovieEntry.CONTENT_URI,
                        MOVIES_COLUMNS,
                        null,
                        null,
                        MovieContract.MovieEntry.ORDER_BY_POPULARITY);
            case MOVIE_LOADER_VOTE:
                return new CursorLoader(
                        getActivity(),
                        MovieContract.MovieEntry.CONTENT_URI,
                        MOVIES_COLUMNS,
                        null,
                        null,
                        MovieContract.MovieEntry.ORDER_BY_VOTE_AVERAGE);
            case MOVIE_LOADER_FAVORITE:
                return new CursorLoader(
                        getActivity(),
                        MovieContract.MovieEntry.CONTENT_URI,
                        MOVIES_COLUMNS,
                        MovieContract.MovieEntry.FAVORITE + " = ?",
                        new String[]{MovieContract.MovieEntry.FAVORITED},
                        null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader loader) {
        mAdapter.swapCursor(null);
    }

    private class MovieAdapter extends CursorAdapter{
        private LayoutInflater inflater;

        public MovieAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = inflater.inflate(R.layout.grid_item_movie, parent, false);

            ViewHolder viewHolder = new ViewHolder(view);
            view.setTag(R.integer.view_holder, viewHolder);

            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            final String POSTER_URL_BASE_PATH = "http://image.tmdb.org/t/p/" + getResources().getString(R.string.poster_movie_grid_size) + "/";
            ViewHolder viewHolder = (ViewHolder) view.getTag(R.integer.view_holder);

            view.setTag(R.integer.movie_api_id, cursor.getLong(COL_MOVIES_API_ID));
            String posterUrl = POSTER_URL_BASE_PATH + cursor.getString(COL_MOVIES_POSTER);
            Picasso.with(context).load(posterUrl).placeholder(R.drawable.poster_holder).into(viewHolder.posterView);
        }

        private class ViewHolder {
            public final ImageView posterView;

            public ViewHolder(View view) {
                posterView = (ImageView) view.findViewById(R.id.poster);
            }
        }
    }

    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri, long movieApiId);
    }

}
