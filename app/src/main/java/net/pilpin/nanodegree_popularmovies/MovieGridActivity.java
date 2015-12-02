package net.pilpin.nanodegree_popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Calendar;

public class MovieGridActivity extends AppCompatActivity implements MovieGrid_Fragment.OnFragmentInteractionListener {
    private final String DETAIL_FRAGMENT_TAG = "detailfragment";
    private final String SPINNER_POSITION = "spinner_position";
    private Spinner spinner;
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_grid);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        AdapterView.OnItemSelectedListener fragment = (AdapterView.OnItemSelectedListener) getFragmentManager().findFragmentById(R.id.fragment_movies_grid);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getSupportActionBar().getThemedContext(), R.array.nav_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = (Spinner) findViewById(R.id.spinner_nav);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(fragment);

        if (findViewById(R.id.movie_detail_container) != null) {
            mTwoPane = true;
        }
    }

    @Override
    protected void onResume() {
        // if the last time we fetched the movie list was more than 6 hours ago, let's update
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, -6);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long last_update = preferences.getLong(getResources().getString(R.string.pref_last_updated_key), 0);

        if(last_update < cal.getTimeInMillis()) {
            FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(this);
            fetchMoviesTask.execute();
        }

        super.onResume();
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        spinner.setSelection(savedInstanceState.getInt(SPINNER_POSITION));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SPINNER_POSITION, spinner.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        if(mTwoPane){
            Bundle args = new Bundle();
            args.putParcelable(MovieDetails_Fragment.DETAIL_URI, uri);

            MovieDetails_Fragment fragment = new MovieDetails_Fragment();
            fragment.setArguments(args);
            getFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, DETAIL_FRAGMENT_TAG)
                    .commit();
        }else {
            Intent intent = new Intent(this, MovieDetails_Activity.class).setData(uri);
            startActivity(intent);
        }
    }
}
