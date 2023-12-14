package com.example.checkroute;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonObject;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import timber.log.Timber;

import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapFragment extends Fragment implements OnMapReadyCallback,
        PermissionsListener, MapView.OnDidFinishRenderingMapListener {

    private static final String TAG = "MapFragment";
    private OnFragmentInteractionListener mListener;
    private MapView mapView;
    private Marker stationMarker;
    private Marker activeMarker;
    private MarkerOptions markerOptions;
    private MapboxMap mapboxMap;
//    private String URL_STOP = "http://165.22.193.95/api/stops";
    private String URL_STOP = "http://shegeradmin.ml/api/stops";
    private String URL_EID = "https://hst-api.wialon.com/wialon/ajax.html?svc=token/login&params={\"token\":\"492ffdab49f06d76d66afea082d03e036ED1B21777DFFE69597FF1087885216046999AA5\"}";
//    private String URL_EID = "https://hst-api.wialon.com/wialon/ajax.html?svc=token/" +
//        "login&params={%22token%22:%22492ffdab49f06d76d66afea082d03e033DC9121314B05AA3B" +
//        "FA1A6C689AB1759443551B5%22}";
    private String URL_BUS = "https://hst-api.wialon.com/wialon/ajax.html?svc=core/" +
            "update_data_flags&params={\"spec\":[{\"type\":\"type\",\"data\":\"avl_unit\"," +
            "\"flags\":1024,\"mode\":0}]}&sid=";
    private String URL_REAL_TIME_BUS = "http://hst-api.wialon.com/avl_evts?sid=";
//    private static final String URL_ROUTES = "http://165.22.193.95/api/routes";
    private static final String URL_ROUTES = "http://shegeradmin.ml/api/routes";

    private String eid;
    private Style dStyle;

    private Station station;
    private Bus bus;
    ArrayList<Station> arrayList;
    List<Feature> markerCoordinates;

    private static final String ROUTE_LAYER_ID = "route-layer-id";
    private static final String ROUTE_SOURCE_ID = "route-source-id";
    private static final String ICON_LAYER_ID = "icon-layer-id";
    private static final String ICON_SOURCE_ID = "icon-source-id";
    private static final String RED_PIN_ICON_ID = "red-pin-icon-id";

    private DirectionsRoute currentRoute;
    private MapboxDirections client;
    private Point origin = Point.fromLngLat(0, 0);
    private Point destination = Point.fromLngLat(0, 0);
    private Point middle = Point.fromLngLat(0, 0);

//    private String sLongitude;
//    private String sLatitude;
//    private String dLongitude;
//    private String dLatitude;

    private double sLongitude;
    private double sLatitude;
    private double dLongitude;
    private double dLatitude;
    private double mLongitude;
    private double mLatitude;

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private String symbolIconId = "symbolIconId";
    private PermissionsManager permissionsManager;
    private static Context context;
    private View view;
    private Timer timer;
    boolean locateFlag;

    public MapFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment MapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MapFragment newInstance(Context appContext) {
        MapFragment fragment = new MapFragment();
        context = appContext;
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));

        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        arrayList = new ArrayList<>();
        markerOptions = new MarkerOptions();

        timer = new Timer();

        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return view;
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                dStyle = style;
                initSearchFab();
                enableLocationComponent(style);
                currentLocation(style);

//                new LoadGeoJson(MapFragment.this).execute();
                locateStations();

                getEid();

                LocalBroadcastManager.getInstance(getContext()).
                        registerReceiver(broadcastReceiverStation,
                        new IntentFilter("find_station"));

                LocalBroadcastManager.getInstance(getContext()).
                        registerReceiver(broadcastReceiverRoute,
                                new IntentFilter("find_route"));

                LocalBroadcastManager.getInstance(getContext()).
                        registerReceiver(broadcastReceiverStraightRoute,
                        new IntentFilter("straight_route"));

                LocalBroadcastManager.getInstance(getContext()).
                        registerReceiver(broadcastReceiverMiddleRoute,
                        new IntentFilter("multiple_route"));

                // Add the symbol layer icon to map for future use
                style.addImage(symbolIconId, BitmapFactory.decodeResource(
                        MapFragment.this.getResources(), R.drawable.blue_marker));

                // Create an empty GeoJSON source using the empty feature collection
//                setUpSource(style);
                initSource(style);
                // Set up a new symbol layer for displaying the searched location's feature coordinates
                setupLayer(style);
            }
        });
    }
    

    private void setUpSource(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addSource(new GeoJsonSource(geojsonSourceLayerId));
    }

    private void setupLayer(@NonNull Style loadedMapStyle) {
//        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", geojsonSourceLayerId)
//                .withProperties(
//                iconImage(symbolIconId),
//                iconOffset(new Float[] {0f, -8f})
//        ));
        loadedMapStyle.addLayer(new SymbolLayer("SYMBOL_LAYER_ID", ROUTE_SOURCE_ID)
                .withProperties(
                        iconImage(symbolIconId),
                        iconOffset(new Float[] {0f, -8f})
                ));
    }

    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(getContext())) {

            // Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

            // Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(getContext(), loadedMapStyle).build());

            // Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

            // Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

            // Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(getActivity());
        }
    }

    @SuppressLint("MissingPermission")
    private void currentLocation(final Style style) {
        view.findViewById(R.id.findMyLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double lat = mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude();
                double lon = mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude();

                animateCamera(lat, lon);
            }
        });

        view.findViewById(R.id.refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                style.removeLayer(ROUTE_LAYER_ID);
                style.removeImage(RED_PIN_ICON_ID);
                style.removeImage(symbolIconId);
            }
        });
    }

    public void animateCamera(double lat, double lon){
        Log.d("animateCamera", "animateCamera: ");
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(new LatLng(lat,lon))
                        .zoom(16)
                        .build()), 4000);
    }

    private void initSearchFab() {
        view.findViewById(R.id.fab_location_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken())
                        .placeOptions(PlaceOptions.builder()
                                .backgroundColor(Color.parseColor("#EEEEEE"))
                                .limit(10)
                                .country("ET")
                                .build(PlaceOptions.MODE_CARDS))
                        .build(getActivity());
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);
            Log.d("MapFragementt", "lat: " + PlaceAutocomplete.getPlace(data).geometry());

            // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
            // Then retrieve and update the source designated for showing a selected location's symbol layer icon

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {
                    GeoJsonSource source = style.getSourceAs(geojsonSourceLayerId);
                    if (source != null) {
                        source.setGeoJson(FeatureCollection.fromFeatures(
                                new Feature[] {Feature.fromJson(selectedCarmenFeature.toJson())}));
                    }

                    // Move map camera to the selected location
                    animateCamera(((Point) selectedCarmenFeature.geometry()).latitude(),
                            ((Point) selectedCarmenFeature.geometry()).longitude());
                }
            }
        }
    }

    private void locateStations(){
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                URL_STOP, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int count = 0;
                while (count < response.length()){
                    try {
                        JSONObject jsonObject = response.getJSONObject(count);
                        Station station = new Station(
                                jsonObject.getString("name"),
                                jsonObject.getDouble("latitude"),
                                jsonObject.getDouble("longitude")
                        );
                        arrayList.add(station);
                        createStationMarker(station.getName(), station.getLatitude(),
                                station.getLongitude());
                        count++;
                    }catch (JSONException error){
                        error.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Error fetching JSON",
                        Toast.LENGTH_SHORT).show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonArrayRequest);
    }

    private void createStationMarker(String name, double latitude, double longitude){
        stationMarker = mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .title(name));
    }

    private void createMarker(String name, double latitude, double longitude, boolean flag){
        // Create an Icon object for the marker to use

        IconFactory iconFactory = IconFactory.getInstance(getContext());
//        Icon icon = iconFactory.fromResource(R.drawable.bus_realtime2);
        Icon icon = iconFactory.fromResource(R.drawable.blue_marker);

        markerOptions.position(new LatLng(latitude, longitude))
                .icon(icon)
                .title(name);

        if (flag){
            Log.d("FLAGTEST!", "createMarker: ");
            activeMarker = mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(latitude, longitude))
                    .icon(icon)
                    .title(name));
        }else {
            activeMarker.setPosition(new LatLng(latitude, longitude));
        }
    }

    private void getEid(){
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                URL_EID,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            eid = jsonObject.getString("eid");
                            Log.d(TAG, "eid: " + eid);
                            Log.d(TAG, "onResponse: " + response);
                            locateBus();
//                            realtimeBus();
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.d("EID", "onResponse: " + e.toString());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Error fetching EID",
                                Toast.LENGTH_SHORT).show();
                        error.printStackTrace();
                    }
                });
        fiveSecondScheduler();
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }

    private void locateBus(){
        final String URL = URL_BUS + eid;
        locateFlag = true;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                URL, null, new Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int count = 0;
                while (count < response.length()){
                    try{
                        JSONObject jsonObject = response.getJSONObject(count);
                        JSONObject getD = (JSONObject) jsonObject.get("d");
                        JSONObject getPos = (JSONObject) getD.get("pos");
                        bus = new Bus(jsonObject.getString("i"),
                                getPos.getDouble("y"),
                                getPos.getDouble("x"));

                        createMarker(bus.getId(), bus.getLatitude(),
                                bus.getLongitude(), locateFlag);
                        count++;
                    }catch (JSONException e){
                        e.printStackTrace();
                    }
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getContext(), "Error fetching buses", Toast.LENGTH_SHORT).show();
            }
        });
//        updateBus();
//        createMarker("", 0.0, 0.0, flag);
        fiveSecondScheduler();
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(jsonArrayRequest);
    }

    private void fiveSecondScheduler(){
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Log.d("TTTTTT", "run: ");
                realtimeBus();
//                getEid();
//                locateBus();
            }
        },0, 3000);
    }

    private void realtimeBus(){
        final String URL = URL_REAL_TIME_BUS + eid;
        final boolean flag = false;
        Log.d("checkkk", "onResponse: " + URL);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        int count = 0;
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray getEvent = jsonObject.getJSONArray("events");

                            while (count < getEvent.length()){
                                JSONObject json_object = getEvent.getJSONObject(count);
                                JSONObject getD = (JSONObject) json_object.get("d");
                                JSONObject getPos = getD.getJSONObject("pos");

                                bus = new Bus(json_object.getString("i"),
                                        getPos.getDouble("y"),
                                        getPos.getDouble("x"));

                                Log.d("checkkkk", "id: " + bus.getId() +
                                        ", lat: " + bus.getLatitude() +
                                        ", lon: " + bus.getLongitude());

                                createMarker(bus.getId(), bus.getLatitude(),
                                        bus.getLongitude(), flag);

                                count++;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
//                            Toast.makeText(getContext(), "Error fetching active buses!",
//                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
//                Toast.makeText(getContext(), "Error fetching active buses", Toast.LENGTH_SHORT)
//                        .show();
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }

    BroadcastReceiver broadcastReceiverStation = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("broadcastttt", "before animate station: ");
            animateCamera(intent.getExtras().getDouble("lat"),
                    intent.getExtras().getDouble("lon"));
            Log.d("broadcastttt", "after animate station: ");
        }
    };

    BroadcastReceiver broadcastReceiverRoute = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            sLongitude = Double.parseDouble(intent.getExtras().getString("sLon"));
            sLatitude = Double.parseDouble(intent.getExtras().getString("sLat"));
            dLongitude = Double.parseDouble(intent.getExtras().getString("dLon"));
            dLatitude = Double.parseDouble(intent.getExtras().getString("dLat"));

            origin = Point.fromLngLat(sLongitude, sLatitude);
            destination = Point.fromLngLat(dLongitude, dLatitude);

            getRoute(dStyle, origin, destination);
//            initSource(dStyle);
            initLayers(dStyle);
            animateCamera(sLatitude, sLongitude);
        }
    };
    BroadcastReceiver broadcastReceiverStraightRoute = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ReceiverStraightRoute", "onReceive: ");
            sLongitude = Double.parseDouble(intent.getExtras().getString("sLon"));
            sLatitude = Double.parseDouble(intent.getExtras().getString("sLat"));
            dLongitude = Double.parseDouble(intent.getExtras().getString("dLon"));
            dLatitude = Double.parseDouble(intent.getExtras().getString("dLat"));

            origin = Point.fromLngLat(sLongitude, sLatitude);
            destination = Point.fromLngLat(dLongitude, dLatitude);

            getRoute(dStyle, origin, destination);
            Log.d("afterRoute", "onReceive: ");
            initLayers(dStyle);
            animateCamera(sLatitude, sLongitude);
            Log.d("afterAnimateCamera", "onReceive: ");
        }
    };

    BroadcastReceiver broadcastReceiverMiddleRoute = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("ReceiverMiddleRoute", "onReceive: ");
            sLongitude = Double.parseDouble(intent.getExtras().getString("sLon"));
            sLatitude = Double.parseDouble(intent.getExtras().getString("sLat"));
            dLongitude = Double.parseDouble(intent.getExtras().getString("dLon"));
            dLatitude = Double.parseDouble(intent.getExtras().getString("dLat"));
            mLongitude = Double.parseDouble(intent.getExtras().getString("mLon"));
            mLatitude = Double.parseDouble(intent.getExtras().getString("mLat"));

            origin = Point.fromLngLat(sLongitude, sLatitude);
            middle = Point.fromLngLat(mLongitude, mLatitude);
            destination = Point.fromLngLat(dLongitude, dLatitude);

            getRoute(dStyle, origin, middle);
            getRoute(dStyle, middle, destination);
            Log.d("afterRoute", "onReceive: ");
            initLayers(dStyle);
            animateCamera(sLatitude, sLongitude);
            Log.d("afterAnimateCamera", "onReceive: ");
        }
    };

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

    private void initLayers(@NonNull Style loadedMapStyle) {
        loadedMapStyle.removeLayer(ROUTE_LAYER_ID);
        loadedMapStyle.removeLayer(ICON_LAYER_ID);
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


//         Add the red marker icon SymbolLayer to the map
        loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
                iconImage(RED_PIN_ICON_ID),
                iconIgnorePlacement(true),
                iconIgnorePlacement(true),
                iconOffset(new Float[] {0f, -4f})));
    }

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
                                   retrofit2.Response<DirectionsResponse> response) {
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
                Toast.makeText(getContext(), "Error: " +
                        throwable.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

//    public void getShegerBusRoute(final Style style) {
//        Log.d("DirectionRoute", "getRoutes: ");
//        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
//                URL_ROUTES, null, new com.android.volley.Response.Listener<JSONArray>() {
//            @Override
//            public void onResponse(JSONArray response) {
//                int count = 0;
//                while (count < response.length()){
//                    try{
//                        JSONObject jsonObject = response.getJSONObject(count);
//                        JSONObject jsonObject1 = new JSONObject(jsonObject.getString("points"));
//                        JSONArray jsonArray = jsonObject1.getJSONArray("coordinates");
//                        List<String> list = new ArrayList<String>();
//                        if (jsonArray != null){
//                            for (int i = 0; i < jsonArray.length(); i++){
//                                list.add(jsonArray.getString(i));
//                            }
//                        }
//
//                        String[] strings = list.toArray(new String[0]);
//                        String[] args = strings[0].split(",");
//                        String sourceLongitude = args[0];
//                        String sourceLatitude = args[1];
//                        String destinationLongitude = args[args.length - 2];
//                        String destinationLatitude = args[args.length - 1];
//
//                        StringBuilder stringBuilderSLO = new StringBuilder(sourceLongitude);
//                        stringBuilderSLO.deleteCharAt(0);
//                        stringBuilderSLO.deleteCharAt(0);
//                        sLongitude = stringBuilderSLO.toString();
//
//                        StringBuilder stringBuilderSLA = new StringBuilder(sourceLatitude);
//                        stringBuilderSLA.deleteCharAt(sourceLatitude.length()-1);
//                        sLatitude = stringBuilderSLA.toString();
//
//                        StringBuilder stringBuilderDLO = new StringBuilder(destinationLongitude);
//                        stringBuilderDLO.deleteCharAt(0);
//                        dLongitude = stringBuilderDLO.toString();
//
//                        StringBuilder stringBuilderDLA = new StringBuilder(destinationLatitude);
//                        stringBuilderDLA.deleteCharAt(destinationLatitude.length()-1);
//                        stringBuilderDLA.deleteCharAt(destinationLatitude.length()-2);
//                        dLatitude = stringBuilderDLA.toString();
//
////                        Log.d("sourceLongitude", "onResponse: " + sLongitude);
////                        Log.d("sourceLatitude", "onResponse: " + sLatitude);
////                        Log.d("destinationLongitude", "onResponse: " + dLongitude);
////                        Log.d("destinationLatitude", "onResponse: " + dLatitude);
////                        Log.d("DirectionRoute", "onResponse: " + strings[0]);
//
//                        Log.d("longname", "onResponse: " + jsonObject.getString("longname"));
//                        count++;
//                    }catch (JSONException e){
//                        e.printStackTrace();
//                    }
//                }
//                Log.d("sourceLongitude", "onResponse: " + sLongitude);
//                Log.d("sourceLatitude", "onResponse: " + sLatitude);
//                Log.d("destinationLongitude", "onResponse: " + dLongitude);
//                Log.d("destinationLatitude", "onResponse: " + dLatitude);
//                origin = Point.fromLngLat(Double.parseDouble(sLongitude),
//                        Double.parseDouble(sLatitude));
//                destination = Point.fromLngLat(Double.parseDouble(dLongitude),
//                        Double.parseDouble(dLatitude));
//                getRoute(style, origin, destination);
//                initSource(style);
//                initLayers(style);
//            }
//        }, new com.android.volley.Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                error.printStackTrace();
//                Toast.makeText(getContext(), "Error fetching routes", Toast.LENGTH_SHORT).show();
//            }
//        });
//        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
//        requestQueue.add(jsonArrayRequest);
//    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        Toast.makeText(getContext(), "onDetach", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiverStation,
                new IntentFilter("find_station"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiverRoute,
                new IntentFilter("find_route"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiverStraightRoute,
                new IntentFilter("straight_route"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiverMiddleRoute,
                new IntentFilter("multiple_route"));
        Toast.makeText(getContext(), "onResume", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPause() {
        super.onPause();
        Toast.makeText(getContext(), "onPause", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(getContext(), "permission not granted", Toast.LENGTH_LONG).show();
            getActivity().finish();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDidFinishRenderingMap(boolean fully) {
        Log.d("onDidFinishRenderingMap", "onDidFinishRenderingMap: " + fully);
        Toast.makeText(context, "READY", Toast.LENGTH_SHORT).show();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
