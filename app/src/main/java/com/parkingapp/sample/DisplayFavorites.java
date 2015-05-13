package com.parkingapp.sample;

import android.content.ContextWrapper;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.pooja.sfparksample.R;
import com.parkingapp.database.FavoritesConnectionHandler;


public class DisplayFavorites extends AppCompatActivity {

    TextView markerSnippet;
    FavoritesConnectionHandler favoritesConnectionHandler;
    String test_string;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String test_string1;
        setContentView(R.layout.favorites_card_layout);
        ContextWrapper contextWrapper = new ContextWrapper(getBaseContext());
        favoritesConnectionHandler = FavoritesConnectionHandler.getDBHandler(contextWrapper);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        test_string1 = favoritesConnectionHandler.getData(2);
        markerSnippet = (TextView)findViewById(R.id.marker_snippet);
        markerSnippet.setText(test_string1);
    }

    public boolean onOptionsItemSelected(MenuItem item){
        int id = item.getItemId();

        if (id==android.R.id.home) {
            finish();
        }
        return true;

    }

}
