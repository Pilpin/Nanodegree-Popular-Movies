package net.pilpin.nanodegree_popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements MovieGrid_Fragment.OnFragmentInteractionListener {
    private final String SPINNER_POSITION = "spinner_position";
    private Spinner spinner;
    private TextView lost_connection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        lost_connection = (TextView) findViewById(R.id.internet_info);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        AdapterView.OnItemSelectedListener fragment = (AdapterView.OnItemSelectedListener) getFragmentManager().findFragmentById(R.id.fragment_movies_grid);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getSupportActionBar().getThemedContext(), R.array.nav_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner = (Spinner) findViewById(R.id.spinner_nav);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(fragment);
    }

    @Override
    protected void onResume() {
        // if the last time we fetched the movie list was more than 6 hours ago, let's update
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR, -6);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        long last_update = preferences.getLong(getResources().getString(R.string.pref_last_updated_key), 0);

        if(last_update < cal.getTimeInMillis()) {
            ConnectivityManager cm =(ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

            if(activeNetwork != null && activeNetwork.isConnected()){
                lost_connection.setVisibility(View.GONE);
                FetchMoviesTask fetchMoviesTask = new FetchMoviesTask(this);
                fetchMoviesTask.execute();
            }else{
                if(last_update == 0){
                    lost_connection.setText(getResources().getString(R.string.offline_no_movies));
                }else{
                    lost_connection.setText(getResources().getString(R.string.offline_movies));
                }
                lost_connection.setVisibility(View.VISIBLE);
            }

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
        Intent intent = new Intent(this, MovieDetails_Activity.class);
        intent.setData(uri);
        startActivity(intent);
    }
}
