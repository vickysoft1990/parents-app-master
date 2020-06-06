package in.codeomega.parents.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import in.codeomega.parents.AppController;
import in.codeomega.parents.R;
import in.codeomega.parents.adapters.NotificationAdapter;
import us.zoom.sdk.ZoomSDK;
import us.zoom.sdk.ZoomSDKInitParams;
import us.zoom.sdk.ZoomSDKInitializeListener;

import static in.codeomega.parents.interfaces.Constants.APP_KEY;
import static in.codeomega.parents.interfaces.Constants.APP_SECRET;
import static in.codeomega.parents.interfaces.Constants.WEB_DOMAIN;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link Notification#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Notification extends Fragment implements ZoomSDKInitializeListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public Notification() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Notification.
     */
    // TODO: Rename and change types and number of parameters
    public static Notification newInstance(String param1, String param2) {
        Notification fragment = new Notification();
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

    ProgressDialog progressDialog;

    ListView notificationListView;
    ArrayList<in.codeomega.parents.model.Notification> notifications = new ArrayList<>();
    NotificationAdapter notificationAdapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        progressDialog = AppController.getInstance().createProgressDialog(progressDialog, getActivity());

        notificationAdapter = new NotificationAdapter(getActivity(),notifications);
        notificationListView = (ListView)view.findViewById(R.id.notificationListView);
        notificationListView.setAdapter(notificationAdapter);

        getMessages();
        ZoomSDK zoomSDK = ZoomSDK.getInstance();
        if(savedInstanceState == null) {
            ZoomSDKInitParams initParams = new ZoomSDKInitParams();
            initParams.appKey = APP_KEY;
            initParams.appSecret = APP_SECRET;
            initParams.domain = WEB_DOMAIN;
            zoomSDK.initialize(getContext(), this, initParams);
        }
        return view;
    }

    private void getMessages() {

        progressDialog.show();

        Log.e("getMessages", "" + getResources().getString(R.string.base_url) + getResources().getString(R.string.getMessages));

        StringRequest getMessages = new StringRequest(Request.Method.POST, getResources().getString(R.string.base_url) + getResources().getString(R.string.getMessages), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

                Log.e("response", response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("status")) {

                        JSONArray responseJsonArray =  jsonObject.getJSONArray("response");

                        String message,message_type,name,pic,voice_text,date,class_names,section_names;
                        String baseUrl = getResources().getString(R.string.pictureBaseUrl);

                        for (int i = 0; i < responseJsonArray.length() ; i++) {

                            message = responseJsonArray.getJSONObject(i).get("message").toString();
                            message_type = responseJsonArray.getJSONObject(i).get("message_type").toString();
                            name = responseJsonArray.getJSONObject(i).get("emp_name").toString();
                            pic = baseUrl + responseJsonArray.getJSONObject(i).get("photo_nm").toString().replace("../upload/", "");

                            class_names = responseJsonArray.getJSONObject(i).get("class_names").toString().replace(",",", ");
                            section_names = responseJsonArray.getJSONObject(i).get("section_names").toString().replace(",",", ");
                            voice_text = responseJsonArray.getJSONObject(i).get("voice_text").toString();
                            date = responseJsonArray.getJSONObject(i).get("updated").toString();

                            notifications.add(new in.codeomega.parents.model.Notification(message,message_type,name,pic,voice_text,date,class_names,section_names));
                            Log.e("pic", pic);
                        }

                    } else {

                        AppController.getInstance().show_popup_alert(jsonObject.getString("message"), getActivity());

                    }


                } catch (Exception e) {

                    Log.e("e", e.toString());
                    AppController.getInstance().show_popup_alert("Something went wrong, Please try later !", getActivity());

                }

                progressDialog.dismiss();
                notificationAdapter.notifyDataSetChanged();

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
                params.put("user_id", AppController.getInstance().preferences.getString("user_id", ""));
                params.put("std", AppController.getInstance().preferences.getString("class", ""));
                params.put("sec", AppController.getInstance().preferences.getString("section", ""));
                params.put("academic_year", AppController.getInstance().preferences.getString("academic_year", ""));
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

        AppController.getInstance().setRetryPolicies(getMessages);
        Log.e("request", "getMessages :" + "sent");

        AppController.getInstance().requestQueue.add(getMessages);

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
    public void onZoomSDKInitializeResult(int i, int i1) {

    }

    @Override
    public void onZoomAuthIdentityExpired() {

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

    @Override
    public void onResume() {
        super.onResume();

        AppController.getInstance().notificationManager.cancelAll();
        mListener = (OnFragmentInteractionListener) getActivity();
    }
}
