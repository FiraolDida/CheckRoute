package com.example.checkroute;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class RealTimeBusAlarm extends BroadcastReceiver {

    private Context context;
    private MapboxMap mapboxMap;
    private String eid;
    private String URL_REAL_TIME_BUS;
    private Bus bus;

    public RealTimeBusAlarm(Context context, MapboxMap mapboxMap, String eid) {
        this.context = context;
        this.mapboxMap = mapboxMap;
        this.eid = eid;
    }

    public RealTimeBusAlarm() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Real time bus alarm", Toast.LENGTH_SHORT).show();
        locateRealTimeBus();
    }

    private void locateRealTimeBus(){
        final String URL = URL_REAL_TIME_BUS + eid;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, URL,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        int count = 0;
                        Log.d("locaterealtimebus", "onResponse: " + response.length());
                        while (count < response.length()){
                            try {
                                JSONObject jsonObject = response.getJSONObject(count);
                                JSONObject getEvent = (JSONObject) jsonObject.get("events");
                                JSONObject getD = (JSONObject) getEvent.get("d");
                                JSONObject getPos = (JSONObject) getD.get("pos");
                                bus = new Bus(getEvent.getString("i"),
                                        getPos.getDouble("y"),
                                        getPos.getDouble("x"));

                                Log.d("locaterealtimebus", "id: " + bus.getId() +
                                        ", lat: " + bus.getLatitude() +
                                        ", lon: " + bus.getLongitude());

                                createMarker(bus.getId(), bus.getLatitude(),
                                        bus.getLongitude());

                                count++;
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        setNextAlarm();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(context, "Error fetching active buses", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createMarker(String name, double latitude, double longitude){
        // Create an Icon object for the marker to use
        IconFactory iconFactory = IconFactory.getInstance(context);
        Icon icon = iconFactory.fromResource(R.drawable.bus_station);

        mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
//                .icon(icon)
                .title(name));
    }

    private void setNextAlarm(){
        AlarmManager alarmManager = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(getApplicationContext(), RealTimeBusAlarm.class);

        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1,
                intent, 0);

        // Set the alarm //
        alarmManager.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + 5000,
                pendingIntent);
    }

}
