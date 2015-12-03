package net.pilpin.nanodegree_popularmovies;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.pilpin.nanodegree_popularmovies.data.MovieContract;

public class MovieReviews_Fragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private final int REVIEWS_LOADER = 11;

    private static final String[] REVIEWS_COLUMNS = {
            MovieContract.ReviewEntry.CONTENT,
            MovieContract.ReviewEntry.AUTHOR};

    static final int COL_REVIEW_CONTENT = 0;
    static final int COL_REVIEW_AUTHOR = 1;

    private long mMovie_id;
    private long mMovieApiId;

    private LinearLayout content;
    private TextView title;

    public static MovieReviews_Fragment newInstance(Uri data, long movieApiId){
        Bundle args = new Bundle();
        args.putLong(MovieDetails_Activity.MOVIE_ID, Long.decode(data.getLastPathSegment()));
        args.putLong(MovieDetails_Activity.MOVIE_API_ID, movieApiId);

        MovieReviews_Fragment fragment = new MovieReviews_Fragment();
        fragment.setArguments(args);

        return fragment;
    }

    public MovieReviews_Fragment() {
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
            mMovie_id = args.getLong(MovieDetails_Activity.MOVIE_ID);
            mMovieApiId = args.getLong(MovieDetails_Activity.MOVIE_API_ID);
        }

        content = (LinearLayout) inflater.inflate(R.layout.fragment_movie_reviews, container, false);
        title = (TextView) container.findViewById(R.id.reviews_title);

        getLoaderManager().initLoader(REVIEWS_LOADER, null, this);

        return content;
    }

    @Override
    public void onResume() {
        super.onResume();
            FetchReviewsTask fetchReviewsTask = new FetchReviewsTask(getActivity(), mMovie_id, mMovieApiId);
            fetchReviewsTask.execute();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                getActivity(),
                MovieContract.ReviewEntry.buildMovieUri(mMovie_id),
                REVIEWS_COLUMNS,
                null,
                null,
                MovieContract.ReviewEntry.ORDER_BY);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data == null || data.getCount() == 0){
            content.setVisibility(View.GONE);
        }else {
            content.setVisibility(View.VISIBLE);
            content.removeViews(1, content.getChildCount() - 1);
            while(data.moveToNext()) {
                TextView review = new TextView(getActivity());
                review.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                int padding = (int) getResources().getDimension(R.dimen.padding);
                review.setPadding(padding, padding, padding, padding);
                review.setText(Html.fromHtml(Html.fromHtml(data.getString(COL_REVIEW_CONTENT)).toString() + "<br /><b>" + data.getString(COL_REVIEW_AUTHOR) + "</b>"));
                content.addView(review);
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
