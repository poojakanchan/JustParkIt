package com.parkingapp.sample;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.pooja.sfparksample.R;
import com.parkingapp.parser.OperationHoursBean;
import com.parkingapp.parser.SFParkBean;
import com.parkingapp.connection.SFParkHandler;
import com.parkingapp.exception.ParkingAppException;

import org.apache.http.HttpEntity;
import org.apache.http.entity.HttpEntityWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.GZIPInputStream;


public class SFParkSampleActivity extends ActionBarActivity {

    private static final int SECOND_IN_MILLIS = (int) DateUtils.SECOND_IN_MILLIS;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sfparksample);
        TextView t = (TextView)findViewById(R.id.apimsgTextView);
        SFParkHandler sfParkHandler = new SFParkHandler();
        String latitude= "37.792275";
        String longitude ="-122.397089";
        String radius = "0.25";
        //StringBuilder response = null;
        List<SFParkBean> response = null;
        try {
            response = sfParkHandler.callAvailabilityService(latitude, longitude, radius);

        } catch(ParkingAppException e) {

        }
        if(response != null) {
            StringBuilder sf = new StringBuilder();
            int count =0;
            for(SFParkBean bean: response) {
                count ++;
                if(bean.getOperationHours() != null) {
                    sf.append(" ********* Bean Info ********* ");
                    sf.append("Name " + bean.getName());
                    sf.append("Address " + bean.getAddress() + "\n");
                    sf.append("latitude " + bean.getLatitude() + "\n");
                    sf.append("longitude " + bean.getLongitude() + "\n");
                    sf.append("type " + bean.getType() + "\n");
                    sf.append("contact " + bean.getContactNumber() + "\n");
                    sf.append(" operation hours \n");
                    for (OperationHoursBean oprBean : bean.getOperationHours()) {
                        sf.append("fromday " + oprBean.getFromDay() + "\n");
                        sf.append("today" + oprBean.getToDay() + "\n");
                        sf.append("starttime " + oprBean.getStartTime() + "\n");
                        sf.append("endtime " + oprBean.getEndTime() + "\n");

                    }
                }
            }

            t.setText("count " + count + "info " + sf);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    private static class InflatingEntity extends HttpEntityWrapper {
        public InflatingEntity(HttpEntity wrapped) {
            super(wrapped);
        }

        @Override
        public InputStream getContent() throws IOException {
            return new GZIPInputStream(wrappedEntity.getContent());
        }

        @Override
        public long getContentLength() {
            return -1;
        }
    }

}