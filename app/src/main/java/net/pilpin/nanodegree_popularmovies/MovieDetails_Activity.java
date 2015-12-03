package net.pilpin.nanodegree_popularmovies;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class MovieDetails_Activity extends AppCompatActivity {
    public static final String MOVIE_DETAIL_URI = "details";
    public static final String MOVIE_ID = "movie_id";
    public static final String MOVIE_API_ID = "movie_api_id";

    public final String DETAILS_TAG = "details";
    public final String TRAILERS_TAG = "trailers";
    public final String REVIEWS_TAG = "reviews";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null){
            getFragmentManager().beginTransaction()
                    .add(R.id.movie_details_container, MovieDetails_Fragment.newInstance(getIntent().getData()), DETAILS_TAG)
                    .add(R.id.movie_trailers_container, MovieTrailers_Fragment.newInstance(getIntent().getData(), getIntent().getLongExtra(MOVIE_API_ID, 0)), TRAILERS_TAG)
                    .add(R.id.movie_reviews_container, MovieReviews_Fragment.newInstance(getIntent().getData(), getIntent().getLongExtra(MOVIE_API_ID, 0)), REVIEWS_TAG)
                    .commit();
        }
    }
}
