package com.example.checkroute;

import android.app.ProgressDialog;
import android.graphics.Color;

import android.os.Bundle;

import android.support.annotation.NonNull;

import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonParser;
import com.mapbox.api.directions.v5.DirectionsCriteria;

import com.mapbox.api.directions.v5.MapboxDirections;

import com.mapbox.api.directions.v5.models.DirectionsResponse;

import com.mapbox.api.directions.v5.models.DirectionsRoute;

import com.mapbox.geojson.Feature;

import com.mapbox.geojson.FeatureCollection;

import com.mapbox.geojson.LineString;

import com.mapbox.geojson.Point;

import com.mapbox.mapboxsdk.Mapbox;

import com.mapbox.mapboxsdk.maps.MapView;

import com.mapbox.mapboxsdk.maps.MapboxMap;

import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.mapboxsdk.style.layers.LineLayer;

import com.mapbox.mapboxsdk.style.layers.Property;

import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

import retrofit2.Callback;

import retrofit2.Response;

import timber.log.Timber;

import static com.mapbox.core.constants.Constants.PRECISION_6;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;



/**

 * Use Mapbox Java Services to request directions from the Mapbox Directions API and show the

 * route with a LineLayer.

 */

public class DirectionActivity extends AppCompatActivity implements
        MapView.OnDidFinishLoadingMapListener, MapView.OnDidFinishLoadingStyleListener {



    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String RED_PIN_ICON_ID = "red-pin-icon-id";
    private MapView mapView;

    private DirectionsRoute currentRoute;
    private MapboxDirections client;
    private Point origin;
    private Point destination;

    private RouteFragment.OnFragmentInteractionListener mListener;
    private static final String URL_ROUTES = "http://165.22.193.95/api/routes";
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<Route> listRoute;
    private String sLongitude;
    private String sLatitude;
    private String dLongitude;
    private String dLatitude;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application

        // object or in the same activity which contains the mapview.

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));



        // This contains the MapView in XML and needs to be called after the access token is configured.

        setContentView(R.layout.activity_direction);



        // Setup the MapView

        mapView = findViewById(R.id.mapViewRoute);

        mapView.onCreate(savedInstanceState);

        listRoute = new ArrayList<>();

        mapView.getMapAsync(new OnMapReadyCallback() {

            @Override

            public void onMapReady(@NonNull MapboxMap mapboxMap) {



                mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {

                    @Override

                    public void onStyleLoaded(@NonNull Style style) {

                        // Set the origin location to the Alhambra landmark in Granada, Spain.
//
//                        double ghi = Double.parseDouble(sLongitude);
//                        double jkl = Double.parseDouble(sLatitude);
//                        Log.d("qazwsx", "onStyleLoaded: " + ghi);
//                        Log.d("qazwsx", "onStyleLoaded: " + jkl);
//
//                        origin = Point.fromLngLat(ghi, jkl);



                        // Set the destination location to the Plaza del Triunfo in Granada, Spain.

//                        destination = Point.fromLngLat(Double.parseDouble(dLongitude),
//                                Double.parseDouble(dLatitude));

//                        double abc = Double.parseDouble(dLongitude);
//                        double def = Double.parseDouble(dLatitude);
//
//                        destination = (Point) Point.fromLngLat(abc, def);



//                        initSource(style);
//                        initLayers(style);


                        // Get the directions route from the Mapbox Directions API
                        getRoutes(style);

//                        getRoute(style, origin, destination);

                    }

                });

            }

        });

    }



    /**

     * Add the route and marker sources to the map

     */

    private void initSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID,
                FeatureCollection.fromFeatures(new Feature[] {})));

        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID,
                FeatureCollection.fromFeatures(new Feature[] {
                    Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude())),
                    Feature.fromGeometry(Point.fromLngLat(destination.longitude(),
                            destination.latitude()))}));
        loadedMapStyle.addSource(iconGeoJsonSource);
    }

    /**

     * Add the route and maker icon layers to the map

     */

    private void initLayers(@NonNull Style loadedMapStyle) {
        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);
        // Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(Color.parseColor("#009688"))
        );
        loadedMapStyle.addLayer(routeLayer);

        // Add the red marker icon image to the map

        loadedMapStyle.addImage(RED_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
                getResources().getDrawable(R.drawable.blue_marker)));

        // Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
                iconImage(RED_PIN_ICON_ID),
                iconIgnorePlacement(true),
                iconIgnorePlacement(true),
                iconOffset(new Float[] {0f, -4f})));
    }

    /**

     * Make a request to the Mapbox Directions API. Once successful, pass the route to the

     * route layer.

     *

     * @param origin      the starting point of the route

     * @param destination the desired finish point of the route

     */

    private void getRoute(@NonNull final Style style, Point origin, Point destination) {
        client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(getString(R.string.mapbox_access_token))
                .build();
        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call,
                                   Response<DirectionsResponse> response) {
                System.out.println(call.request().url().toString());
                // You can get the generic HTTP info about the response
                Timber.d("Response code: " + response.code());
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set the right user " +
                            "and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Timber.e("No routes found");
                    return;
                }
                // Get the directions route
                currentRoute = response.body().routes().get(0);
                // Make a toast which displays the route's distance
//                Toast.makeText(DirectionActivity.this, String.format(
//                        getString(R.string.directions_activity_toast_message),
//                        currentRoute.distance()), Toast.LENGTH_SHORT).show();

                if (style.isFullyLoaded()) {
                    // Retrieve and update the source designated for showing the directions route
                    GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);
                    // Create a LineString with the directions route's geometry and
                    // reset the GeoJSON source for the route LineLayer source
                    if (source != null) {
                        Timber.d("onResponse: source != null");
                        source.setGeoJson(FeatureCollection.fromFeature(
                                Feature.fromGeometry(LineString.fromPolyline(
                                        currentRoute.geometry(), PRECISION_6))));
                    }
                }
            }
            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Timber.e("Error: " + throwable.getMessage());
                Toast.makeText(DirectionActivity.this, "Error: " +
                                throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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

    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        mapView.onSaveInstanceState(outState);

    }



    @Override

    protected void onDestroy() {

        super.onDestroy();

        // Cancel the Directions API request

        if (client != null) {

            client.cancelCall();

        }

        mapView.onDestroy();

    }



    @Override

    public void onLowMemory() {

        super.onLowMemory();

        mapView.onLowMemory();

    }

    public void getRoutes(final Style style) {
        Log.d("DirectionRoute", "getRoutes: ");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                URL_ROUTES, null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int count = 0;
                while (count < response.length()){
                    try{
                        JSONObject jsonObject = response.getJSONObject(count);
                        JSONObject jsonObject1 = new JSONObject(jsonObject.getString("points"));
                        JSONArray jsonArray = jsonObject1.getJSONArray("coordinates");
                        List<String> list = new ArrayList<String>();
                        if (jsonArray != null){
                            for (int i = 0; i < jsonArray.length(); i++){
                                list.add(jsonArray.getString(i));
                            }
                        }

                        String[] strings = list.toArray(new String[0]);
                        String[] args = strings[0].split(",");
                        String sourceLongitude = args[0];
                        String sourceLatitude = args[1];
                        String destinationLongitude = args[args.length - 2];
                        String destinationLatitude = args[args.length - 1];

                        StringBuilder stringBuilderSLO = new StringBuilder(sourceLongitude);
                        stringBuilderSLO.deleteCharAt(0);
                        stringBuilderSLO.deleteCharAt(0);
                        sLongitude = stringBuilderSLO.toString();

                        StringBuilder stringBuilderSLA = new StringBuilder(sourceLatitude);
                        stringBuilderSLA.deleteCharAt(sourceLatitude.length()-1);
                        sLatitude = stringBuilderSLA.toString();

                        StringBuilder stringBuilderDLO = new StringBuilder(destinationLongitude);
                        stringBuilderDLO.deleteCharAt(0);
                        dLongitude = stringBuilderDLO.toString();

                        StringBuilder stringBuilderDLA = new StringBuilder(destinationLatitude);
                        stringBuilderDLA.deleteCharAt(destinationLatitude.length()-1);
                        stringBuilderDLA.deleteCharAt(destinationLatitude.length()-2);
                        dLatitude = stringBuilderDLA.toString();


//                        Log.d("sourceLongitude", "onResponse: " + sLongitude);
//                        Log.d("sourceLatitude", "onResponse: " + sLatitude);
//                        Log.d("destinationLongitude", "onResponse: " + dLongitude);
//                        Log.d("destinationLatitude", "onResponse: " + dLatitude);
//                        Log.d("DirectionRoute", "onResponse: " + strings[0]);

                        Log.d("longname", "onResponse: " + jsonObject.getString("longname"));
//                        Route route = new Route(jsonObject.getString("longname"));
//                        listRoute.add(route);
                        count++;
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }
                Log.d("sourceLongitude", "onResponse: " + sLongitude);
                Log.d("sourceLatitude", "onResponse: " + sLatitude);
                Log.d("destinationLongitude", "onResponse: " + dLongitude);
                Log.d("destinationLatitude", "onResponse: " + dLatitude);
//                origin = Point.fromLngLat(Double.parseDouble(sLongitude),
//                                Double.parseDouble(sLatitude));
//                destination = Point.fromLngLat(Double.parseDouble(dLongitude),
//                                Double.parseDouble(dLatitude));
                origin = Point.fromLngLat(38.76129,
                        9.06227);
                destination = Point.fromLngLat(8.98857,
                        8.98857);
                getRoute(style, origin, destination);
                initSource(style);
                initLayers(style);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(), "Error fetching routes", Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);
    }

    @Override
    public void onDidFinishLoadingMap() {
        Toast.makeText(this, "DONE LOADING", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDidFinishLoadingStyle() {
        Toast.makeText(this, "DONE LOADING STYLE", Toast.LENGTH_SHORT).show();

    }
}