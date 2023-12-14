package com.example.checkroute;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.ViewHolder> {

    private List<Route> listRoute;
    private Context context;

    public RouteAdapter(List<Route> listRoute, Context context) {
        this.listRoute = listRoute;
        this.context = context;
    }

    @NonNull
    @Override
    public RouteAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.route_list_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteAdapter.ViewHolder viewHolder, int i) {
        final Route route = listRoute.get(i);

        viewHolder.heading.setText(route.getName());
        viewHolder.linearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "name: " + route.getName()
                        + ", sLat: " + route.getsLatitude()
                        + ", sLon: " + route.getsLongitude()
                        + ", dLat: " + route.getdLatitude()
                        + ", dLon: " + route.getdLongitude(), Toast.LENGTH_SHORT).show();

                Intent intent = new Intent("find_route");
                intent.putExtra("sLon", route.getsLongitude());
                intent.putExtra("sLat", route.getsLatitude());
                intent.putExtra("dLon", route.getdLongitude());
                intent.putExtra("dLat", route.getdLatitude());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listRoute.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView heading;
        private TextView description;
        private LinearLayout linearLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            heading = itemView.findViewById(R.id.heading);
            description = itemView.findViewById(R.id.description);
            linearLayout = itemView.findViewById(R.id.linearLayoutRoute);
        }
    }

}
