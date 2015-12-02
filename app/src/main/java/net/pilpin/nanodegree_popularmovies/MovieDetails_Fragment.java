package net.pilpin.nanodegree_popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.pilpin.nanodegree_popularmovies.data.MovieContract;

import java.util.Calendar;
import java.util.Locale;

public class MovieDetails_Fragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    public static final String DETAIL_URI = "details";

    private final int MOVIE_LOADER = 2000;

    private static final String[] MOVIE_DETAILS_COLUMNS = {
            MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.TITLE,
            MovieContract.MovieEntry.RELEASE_DATE,
            MovieContract.MovieEntry.SYNOPSIS,
            MovieContract.MovieEntry.POSTER,
            MovieContract.MovieEntry.VOTE_AVERAGE,
            MovieContract.MovieEntry.FAVORITE
    };

    static final int COL_MOVIE_DETAILS_ID = 0;
    static final int COL_MOVIE_DETAILS_TITLE = 1;
    static final int COL_MOVIE_DETAILS_RELEASE_DATE = 2;
    static final int COL_MOVIE_DETAILS_SYNOPSIS = 3;
    static final int COL_MOVIE_DETAILS_POSTER = 4;
    static final int COL_MOVIE_DETAILS_VOTE_AVERAGE = 5;
    static final int COL_MOVIE_DETAILS_FAVORITE = 6;

    private ImageView mPoster;
    private TextView mTitle;
    private TextView mReleaseDate;
    private TextView mVoteAverage;
    private CheckBox mFavorite;
    private TextView mSynopsis;

    private Uri mUri;

    public MovieDetails_Fragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle args = getArguments();
        if(args != null){
            mUri = args.getParcelable(DETAIL_URI);
        }

        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        mPoster = (ImageView) view.findViewById(R.id.details_poster);
        mTitle = (TextView) view.findViewById(R.id.details_title);
        mReleaseDate = (TextView) view.findViewById(R.id.details_release_date);
        mVoteAverage = (TextView) view.findViewById(R.id.details_vote_average);
        mFavorite = (CheckBox) view.findViewById(R.id.details_favorite);
        mSynopsis = (TextView) view.findViewById(R.id.details_synopsis);

        mFavorite.setOnClickListener(this);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(this.getClass().toString(), "Test " + getActivity().getClass().toString());
        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Intent intent = getActivity().getIntent();
        if(intent != null) {
            return new CursorLoader(
                    getActivity(),
                    mUri,
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

            setTitle(data.getString(COL_MOVIE_DETAILS_TITLE));

            mVoteAverage.setText(data.getString(COL_MOVIE_DETAILS_VOTE_AVERAGE) + " / 10");

            if(!data.isNull(COL_MOVIE_DETAILS_RELEASE_DATE)) {
                cal.setTimeInMillis(data.getLong(COL_MOVIE_DETAILS_RELEASE_DATE));
                dateStr = cal.get(Calendar.DAY_OF_MONTH) + " " + cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " " + cal.get(Calendar.YEAR);
            }else{
                dateStr = "Released date unknown";
            }

            mReleaseDate.setText(dateStr);

            mFavorite.setChecked(data.getString(COL_MOVIE_DETAILS_FAVORITE).equals(MovieContract.MovieEntry.FAVORITED));

            if(!data.isNull(COL_MOVIE_DETAILS_SYNOPSIS)) {
                mSynopsis.setText(data.getString(COL_MOVIE_DETAILS_SYNOPSIS));
            }
            if(!data.isNull(COL_MOVIE_DETAILS_POSTER)) {
                Picasso.with(getActivity()).load(POSTER_URL_BASE_PATH + data.getString(COL_MOVIE_DETAILS_POSTER)).placeholder(R.drawable.poster_holder).into(mPoster);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onClick(View v) {
        CheckBox cb = (CheckBox) v;
        ContentValues cv = new ContentValues();
        if(cb.isChecked()){
            cv.put(MovieContract.MovieEntry.FAVORITE, MovieContract.MovieEntry.FAVORITED);
        }else{
            cv.put(MovieContract.MovieEntry.FAVORITE, MovieContract.MovieEntry.NOT_FAVORITED);
        }

        getActivity().getContentResolver().update(
                mUri,
                cv,
                null,
                null
        );
    }

    public void setTitle(String title){
        if (mTitle != null){
            mTitle.setText(title);
        }else{
            if(getActivity().getClass().equals(MovieDetails_Activity.class)) {
                ActionBar actionBar = ((MovieDetails_Activity) getActivity()).getSupportActionBar();
                if(actionBar != null) {
                    actionBar.setTitle(title);
                }
            }
        }
    }
}
