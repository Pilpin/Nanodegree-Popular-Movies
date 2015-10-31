package net.pilpin.nanodegree_popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener, MovieGrid_Fragment.OnFragmentInteractionListener {
    private final String SPINNER_POSITION = "spinner_position";
    private Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        spinner = (Spinner) findViewById(R.id.spinner_nav);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getSupportActionBar().getThemedContext(), R.array.nav_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        // NOT WORKING
        Log.e(this.getClass().toString(), String.valueOf(savedInstanceState.getInt(SPINNER_POSITION)));
        spinner.setSelection(savedInstanceState.getInt(SPINNER_POSITION));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(SPINNER_POSITION, spinner.getSelectedItemPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(view != null) {
            String selected = ((TextView) view).getText().toString();
            MovieGrid_Fragment fragment = (MovieGrid_Fragment) getFragmentManager().findFragmentById(R.id.fragment_movies_grid);

            if (selected == getResources().getString(R.string.movies_most_popular)) {
                fragment.switchLoader(fragment.MOVIE_LOADER_POPULARITY);
            } else if (selected == getResources().getString(R.string.movies_highest_rated)) {
                fragment.switchLoader(fragment.MOVIE_LOADER_VOTE);
            } else if (selected == getResources().getString(R.string.movies_favorite)) {
                //
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        Intent intent = new Intent(this, MovieDetails_Activity.class);
        intent.setData(uri);
        startActivity(intent);
    }
}
