package com.parkingapp.sample;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.pooja.sfparksample.R;

import java.util.List;

/**
 * Created by Omar on 5/13/2015.
 */
public class RVAdapter extends RecyclerView.Adapter<RVAdapter.MarkerViewHolder> {

    public static class MarkerViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView markerName;

        MarkerViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            markerName = (TextView)itemView.findViewById(R.id.marker_snippet);
        }
    }

    List<Marker> markers;

    RVAdapter(List<Marker> markers){
        this.markers = markers;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public MarkerViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
        MarkerViewHolder mvh = new MarkerViewHolder(v);
        return mvh;
    }

    @Override
    public void onBindViewHolder(MarkerViewHolder markerViewHolder, int i) {
        markerViewHolder.markerName.setText(markers.get(i).snippet);
    }
    @Override
    public int getItemCount() {
        return markers.size();
    }
}
