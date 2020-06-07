package in.codeomega.parents.fragments;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.codeomega.parents.AppController;
import in.codeomega.parents.R;
import in.codeomega.parents.model.Van;

import static in.codeomega.parents.interfaces.AppConstants.URLforGetRoute;
import static in.codeomega.parents.interfaces.AppConstants.URLforGetVanNo;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Track.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Track#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Track extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Track() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Track.
     */
    // TODO: Rename and change types and number of parameters
    public static Track newInstance(String param1, String param2) {
        Track fragment = new Track();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private GoogleMap mMap;
    ProgressDialog progressDialog = null;

    ArrayList<String> vanNames = new ArrayList<>();
    ArrayList<Van> vanArrayList = new ArrayList<>();
    ArrayAdapter<String> vanAdapter;
    private Spinner vanSpinner;
    Marker vanMarker;
    MyBroadcastReceiver receiver;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_track, container, false);

        progressDialog = AppController.getInstance().createProgressDialog(progressDialog, getActivity());

        this.vanSpinner = (Spinner) view.findViewById(R.id.van);

//        SupportMapFragment mapFragment = (SupportMapFragment) getActivity().getSupportFragmentManager()
//                .findFragmentById(R.id.map);

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);  //use SuppoprtMapFragment for using in fragment instead of activity  MapFragment = activity   SupportMapFragment = fragment

        mapFragment.getMapAsync(this);

        vanSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int i, long l) {

                final int oldPosition = AppController.getInstance().preferences.getInt("van",0);

                Log.e("old","van_"+vanArrayList.get(oldPosition).refid+"_"+vanArrayList.get(oldPosition).van_no);
                Log.e("new","van_"+vanArrayList.get(i).refid+"_"+vanArrayList.get(i).van_no);


                FirebaseMessaging.getInstance().unsubscribeFromTopic("van_"+vanArrayList.get(oldPosition).refid+"_"+vanArrayList.get(oldPosition).van_no)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                FirebaseMessaging.getInstance().subscribeToTopic("van_"+vanArrayList.get(i).refid+"_"+vanArrayList.get(i).van_no)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                AppController.getInstance().editor.putInt("van",i);
                                                AppController.getInstance().editor.putString("van_refid",vanArrayList.get(i).refid);
                                                AppController.getInstance().editor.commit();

                                            }
                                        });
                            }
                        });

                getRoute(vanArrayList.get(i).van_no);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        get_van_nos();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("in.codeomega.parents.NOTIFICATION_VAN");
        receiver = new MyBroadcastReceiver();
        getActivity().registerReceiver(receiver, intentFilter);


        return view;
    }

    private void getRoute(final String van_no) {

        progressDialog.show();

        StringRequest getRoute = new StringRequest(Request.Method.POST, URLforGetRoute, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

                try {

                    JSONObject jsonObject = new JSONObject(response);


                    Log.e("response", jsonObject.toString(1));

                    mMap.clear();

                    if (jsonObject.getBoolean("status")) {

                        JSONArray responseJsonArray = jsonObject.getJSONArray("response");
                        JSONArray vanJsonArray = jsonObject.getJSONArray("van_nos");
                        JSONObject stop, van;
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();

                        for (int i = 0; i < responseJsonArray.length(); i++) {

                            stop = responseJsonArray.getJSONObject(i);

                            LatLng stopLatLng = new LatLng(Double.parseDouble(stop.get("lat").toString()), Double.parseDouble(stop.get("lng").toString()));

                            View customMarkerView;
                            TextView name;
                            Bitmap returnedBitmap;
                            Canvas canvas;
                            Drawable drawable;

                            customMarkerView = getLayoutInflater().inflate(R.layout.custom_marker, null);
                            name = (TextView) customMarkerView.findViewById(R.id.name);
                            name.setText(stop.get("route").toString());

                            customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                            customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
                            customMarkerView.buildDrawingCache();
                            returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                                    Bitmap.Config.ARGB_8888);
                            canvas = new Canvas(returnedBitmap);
                            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
                            drawable = customMarkerView.getBackground();
                            if (drawable != null)
                                drawable.draw(canvas);
                            customMarkerView.draw(canvas);

                            mMap.addMarker(new MarkerOptions()
                                    .position(stopLatLng)
                                    .icon(BitmapDescriptorFactory.fromBitmap(returnedBitmap))).setTag("stop");
                        }

                        for (int i = 0; i < vanJsonArray.length(); i++) {

                            van = vanJsonArray.getJSONObject(i);

                            LatLng vanLatLng = new LatLng(Double.parseDouble(van.get("lat").toString()), Double.parseDouble(van.get("lng").toString()));

                            View customMarkerView;

                            Bitmap returnedBitmap;
                            Canvas canvas;
                            Drawable drawable;

                            customMarkerView = getLayoutInflater().inflate(R.layout.custom_van, null);

                            customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
                            customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
                            customMarkerView.buildDrawingCache();
                            returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                                    Bitmap.Config.ARGB_8888);
                            canvas = new Canvas(returnedBitmap);
                            canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
                            drawable = customMarkerView.getBackground();
                            if (drawable != null)
                                drawable.draw(canvas);
                            customMarkerView.draw(canvas);

                            vanMarker = mMap.addMarker(new MarkerOptions()
                                    .position(vanLatLng)
                                    .icon(BitmapDescriptorFactory.fromBitmap(returnedBitmap)));

                            vanMarker.setTag("van");

                            builder.include(vanLatLng);

                        }

                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 100));


                    } else {
                        AppController.getInstance().show_popup_alert(jsonObject.getString("message"), getActivity());
                    }

                } catch (Exception e) {

                    AppController.getInstance().printError(e);
                    AppController.getInstance().show_popup_alert("Something went wrong, Please try later !", getActivity());

                }

                progressDialog.dismiss();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO Auto-generated method stub

                progressDialog.dismiss();
                AppController.getInstance().show_toast("Unable to connect !", getActivity(), Toast.LENGTH_SHORT);
                Log.e("error", "" + volleyError.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // TODO Auto-generated method stub
                Map<String, String> params = new HashMap<String, String>();

                params.put("token", AppController.getInstance().preferences.getString("token", ""));
                params.put("btngetvan", "1");
                params.put("van_no", van_no);
                params.put("academic_year", AppController.getInstance().preferences.getString("academic_year", ""));

                Log.e("params", "" + params.toString());

                return params;

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // TODO Auto-generated method stub
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;

            }

        };

        AppController.getInstance().setRetryPolicies(getRoute);
        Log.e("request", "getRoute :" + "sent");

        AppController.getInstance().requestQueue.add(getRoute);
    }

    private void get_van_nos() {

        progressDialog.show();

        StringRequest get_van_nos = new StringRequest(Request.Method.POST, URLforGetVanNo, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

                Log.e("response", response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("status")) {

                        JSONArray vansJSONArray = jsonObject.getJSONArray("response");
                        
                        vanArrayList.clear();
                        vanNames.clear();

                        int vanPosition = 0;
                        String van_refid = AppController.getInstance().preferences.getString("van_refid","0");

                        for (int i = 0; i < vansJSONArray.length(); i++) {

                            vanArrayList.add(new Van(vansJSONArray.getJSONObject(i).get("refid").toString(),vansJSONArray.getJSONObject(i).get("van_no").toString()));
                            vanNames.add("Van No. "+vansJSONArray.getJSONObject(i).get("van_no").toString());

                            if(van_refid.equals(vansJSONArray.getJSONObject(i).get("refid").toString()))
                            {
                                vanPosition = i;
                            }

                        }

                        vanAdapter = new ArrayAdapter<String>(
                                getActivity(), android.R.layout.simple_spinner_item, vanNames);

                        vanAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        vanSpinner.setAdapter(vanAdapter);
                        vanAdapter.notifyDataSetChanged();
                        vanSpinner.setSelection(vanPosition);

                    } else {

                        AppController.getInstance().show_popup_alert(jsonObject.getString("message"), getActivity());
                    }


                } catch (Exception e) {

                    AppController.getInstance().printError(e);
                    AppController.getInstance().show_popup_alert("Something went wrong, Please try later !", getActivity());

                }

                progressDialog.dismiss();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO Auto-generated method stub
 
                progressDialog.dismiss();
                AppController.getInstance().show_toast("Unable to connect !", getActivity(), Toast.LENGTH_SHORT);
                Log.e("error", "" + volleyError.toString());
            }
        }) {

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                // TODO Auto-generated method stub
                Map<String, String> params = new HashMap<String, String>();

                params.put("token", AppController.getInstance().preferences.getString("token", ""));
                params.put("btngetmsg", "1");
                Log.e("params", "" + params.toString());

                return params;

            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                // TODO Auto-generated method stub
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;

            }

        };

        AppController.getInstance().setRetryPolicies(get_van_nos);
        Log.e("request", "get_van_nos :" + "sent");

        AppController.getInstance().requestQueue.add(get_van_nos);
        
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;



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


    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            try {

                Log.e("onReceive","onReceive "+intent.getDoubleExtra("lat",0.0)+" "+intent.getDoubleExtra("lng",0.0));
                vanMarker.setPosition(new LatLng(intent.getDoubleExtra("lat",0.0),intent.getDoubleExtra("lng",0.0)));

            }catch (Exception e)
            {
                AppController.getInstance().printError(e);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeReceiver();
    }

    private void removeReceiver() {

        try {

            getActivity().unregisterReceiver(receiver);

        }catch (Exception e)
        {
            AppController.getInstance().printError(e);

        }

    }


}
