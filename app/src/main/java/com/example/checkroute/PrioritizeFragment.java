package com.example.checkroute;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
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
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PrioritizeFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PrioritizeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PrioritizeFragment extends Fragment implements OnMapReadyCallback,
        PermissionsListener, View.OnClickListener, View.OnTouchListener,
        MapboxMap.OnMarkerClickListener, RadioGroup.OnCheckedChangeListener {

    private MapboxMap mapboxMap;
    private MapView mapView;
    private Geocoder geocoder;
    private EditText initialLocation;
    private EditText destinationLocation;
    private Button prioritizeBtn;
    private RadioGroup prioritizationType;
    private RadioButton fare;
    private RadioButton time;
    private Marker stationMarker;
    List<Address> addresses;
    List<Station> listStation;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private OnFragmentInteractionListener mListener;
    private PermissionsManager permissionsManager;

    private static final String DESTINATION_URL = "http://admin.bemengede.ml/api/user_destination";
    private static final String PRIORITIZE_URL = "http://admin.bemengede.ml/api/prioritized_trip";
//    private static final String DESTINATION_URL = "http://de34e335.ngrok.io/api/user_destination";
//    private static final String PRIORITIZE_URL = "http://de34e335.ngrok.io/api/prioritized_trip";

    private static final int REQUEST_CODE_AUTOCOMPLETE = 1;
    private String geojsonSourceLayerId = "geojsonSourceLayerId";
    private double initialLatitiude;
    private double initialLongitude;
    private double destinationLatitiude;
    private double destinationLongitude;
    private String destinationName;
    private Boolean flag;
    private int id;
    private int idd;

    public PrioritizeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment PrioritizeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static PrioritizeFragment newInstance() {
        PrioritizeFragment fragment = new PrioritizeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        currentLocation();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Mapbox.getInstance(getContext(), getString(R.string.mapbox_access_token));

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_prioritize, container, false);

        geocoder = new Geocoder(getContext(), Locale.getDefault());
        initialLocation = view.findViewById(R.id.initialLocation);
        destinationLocation = view.findViewById(R.id.destinationLocation);
        prioritizeBtn = view.findViewById(R.id.prioritizeBtn);
        prioritizationType = view.findViewById(R.id.prioritizeType);
        fare = view.findViewById(R.id.fareRadio);
        time = view.findViewById(R.id.timeRadio);

        prioritizeBtn.setOnClickListener(this);
        destinationLocation.setOnTouchListener(this);
        prioritizationType.setOnCheckedChangeListener(this);


        recyclerView = view.findViewById(R.id.prioritizeRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        listStation = new ArrayList<>();

        mapView = view.findViewById(R.id.mapViewPrioritize);


        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        return view;
    }

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
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setOnMarkerClickListener((MapboxMap.OnMarkerClickListener) this);
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableLocationComponent(style);
                currentLocation();
                LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiverNearby,
                        new IntentFilter("find_nearby_station"));
            }
        });
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
    private void currentLocation() {
        initialLatitiude = mapboxMap.getLocationComponent().getLastKnownLocation().getLatitude();
        initialLongitude = mapboxMap.getLocationComponent().getLastKnownLocation().getLongitude();

        try {
            addresses = geocoder.getFromLocation(initialLatitiude, initialLongitude, 1);
            Log.d("prioritize fragement", "address: " + addresses);
            initialLocation.setText(addresses.get(0).getAddressLine(0));
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {

        prioritizeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!destinationLocation.toString().isEmpty()){
                    if (idd == 2131230968){
                        Toast.makeText(getContext(), "in time", Toast.LENGTH_SHORT).show();
                        prioritize(initialLatitiude, initialLongitude, true);
                    } else if (idd == 2131230802) {
                        Toast.makeText(getContext(), "in fare", Toast.LENGTH_SHORT).show();
                        prioritize(initialLatitiude, initialLongitude, false);
                    } else {
                        Toast.makeText(getContext(), "Please fill the field", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Destination filled is empty", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP){
            Intent intent = new PlaceAutocomplete.IntentBuilder()
                    .accessToken(Mapbox.getAccessToken())
                    .placeOptions(PlaceOptions.builder()
                            .backgroundColor(Color.parseColor("#EEEEEE"))
                            .limit(10)
                            .country("ET")
                            .build(PlaceOptions.MODE_CARDS))
                    .build(getActivity());
            startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
            return true;
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

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

                    // get the selected location
                    destinationLatitiude = ((Point) selectedCarmenFeature.geometry()).latitude();
                    destinationLongitude = ((Point) selectedCarmenFeature.geometry()).longitude();
                    Log.d("prioritizeFragment", "desLat: " + destinationLatitiude +
                            ", destLon: " + destinationLongitude);

                    try {
                        addresses = geocoder.getFromLocation(destinationLatitiude,
                                destinationLongitude, 1);
                        Log.d("prioritizeFragment", "address: " +
                                addresses);
                        destinationLocation.setText(addresses.get(0).getAddressLine(0));

//                        recyclerView.setVisibility(View.VISIBLE);
                        mapView.setVisibility(View.VISIBLE);
                        prioritizeBtn.setEnabled(false);
                        nearbyDestinationStops(destinationLatitiude, destinationLongitude);
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    BroadcastReceiver broadcastReceiverNearby = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d("broadcastReceiverNearby", "onReceive: " +
                    intent.getExtras());
            destinationName = intent.getExtras().getString("destName");
            destinationLongitude = intent.getExtras().getDouble("destLat");
            destinationLatitiude = intent.getExtras().getDouble("destLon");

            try {
                addresses = geocoder.getFromLocation(destinationLatitiude,
                        destinationLongitude, 1);
                destinationLocation.setText(destinationName);
                recyclerView.setVisibility(View.GONE);
                prioritizeBtn.setEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public void nearbyDestinationStops(final double lat, final double lon){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, DESTINATION_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("sendDestinationLocation","Response" + response);
                        Toast.makeText(getContext(), "data Sent", Toast.LENGTH_SHORT).show();

                        try {
                            JSONArray jsonArray = new JSONArray(response);
                            for (int i = 0; i < jsonArray.length(); i++){
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                Station station = new Station(
                                        jsonObject.getString("name"),
                                        jsonObject.getInt("id"),
                                        jsonObject.getDouble("lat"),
                                        jsonObject.getDouble("lon")
                                );
                                listStation.add(station);
                                adapter = new PrioritizeAdapter(listStation, getContext());
                                recyclerView.setAdapter(adapter);
                                Log.d("sendDestinationLocation","Response" + jsonObject);
                                createStationMarker(station.getName(), station.getLatitude(),
                                        station.getLongitude(), station.getId());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Error sending stops", Toast.LENGTH_SHORT).show();
                Log.d("sendDestinationLocation","OnError: " + error.toString());
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("destinationLat", String.valueOf(lat));
                params.put("destinationLong", String.valueOf(lon));
                Log.d("sendDestinationLocation","GetParamsCalled");

                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }

    public void prioritize(final double lat, final double lon, final boolean flag){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, PRIORITIZE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("prioritize","Response" + response);
                        Toast.makeText(getContext(), "data Sent", Toast.LENGTH_SHORT).show();

                        if (!response.isEmpty()){
                            try {
                                JSONObject jsonObject = new JSONObject(response);
                                if (jsonObject.getString("flag").equals("false")){
                                    Log.d("prioritize","Response" + response);

                                    JSONObject getData = new JSONObject(jsonObject
                                            .getString("data"));

                                    Route route = new Route(
                                            getData.getString("initialStopLongitude"),
                                            getData.getString("initialStopLatitude"),
                                            getData.getString("destinationStopLongitude"),
                                            getData.getString("destinationStopLatitude")
                                    );

                                    Intent intent = new Intent("straight_route");
                                    intent.putExtra("sLon", route.getsLongitude());
                                    intent.putExtra("sLat", route.getsLatitude());
                                    intent.putExtra("dLon", route.getdLongitude());
                                    intent.putExtra("dLat", route.getdLatitude());
                                    LocalBroadcastManager.getInstance(getContext())
                                            .sendBroadcast(intent);

                                    Log.d("prioritize", "onResponse: " + route);

                                } else if (jsonObject.getString("flag").equals("true")){
                                    Log.d("prioritize","Response" + response);

                                    JSONObject getData = new JSONObject(jsonObject
                                            .getString("data"));

                                    Route route = new Route(
                                            getData.getString("initialStopLongitude"),
                                            getData.getString("initialStopLatitude"),
                                            getData.getString("destinationStopLongitude"),
                                            getData.getString("destinationStopLatitude"),
                                            getData.getString("middleStopLongitude"),
                                            getData.getString("middleStopLatitude")
                                    );

                                    Intent intent = new Intent("multiple_route");
                                    intent.putExtra("sLon", route.getsLongitude());
                                    intent.putExtra("sLat", route.getsLatitude());
                                    intent.putExtra("dLon", route.getdLongitude());
                                    intent.putExtra("dLat", route.getdLatitude());
                                    intent.putExtra("mLon", route.getmLongitude());
                                    intent.putExtra("mLat", route.getmLatitude());
                                    LocalBroadcastManager.getInstance(getContext())
                                            .sendBroadcast(intent);

                                    Log.d("prioritize", "onResponse: " + route);

                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Error in Prioritize",
                                        Toast.LENGTH_SHORT).show();
                                Log.d("prioritizeeez", "onResponse: " + response);
                                Log.d("prioritizeeez", "onResponse: " + e.toString());
                            }
                        }else {
                            Toast.makeText(getContext(), "Couldn't prioritize",
                                    Toast.LENGTH_SHORT).show();
                            Log.d("prioritizeeez", "onResponse: " + response);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getContext(), "Error sending data", Toast.LENGTH_SHORT).show();
                Log.d("prioritizeeez","OnError: " + error.toString());
                error.printStackTrace();
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("destinationStop", String.valueOf(id));
                params.put("sourceLat", String.valueOf(initialLatitiude));
                params.put("sourceLong", String.valueOf(initialLongitude));
                params.put("byLength", String.valueOf(flag));
                Log.d("prioritize","destinationStop: " +String.valueOf(id)
                    + ", sourceLat: " + String.valueOf(initialLatitiude)
                    + ", sourceLong: " +  String.valueOf(initialLongitude)
                    + ", byLength: " + String.valueOf(flag));

                return params;
            }

//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String>  params = new HashMap<String, String>();
//                params.put("Content-Type", " application/json");
//
//                return params;
//            }
        };

        stringRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 20000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 20000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }

    private void createStationMarker(String name, double latitude, double longitude, int id){
        stationMarker = mapboxMap.addMarker(new MarkerOptions()
                .position(new LatLng(latitude, longitude))
                .snippet(String.valueOf(id))
                .title(name));
        int zoom = (int) mapboxMap.getCameraPosition().zoom;
        mapboxMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(
                latitude,longitude), zoom), 4000, null);
//        stationMarker.showInfoWindow(mapboxMap, mapView);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker){
        Toast.makeText(getContext(), marker.getTitle(), Toast.LENGTH_SHORT).show();
        destinationName = marker.getTitle();
        destinationLongitude = marker.getPosition().getLongitude();
        destinationLatitiude = marker.getPosition().getLatitude();
        id = Integer.parseInt(marker.getSnippet());
        Log.d("onMarkerClickTest", "onMarkerClick: " + destinationLongitude
                + ", " + destinationLatitiude + ", id: " + id);
        destinationLocation.setText(destinationName);
        mapView.setVisibility(View.INVISIBLE);
        prioritizeBtn.setEnabled(true);
        mapboxMap.clear();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiverNearby,
                new IntentFilter("find_nearby_station"));
        Toast.makeText(getContext(), "onResume", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        idd = 0;
        idd = checkedId;
        Toast.makeText(getContext(), "id: " + checkedId, Toast.LENGTH_SHORT).show();
        Log.d("prioritizationType", "onCheckedChanged: " + checkedId);
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
