package com.parkingapp.sample;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
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
        test_string1 = favoritesConnectionHandler.getData(1);
        markerSnippet = (TextView)findViewById(R.id.marker_snippet);
        markerSnippet.setText(test_string1);
    }

    public void showMoreInfo(View view) {
        DialogFragment myFragment = new FavoriteInformation();
        myFragment.show(getFragmentManager(), "favInfoDialog");
    }

    public static class FavoriteInformation extends DialogFragment {
        String mStr;
        static FavoriteInformation newInstance(String str) {
            FavoriteInformation f = new FavoriteInformation();

            Bundle args = new Bundle();
            args.putString("str", str);
            f.setArguments(args);

            return f;
        }
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder favInfoDialog = new AlertDialog.Builder(getActivity(),R.style.DialogTheme);
            favInfoDialog.setTitle("Test string");
            favInfoDialog.setMessage("Test for the test that I'm testing for the test of.");
            favInfoDialog.setNegativeButton("CLOSE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }
            });
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
