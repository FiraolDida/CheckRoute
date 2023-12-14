package com.example.checkroute;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.Style;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link RouteFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RouteFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RouteFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
//    private static final String URL_ROUTES = "http://165.22.193.95/api/routes";
    private static final String URL_ROUTES = "http://shegeradmin.ml/api/routes";

    //    private static final String URL_ROUTES = "http://078610e7.ngrok.io/api/routes";
    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;
    private List<Route> listRoute;

    private Point origin;
    private Point destination;

    private String sLongitude;
    private String sLatitude;
    private String dLongitude;
    private String dLatitude;

    public RouteFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment RouteFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RouteFragment newInstance() {
        RouteFragment fragment = new RouteFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getShegerBusRoute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_route, container, false);
        recyclerView = view.findViewById(R.id.routeRecyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        listRoute = new ArrayList<>();

        return view;
    }

//    private void getRoutes() {
//        final ProgressDialog progressDialog = new ProgressDialog(getContext());
//        progressDialog.setMessage("Loading data...");
//        progressDialog.show();
//
//        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
//                URL_ROUTES, null, new Response.Listener<JSONArray>() {
//            @Override
//            public void onResponse(JSONArray response) {
//                int count = 0;
//                while (count < response.length()){
//                    try{
//                        progressDialog.dismiss();
//                        JSONObject jsonObject = response.getJSONObject(count);
//                        Route route = new Route(jsonObject.getString("longname"));
//                        listRoute.add(route);
//                        adapter = new RouteAdapter(listRoute, getContext());
//                        recyclerView.setAdapter(adapter);
//                        count++;
//                    }catch (JSONException e){
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                progressDialog.dismiss();
//                error.printStackTrace();
//                Toast.makeText(getContext(), "Error fetching routes", Toast.LENGTH_SHORT).show();
//            }
//        });
//        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
//        requestQueue.add(jsonArrayRequest);
//    }

    public void getShegerBusRoute() {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading data...");
        progressDialog.show();

        Log.d("RouteFragment", "getRoutes: ");
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET,
                URL_ROUTES, null, new com.android.volley.Response.Listener<JSONArray>() {
            @Override
            public void onResponse(JSONArray response) {
                int count = 0;
                while (count < response.length()){
                    try{
                        progressDialog.dismiss();
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

                        Route route = new Route(
                                jsonObject.getString("longname"),
                                sLongitude, sLatitude, dLongitude, dLatitude);

                        listRoute.add(route);
                        adapter = new RouteAdapter(listRoute, getContext());
                        recyclerView.setAdapter(adapter);

//                        Log.d("sourceLongitude", "onResponse: " + sLongitude);
//                        Log.d("sourceLatitude", "onResponse: " + sLatitude);
//                        Log.d("destinationLongitude", "onResponse: " + dLongitude);
//                        Log.d("destinationLatitude", "onResponse: " + dLatitude);
//                        Log.d("DirectionRoute", "onResponse: " + strings[0]);

                        Log.d("longname", "onResponse: " + jsonObject.getString("longname"));
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
//                        Double.parseDouble(sLatitude));
//                destination = Point.fromLngLat(Double.parseDouble(dLongitude),
//                        Double.parseDouble(dLatitude));
//                getRoute(style, origin, destination);
//                initSource(style);
//                initLayers(style);
            }
        }, new com.android.volley.Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getContext(), "Error fetching routes", Toast.LENGTH_SHORT).show();
                Log.d("errorfetchingroutes", "onErrorResponse: " + error.toString());
            }
        });
        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
        requestQueue.add(jsonArrayRequest);
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
