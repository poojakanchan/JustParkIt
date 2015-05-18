package com.parkingapp.sample;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.example.pooja.sfparksample.R;
import com.google.android.gms.maps.model.*;
import com.parkingapp.database.FavoritesConnectionHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Omar on 5/13/2015.
 */
public class RecyclerViewActivity extends AppCompatActivity {

    private List<Marker> markers;
    private RecyclerView rv;
    FavoritesConnectionHandler favoritesConnectionHandler;

    /**
     * Here we initialize a LayoutManager which will manage the position of the text items.
     *
     * It is possible to define our own LayoutManager but I decided to use one of the predefined
     * LayoutManager subclasses, the LinearLayoutManager subclass. This will make our RecyclerView
     * look just like a ListView.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recyclerview_activity);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        rv=(RecyclerView)findViewById(R.id.rv);

        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        initializeData();
        initializeAdapter();
    }

    /**
     * Creates an array list has multiple Marker objects.
     */
    private void initializeData(){
        ContextWrapper contextWrapper = new ContextWrapper(getBaseContext());
        favoritesConnectionHandler = FavoritesConnectionHandler.getDBHandler(contextWrapper);

        if (favoritesConnectionHandler.numberOfRows() <= 4) {
            String test_string1 = favoritesConnectionHandler.getData(1);
            String test_string2 = favoritesConnectionHandler.getData(2);
            String test_string3 = favoritesConnectionHandler.getData(3);
            String test_string4 = favoritesConnectionHandler.getData(4);
            markers = new ArrayList<>();
            markers.add(new Marker(test_string1));
            markers.add(new Marker(test_string2));
            markers.add(new Marker(test_string3));
            markers.add(new Marker(test_string4));
        } else {
            String test_string1 = favoritesConnectionHandler.getData(1);
            String test_string2 = favoritesConnectionHandler.getData(2);
            String test_string3 = favoritesConnectionHandler.getData(3);
            String test_string4 = favoritesConnectionHandler.getData(4);
            markers = new ArrayList<>();
            markers.add(new Marker(test_string1));
            markers.add(new Marker(test_string2));
            markers.add(new Marker(test_string3));
            markers.add(new Marker(test_string4));
        }
    }

    /**
     * Here we initialize the adapter by calling the adapter's constructor and the RecyclerView's
     * setAdapter method.
     */
    private void initializeAdapter(){
        RVAdapter adapter = new RVAdapter(markers);
        rv.setAdapter(adapter);
    }

    /**
     * Returns the favInfoDialog that is implemented below
     * @param view
     */
    public void showMoreInfo(View view) {
        DialogFragment myFragment = new FavoriteInformation();
        myFragment.show(getFragmentManager(), "favInfoDialog");
    }

    /**
     * Used to create a dialog fragment when a user attempts to add a location to their favorites.
     */
    public static class FavoriteInformation extends DialogFragment {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder favInfoDialog = new AlertDialog.Builder(getActivity(),R.style.DialogTheme);
            favInfoDialog.setTitle("TEST DIALOG");
            favInfoDialog.setMessage("Test for the test that I'm testing for the test of the test.");
            return favInfoDialog.create();
        }
    }


    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id==android.R.id.home) {
            finish();
        }
        return true;

    }
}
