package in.codeomega.parents;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.MenuItem;
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
import in.codeomega.parents.adapters.ImageAdapter;

import static in.codeomega.parents.interfaces.AppConstants.URLforGallery;
import static in.codeomega.parents.interfaces.AppConstants.URLforImages;
import static in.codeomega.parents.interfaces.AppConstants.URLforPictureBaseURL;

public class UploadedImages extends AppCompatActivity {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ProgressDialog progressDialog;

    ListView notificationListView;
    ArrayList<in.codeomega.parents.model.Image> images = new ArrayList<>();
    ImageAdapter imageAdapter;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploaded_images);

        progressDialog = AppController.getInstance().createProgressDialog(progressDialog, UploadedImages.this);

        imageAdapter = new ImageAdapter(UploadedImages.this,images);
        notificationListView = (ListView)findViewById(R.id.notificationListView);
        notificationListView.setAdapter(imageAdapter);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle("Gallery");
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        getMessages();

    }

    private void getMessages() {

        progressDialog.show();

        StringRequest getMessages = new StringRequest(Request.Method.POST,   URLforGallery, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                // TODO Auto-generated method stub

                Log.e("response", response);

                try {

                    JSONObject jsonObject = new JSONObject(response);

                    if (jsonObject.getBoolean("status")) {

                        JSONArray responseJsonArray =  jsonObject.getJSONArray("response");

                        String image,title,imgType,class_names,section_names,description,youtubelink,name,pic;
                        String baseUrl = URLforPictureBaseURL;
                        String imageUrl = URLforImages;

                        for (int i = 0; i < responseJsonArray.length() ; i++) {

                            image = imageUrl+responseJsonArray.getJSONObject(i).get("image").toString();
                            title = responseJsonArray.getJSONObject(i).get("title").toString();
                            imgType = responseJsonArray.getJSONObject(i).get("imgType").toString();
                            class_names = responseJsonArray.getJSONObject(i).get("class_names").toString().replace(",",", ");
                            section_names = responseJsonArray.getJSONObject(i).get("section_names").toString().replace(",",", ");
                            description = responseJsonArray.getJSONObject(i).get("description").toString();
                            youtubelink = responseJsonArray.getJSONObject(i).get("youtubelink").toString();
                            name = responseJsonArray.getJSONObject(i).get("emp_name").toString();
                            pic = baseUrl + responseJsonArray.getJSONObject(i).get("photo_nm").toString().replace("../upload/", "");

                            images.add(new in.codeomega.parents.model.Image(image,title,imgType,class_names,section_names,description,youtubelink,name,pic));
                            Log.e("pic", pic);
                        }

                    } else {

                        AppController.getInstance().show_popup_alert(jsonObject.getString("message"), UploadedImages.this);

                    }


                } catch (Exception e) {

                    Log.e("e", e.toString());
                    AppController.getInstance().show_popup_alert("Something went wrong, Please try later !", UploadedImages.this);

                }

                progressDialog.dismiss();
                imageAdapter.notifyDataSetChanged();

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                // TODO Auto-generated method stub

                progressDialog.dismiss();
                AppController.getInstance().show_toast("Unable to connect !", UploadedImages.this, Toast.LENGTH_SHORT);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }
    

    @Override
    public void onResume() {
        super.onResume();

        AppController.getInstance().notificationManager.cancelAll();
    }
}
