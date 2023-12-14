package com.example.checkroute;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
//import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private String URL_TRIP = "http://f793f451.ngrok.io/routes";
    private MapView mapView;
    private List<Point> routeCoordinates;
    private List<Trip> tripList;
    private Trip trip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.mapView1);
        routeCoordinates = new ArrayList<>();
        tripList = new ArrayList<>();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                mapboxMap.setStyle(Style.OUTDOORS, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
//                        initRouteCoordinates();
                        locateTrips();
                        drawTrip();
                        // Create the LineString from the list of coordinates and then make a GeoJSON
                        // FeatureCollection so we can add the line to our map as a layer.
                        style.addSource(new GeoJsonSource("line-source",
                                FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                                        LineString.fromLngLats(routeCoordinates)
                                )})));

                        // The layer properties for our line. This is where we make the line dotted, set the
                        // color, etc.
                        style.addLayer(new LineLayer("linelayer", "line-source").withProperties(
                                PropertyFactory.lineDasharray(new Float[] {0.01f, 2f}),
                                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                                PropertyFactory.lineWidth(5f),
                                PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
                        ));
                    }
                });
            }
        });
    }

    private void locateTrips(){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                URL_TRIP, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int count = 0;
                while (count < response.length()){
                    try {
                        JSONObject object = response.getJSONObject(count);
                            Log.d(TAG, "onResponse: SH0");
//                            routeCoordinates.add(Point.fromLngLat(object.getDouble("lon"),
//                                    object.getDouble("lat")));
                            trip = new Trip(object.getDouble("lat"),
                                    object.getDouble("lon"));
                            tripList.add(trip);
//                            drawTrip(trip.getLongitude(), trip.getLatitude());

                        count++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MainActivity.this, "Error fetching JSON: Trip", Toast.LENGTH_SHORT).show();
                error.printStackTrace();
            }
        });
        JSONRequestHelper.getInstance(getApplicationContext()).addToRequest(jsonArrayRequest);
        jsonArrayRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
    }

    private void drawTrip(){
        Log.d(TAG, "drawTrip: ");
        Iterator i = tripList.iterator();

        while (i.hasNext()){
            Log.d(TAG, "drawTrip: hasNext");
            routeCoordinates.add(Point.fromLngLat(trip.getLongitude(), trip.getLatitude()));
//            routeCoordinates.add(Point.fromLngLat(lon, lat));
        }

    }

    private void initRouteCoordinates() {
// Create a list to store our line coordinates.
        routeCoordinates = new ArrayList<>();
        routeCoordinates.add(Point.fromLngLat(38.76308, 9.03465));
        routeCoordinates.add(Point.fromLngLat(38.76308, 9.03462));
        routeCoordinates.add(Point.fromLngLat(38.76308, 9.03462));
        routeCoordinates.add(Point.fromLngLat(38.76308, 9.03462));
        routeCoordinates.add(Point.fromLngLat(38.76313, 9.0343));
        routeCoordinates.add(Point.fromLngLat(38.76319, 9.0339));
        routeCoordinates.add(Point.fromLngLat(38.76329, 9.03317));
        routeCoordinates.add(Point.fromLngLat(38.76329, 9.03317));
        routeCoordinates.add(Point.fromLngLat(38.76322, 9.03314));
        routeCoordinates.add(Point.fromLngLat(38.76315, 9.03307));
        routeCoordinates.add(Point.fromLngLat(38.76311, 9.03299));
        routeCoordinates.add(Point.fromLngLat(38.76309, 9.03291));
        routeCoordinates.add(Point.fromLngLat(38.7631, 9.03283));
        routeCoordinates.add(Point.fromLngLat(38.76314, 9.03276));
        routeCoordinates.add(Point.fromLngLat(-118.38891287901787, 33.39686511465794));
        routeCoordinates.add(Point.fromLngLat(-118.38898167981154, 33.39671074380141));
        routeCoordinates.add(Point.fromLngLat(-118.38984598978178, 33.396064537239404));
        routeCoordinates.add(Point.fromLngLat(-118.38983738968255, 33.39582400356976));
        routeCoordinates.add(Point.fromLngLat(-118.38955358640874, 33.3955978295119));
        routeCoordinates.add(Point.fromLngLat(-118.389041880506, 33.39578092284221));
        routeCoordinates.add(Point.fromLngLat(-118.38872797688494, 33.3957916930261));
        routeCoordinates.add(Point.fromLngLat(-118.38817327048618, 33.39561218978703));
        routeCoordinates.add(Point.fromLngLat(-118.3872530598711, 33.3956265500598));
        routeCoordinates.add(Point.fromLngLat(-118.38653065153775, 33.39592811523983));
        routeCoordinates.add(Point.fromLngLat(-118.38638444985126, 33.39590657490452));
        routeCoordinates.add(Point.fromLngLat(-118.38638874990086, 33.395737842093304));
        routeCoordinates.add(Point.fromLngLat(-118.38723155962309, 33.395027006653244));
        routeCoordinates.add(Point.fromLngLat(-118.38734766096238, 33.394441819579285));
        routeCoordinates.add(Point.fromLngLat(-118.38785936686516, 33.39403972556368));
        routeCoordinates.add(Point.fromLngLat(-118.3880743693453, 33.393616088784825));
        routeCoordinates.add(Point.fromLngLat(-118.38791956755958, 33.39331092541894));
        routeCoordinates.add(Point.fromLngLat(-118.3874852625497, 33.39333964672257));
        routeCoordinates.add(Point.fromLngLat(-118.38686605540683, 33.39387816940854));
        routeCoordinates.add(Point.fromLngLat(-118.38607484627983, 33.39396792286514));
        routeCoordinates.add(Point.fromLngLat(-118.38519763616081, 33.39346171215717));
        routeCoordinates.add(Point.fromLngLat(-118.38523203655761, 33.393196040109466));
        routeCoordinates.add(Point.fromLngLat(-118.3849955338295, 33.393023711860515));
        routeCoordinates.add(Point.fromLngLat(-118.38355931726203, 33.39339708930139));
        routeCoordinates.add(Point.fromLngLat(-118.38323251349217, 33.39305243325907));
        routeCoordinates.add(Point.fromLngLat(-118.3832583137898, 33.39244928189641));
        routeCoordinates.add(Point.fromLngLat(-118.3848751324406, 33.39108499551671));
        routeCoordinates.add(Point.fromLngLat(-118.38522773650804, 33.38926830725471));
        routeCoordinates.add(Point.fromLngLat(-118.38508153482152, 33.38916777794189));
        routeCoordinates.add(Point.fromLngLat(-118.38390332123025, 33.39012280171983));
        routeCoordinates.add(Point.fromLngLat(-118.38318091289693, 33.38941192035707));
        routeCoordinates.add(Point.fromLngLat(-118.38271650753981, 33.3896129783018));
        routeCoordinates.add(Point.fromLngLat(-118.38275090793661, 33.38902416443619));
        routeCoordinates.add(Point.fromLngLat(-118.38226930238106, 33.3889451769069));
        routeCoordinates.add(Point.fromLngLat(-118.38258750605169, 33.388420985121336));
        routeCoordinates.add(Point.fromLngLat(-118.38177049662707, 33.388083490107284));
        routeCoordinates.add(Point.fromLngLat(-118.38080728551597, 33.38836353925403));
        routeCoordinates.add(Point.fromLngLat(-118.37928506795642, 33.38717870977523));
        routeCoordinates.add(Point.fromLngLat(-118.37898406448423, 33.3873079646849));
        routeCoordinates.add(Point.fromLngLat(-118.37935386875012, 33.38816247841951));
        routeCoordinates.add(Point.fromLngLat(-118.37794345248027, 33.387810620840135));
        routeCoordinates.add(Point.fromLngLat(-118.37546662390886, 33.38847843095069));
        routeCoordinates.add(Point.fromLngLat(-118.37091717142867, 33.39114243958559));

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
