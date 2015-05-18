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

    /**
     * This custom ViewHolder will initialize the views that belong to the items in our RecyclerView
     */
    public static class MarkerViewHolder extends RecyclerView.ViewHolder {

        CardView cv;
        TextView markerName;

        MarkerViewHolder(View itemView) {
            super(itemView);
            cv = (CardView)itemView.findViewById(R.id.cv);
            markerName = (TextView)itemView.findViewById(R.id.marker_snippet);
        }
    }

    /**
     * Our data is in the form of a list of Marker objects so we implement this in the following way
     */
    List<Marker> markers;

    RVAdapter(List<Marker> markers){
        this.markers = markers;
    }

    /**
     * We also need to Override onAttachedToRecyclerView but we can just use the superclass's
     * implementation of this method.
     * @param recyclerView
     */
    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    /**
     * This method will be called when our custom ViewHolder is initialized.
     *
     * We specific which layout each item in our RecyclerView will take. We inflate the item
     * layout using LayoutInflater. We then pass that output to the constructor of our custom
     * ViewHolder.
     * @param viewGroup
     * @param i
     * @return
     */
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

    /**
     * Since we are creating our own custom Adapter, we need to Override getItemCount()
     * This will return the number of items present in our data. In this case, the number of
     * favorites that we are showing.
     * @return
     */
    @Override
    public int getItemCount() {
        return markers.size();
    }
}
