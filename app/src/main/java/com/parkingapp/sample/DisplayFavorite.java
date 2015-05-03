package com.parkingapp.sample;

import android.app.ListActivity;
import android.content.ContextWrapper;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.pooja.sfparksample.R;
import com.parkingapp.database.DBConnectionHandler;
import com.parkingapp.parser.OperationHoursBean;
import com.parkingapp.parser.SFParkBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pooja K
 * The class for handling favorite button operations.
 * It provides an activity to display listview of favorite parking locations stored in
 * database.
 */

public class DisplayFavorite extends ListActivity {

    DBConnectionHandler dbConnectionHandler;
    ArrayList<String> displayList;

    /**
     * extracts records from table PARKING_FAVORITES and displays them in listview.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_favorite);
        ContextWrapper contextWrapper = new ContextWrapper(getBaseContext());
        dbConnectionHandler = DBConnectionHandler.getDBHandler(contextWrapper);

        TextView textView = new TextView(contextWrapper);
        textView.setTextSize(20);
        textView.setHeight(75);
        textView.setText("List of Favorite Parking Locations");
        textView.setTextColor(Color.BLUE);
        getListView().addHeaderView(textView);

        List<SFParkBean> list = dbConnectionHandler.getFavouriteParkingSpots();
        displayList = new ArrayList<String>();
        for (SFParkBean bean : list) {
            StringBuilder sb = new StringBuilder();
            if (bean.getName() != null) {
                sb.append("Name: " + bean.getName() + "\n");
            }
            if (bean.getType() != null) {
                sb.append("Status: " + bean.getType() + "\n");
            }
            if (bean.getAddress() != null) {
                sb.append("Address: " + bean.getAddress() + "\n");
            }
            if (bean.getContactNumber() != null) {
                sb.append("Contact Number: " + bean.getContactNumber() + "\n");
            }
            if (bean.getOperationHours() != null) {
                sb.append("Operation Hours: \n");
                for (OperationHoursBean oprBean : bean.getOperationHours()) {
                    sb.append(oprBean.getFromDay());
                    if (oprBean.getToDay() != null) {
                        sb.append("-");
                        sb.append(oprBean.getToDay());
                    }
                    if (oprBean.getStartTime() != null) {
                        sb.append(" : ");
                        sb.append(oprBean.getStartTime());
                    }
                    if (oprBean.getEndTime() != null) {
                        sb.append("-");
                        sb.append(oprBean.getEndTime() + "\n");
                    }
                }
            }
            displayList.add(sb.toString());
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getListView().getContext(), android.R.layout.simple_list_item_1, displayList);
        getListView().setAdapter(adapter);
        registerForContextMenu(getListView());
    }

    /**
     * the method is called when a user long clicks on any item of the list.
     * On long click, 'delete from favorites' option is provided to user.
     * @param menu
     * @param view
     * @param menuInfo
     */

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        System.out.println("CALLLEEDDDDDD");
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        if (info.position == 0) {
            TextView header = (TextView) view.findViewById(30);
            header.setClickable(false);
            header.setLongClickable(false);
            view.findViewById(30).setClickable(false);
        } else {
            menu.add(Menu.NONE, 0, 0, "Delete from favorites");
        }
    }

    /**
     * The method is called when a user clicks on "remove from favorites".
     * The method removes the selected entry from database and reloads the list.
     * @param item the selected item
     * @return
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
       // int menuItemIndex = item.getItemId();
        String parkingInfo = displayList.get(info.position -1);
        String nameLine = parkingInfo.split("\n")[0];
        String name = nameLine.split(":")[1];
        dbConnectionHandler.removeParkingInfo(name.trim());
        displayList.remove(info.position -1);
        System.out.println("Size of display list after" + displayList.size());
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getListView().getContext(), android.R.layout.simple_list_item_1, displayList);
        getListView().setAdapter(adapter);
        return true;
    }
}
