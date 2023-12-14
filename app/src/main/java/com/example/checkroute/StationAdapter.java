package com.example.checkroute;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class StationAdapter extends RecyclerView.Adapter<StationAdapter.ViewHolder> {

    private List<Station> listStation;
    private Context context;

    public StationAdapter(List<Station> listStation, Context context) {
        this.listStation = listStation;
        this.context = context;
    }

    @NonNull
    @Override
    public StationAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.station_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final StationAdapter.ViewHolder viewHolder, int i) {
        final Station station = listStation.get(i);

        viewHolder.heading.setText(station.getName());
        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "lat: " + station.getLatitude() +
                         ", lon: " + station.getLongitude(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent("find_station");
                intent.putExtra("lat", station.getLatitude());
                intent.putExtra("lon", station.getLongitude());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listStation.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView heading;
        private TextView description;
        private LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            heading = itemView.findViewById(R.id.heading);
            description = itemView.findViewById(R.id.description);
            linearLayout = itemView.findViewById(R.id.linearLayoutStation);
        }
    }
}
